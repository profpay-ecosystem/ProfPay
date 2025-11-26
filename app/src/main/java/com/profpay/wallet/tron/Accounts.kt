package com.profpay.wallet.tron

import android.util.Log
import com.profpay.wallet.AppConstants
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.contract.Contract
import org.tron.trident.core.contract.Trc20Contract
import org.tron.trident.core.key.KeyPair
import org.tron.trident.proto.Response
import java.math.BigInteger

// Обработка Tron API раздела Accounts.
class Accounts {
    // Получаем ресурсы аккаунта.
    fun getAccountResource(
        ownerAddress: String,
        privateKey: String,
    ): Response.AccountResourceMessage {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)
        return wrapper.getAccountResource(ownerAddress)
    }

    // Информация об учетной записи, включая баланс TRX, балансы TRC-10, информацию о ставках,
    // информация о голосовании и разрешениях и т. д.
    fun getAccount(
        ownerAddress: String,
        privateKey: String,
    ): Response.Account {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)
        return wrapper.getAccount(ownerAddress)
    }

    fun allowance(
        spender: String,
        ownerAddress: String,
        privateKey: String,
    ): BigInteger? {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val contract: Contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
        val token = Trc20Contract(contract, ownerAddress, wrapper)
        val result = token.allowance(ownerAddress, spender)

        wrapper.close()
        return result
    }

    fun isAllowanceUnlimited(
        spender: String,
        ownerAddress: String,
        privateKey: String,
    ): Boolean {
        val wrapper = ApiWrapper(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY, privateKey)

        val contract: Contract = wrapper.getContract("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
        val token = Trc20Contract(contract, ownerAddress, wrapper)
        val result = token.allowance(ownerAddress, spender)

        wrapper.close()

        val max = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(10L).pow(6))
        return result >= max
    }

    fun hasEnoughBandwidth(
        address: String?,
        requiredBandwidth: Long,
    ): Boolean {
        return TronNodeManager.executeWithFailover { node ->
            val wrapper = ApiWrapper(node.grpc, node.solidityGrpc, KeyPair.generate().toPrivateKey())
            val resources = wrapper.getAccountResource(address)

            try {
                val freeNetRemaining: Long = resources.freeNetLimit - resources.freeNetUsed
                val paidNetRemaining: Long = resources.netLimit - resources.netUsed

                val totalAvailableBandwidth = freeNetRemaining + paidNetRemaining

                totalAvailableBandwidth >= requiredBandwidth
            } finally {
                try {
                    wrapper.close()
                } catch (e: Exception) {
                    Log.e("wrapper.close()", "Warning: failed to close wrapper: $e")
                }
            }
        }
    }
}
