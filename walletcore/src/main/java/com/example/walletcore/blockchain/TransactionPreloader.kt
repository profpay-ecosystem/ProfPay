package com.example.walletcore.blockchain

import com.example.walletcore.ConfirmParams
import com.example.walletcore.model.SignerParams

interface GenericTransferPreloader : ChainClient {
    suspend fun preloadGeneric(params: ConfirmParams.TransferParams.Generic): SignerParams
}

interface NativeTransferPreloader : ChainClient {
    suspend fun preloadNativeTransfer(params: ConfirmParams.TransferParams.Native): SignerParams
}

interface TokenTransferPreloader : ChainClient {
    suspend fun preloadTokenTransfer(params: ConfirmParams.TransferParams.Token): SignerParams
}