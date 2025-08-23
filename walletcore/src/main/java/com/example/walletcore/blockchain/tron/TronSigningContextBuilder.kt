package com.example.walletcore.blockchain.tron

import com.example.walletcore.ConfirmParams
import com.example.walletcore.blockchain.NativeTransferPreloader
import com.example.walletcore.blockchain.TokenTransferPreloader
import com.example.walletcore.blockchain.tron.models.TronAccount
import com.example.walletcore.blockchain.tron.models.TronAccountUsage
import com.example.walletcore.blockchain.tron.services.TronAccountsService
import com.example.walletcore.blockchain.tron.services.TronCallService
import com.example.walletcore.blockchain.tron.services.TronNodeStatusService
import com.example.walletcore.blockchain.tron.services.getAccount
import com.example.walletcore.blockchain.tron.services.getAccountUsage
import com.example.walletcore.model.ChainSignData
import com.example.walletcore.model.Fee
import com.example.walletcore.model.SignerParams
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.FeePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class TronSigningContextBuilder(
    private val chain: Chain,
    private val nodeStatusService: TronNodeStatusService,
    private val accountsService: TronAccountsService,
    callService: TronCallService,
) : NativeTransferPreloader, TokenTransferPreloader {
    val feeCalculator = TronFeeCalculator(chain, nodeStatusService, callService)

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return preload(
            params, { account, usage ->
                feeCalculator.calculate(params, account, usage)
            }
        ) { emptyMap() }
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return preload(
            params, { account, usage ->
                feeCalculator.calculate(params, account, usage)
            }
        ) { emptyMap() }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

    private suspend fun preload(
        params: ConfirmParams,
        feeCalc: suspend (TronAccount?, TronAccountUsage?) -> Fee,
        votes: suspend (TronAccount?) -> Map<String, Long>,
    ): SignerParams = withContext(Dispatchers.IO) {
        val getAccountUsage = async { accountsService.getAccountUsage(params.from.address) }
        val getAccount = async { accountsService.getAccount(params.from.address, true) }
        val nowBlockJob = async { nodeStatusService.nowBlock() }

        val nowBlock = nowBlockJob.await().getOrThrow()
        val account = getAccount.await()
        val accountUsage = getAccountUsage.await()

        val fee = feeCalc(account, accountUsage)

        SignerParams(
            input = params,
            chainData = TronChainData(
                number = nowBlock.block_header.raw_data.number,
                version = nowBlock.block_header.raw_data.version,
                txTrieRoot = nowBlock.block_header.raw_data.txTrieRoot,
                witnessAddress = nowBlock.block_header.raw_data.witness_address,
                parentHash = nowBlock.block_header.raw_data.parentHash,
                timestamp = nowBlock.block_header.raw_data.timestamp,
                fee = fee,
                votes = votes(account)
            )
        )
    }

    data class TronChainData(
        val number: Long,
        val version: Long,
        val txTrieRoot: String,
        val witnessAddress: String,
        val parentHash: String,
        val timestamp: Long,
        val fee: Fee,
        val votes: Map<String, Long> = emptyMap()
    ) : ChainSignData {
        override fun fee(speed: FeePriority): Fee = fee
    }
}