package com.example.walletcore.blockchain.tron

import com.example.walletcore.ConfirmParams
import com.example.walletcore.blockchain.tron.models.TronAccount
import com.example.walletcore.blockchain.tron.models.TronAccountUsage
import com.example.walletcore.blockchain.tron.services.TronCallService
import com.example.walletcore.blockchain.tron.services.TronNodeStatusService
import com.example.walletcore.blockchain.tron.services.triggerSmartContract
import com.example.walletcore.math.toHexString
import com.example.walletcore.model.Fee
import com.example.walletcore.primitives.AssetId
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58
import java.math.BigDecimal
import java.math.BigInteger

class TronFeeCalculator(
    private val chain: Chain,
    private val nodeStatusService: TronNodeStatusService,
    private val callService: TronCallService,
) {
    val baseFee: BigInteger = BigInteger.valueOf(280_000)

    suspend fun calculate(params: ConfirmParams.TransferParams.Native, account: TronAccount?, accountUsage: TronAccountUsage?) = withContext(Dispatchers.IO) {
        val getParams = async { nodeStatusService.getChainParameters().fold({ it.chainParameter }) { null } }
        val params = getParams.await()

        val isNewAccount = account?.address.isNullOrEmpty()

        val newAccountFee = params?.firstOrNull{ it.key == "getCreateAccountFee" }?.value
        val newAccountFeeInSmartContract = params?.firstOrNull { it.key == "getCreateNewAccountFeeInSystemContract" }?.value
        val availableBandwidth = (accountUsage?.freeNetLimit ?: 0) - (accountUsage?.freeNetUsed ?: 0)
        val coinTransferFee = if (availableBandwidth >= 300) BigInteger.ZERO else baseFee

        if (newAccountFeeInSmartContract == null || newAccountFee == null) {
            throw Exception("Tron unknown key")
        }

        val fee = if (isNewAccount) {
            coinTransferFee + BigInteger.valueOf(newAccountFee + newAccountFeeInSmartContract)
        } else {
            coinTransferFee
        }
        Fee(FeePriority.Normal, AssetId(chain), fee)
    }

    suspend fun calculate(params: ConfirmParams.TransferParams.Token, account: TronAccount?, accountUsage: TronAccountUsage?) = withContext(Dispatchers.IO) {
        val getParams = async { nodeStatusService.getChainParameters().fold({ it.chainParameter }) { null } }

        // https://developers.tron.network/docs/set-feelimit#how-to-estimate-energy-consumption
        val gasLimit = estimateTRC20Transfer(
            ownerAddress = params.from.address,
            recipientAddress =  params.destination.address,
            contractAddress = params.assetId.tokenId ?: throw IllegalArgumentException("Incorrect contract on fee calculation"),
            value = params.amount,
        )

        val isNewAccount = account?.address.isNullOrEmpty()
        val params = getParams.await()
        val newAccountFeeInSmartContract = params?.firstOrNull { it.key == "getCreateNewAccountFeeInSystemContract" }?.value
        val energyFee = params?.firstOrNull { it.key == "getEnergyFee" }?.value
        val availableEnergy = BigInteger.valueOf(accountUsage?.EnergyLimit ?: 0L) - BigInteger.valueOf(accountUsage?.EnergyUsed ?: 0L)
        val energyShortfall = BigInteger.ZERO.max(gasLimit.let { it.add(it.multiply(BigDecimal(0.2))).toBigInteger() } - availableEnergy)

        if (newAccountFeeInSmartContract == null || energyFee == null) {
            throw Exception("Tron unknown key")
        }

        val tokenTransferFee = BigInteger.valueOf(energyFee) * energyShortfall
        val fee = if (isNewAccount) {
            tokenTransferFee + BigInteger.valueOf(newAccountFeeInSmartContract)
        } else {
            tokenTransferFee
        }
        Fee(FeePriority.Normal, AssetId(chain), fee)
    }

    // https://developers.tron.network/docs/set-feelimit#how-to-estimate-energy-consumption
    private suspend fun estimateTRC20Transfer(
        ownerAddress: String,
        recipientAddress: String,
        contractAddress: String,
        value: BigInteger
    ): BigDecimal {
        val address = Base58.decode(recipientAddress).toHexString("")
        val parameter = listOf(
            address,
            value.toByteArray().toHexString("")
        ).joinToString(separator = "") { it.padStart(64, '0') }

        val response = callService.triggerSmartContract(
            contractAddress = contractAddress,
            functionSelector = "transfer(address,uint256)",
            parameter = parameter,
            feeLimit = 0L,
            callValue = 0L,
            ownerAddress = ownerAddress,
            visible = true
        )
        val result = response.getOrNull()
        if (result == null || !result.result.message.isNullOrEmpty()) {
            throw IllegalStateException("Can't get gas limit")
        }
        return BigDecimal.valueOf(result.energy_used.toLong())
    }
}