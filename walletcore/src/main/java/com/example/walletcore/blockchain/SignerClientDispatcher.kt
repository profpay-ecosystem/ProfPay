package com.example.walletcore.blockchain

import com.example.walletcore.ConfirmParams
import com.example.walletcore.model.SignerParams
import com.example.walletcore.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SignerClientDispatcher(
    private val nativeTransferClients: List<NativeTransferPreloader>,
    private val tokenTransferClients: List<TokenTransferPreloader>,
    private val genericPreloaderClients: List<GenericTransferPreloader>,
) : GenericTransferPreloader, NativeTransferPreloader, TokenTransferPreloader {

    suspend fun preload(params: ConfirmParams): SignerParams = withContext(Dispatchers.IO) {

        val preloadJob = async {
            when (params) {
                is ConfirmParams.TransferParams.Native -> preloadNativeTransfer(params)
                is ConfirmParams.TransferParams.Token -> preloadTokenTransfer(params)
                is ConfirmParams.TransferParams.Generic -> preloadGeneric(params)
                is ConfirmParams.Activate -> TODO()
            }
        }
        preloadJob.await()
    }

    override fun supported(chain: Chain): Boolean {
        return (nativeTransferClients
                + tokenTransferClients).getClient(chain) != null
    }

    override suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams {
        return nativeTransferClients.getClient(params.from.chain)?.preloadNativeTransfer(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams {
        return tokenTransferClients.getClient(params.from.chain)?.preloadTokenTransfer(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }

    override suspend fun preloadGeneric(params: ConfirmParams.TransferParams.Generic): SignerParams {
        return genericPreloaderClients.getClient(params.from.chain)?.preloadGeneric(params = params)
            ?: throw IllegalArgumentException("Chain isn't support")
    }
}