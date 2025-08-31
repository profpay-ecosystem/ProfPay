package com.example.telegramWallet.data.flow_db

import com.example.walletcore.BlockchainRpcManager
import com.example.walletcore.blockchain.BlockchainSignManager
import com.example.walletcore.blockchain.BroadcastClientDispatcher
import com.example.walletcore.blockchain.GenericTransferPreloader
import com.example.walletcore.blockchain.SignerClientDispatcher
import com.example.walletcore.blockchain.TokenTransferPreloader
import com.example.walletcore.blockchain.tron.manager.TronSigningContextBuilder
import com.example.walletcore.blockchain.tron.services.TronBroadcastService
import com.example.walletcore.blockchain.tron.signer.TronTransactionSigner
import com.example.walletcore.extension.available
import com.example.walletcore.extension.toChainType
import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.ChainType
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class BlockchainModule {
    @Provides
    @Singleton
    fun provideBroadcastClientDispatcher(
        rpcClients: BlockchainRpcManager,
    ): BroadcastClientDispatcher = BroadcastClientDispatcher(
        Chain.available().map {
            when (it.toChainType()) {
                ChainType.Tron -> TronBroadcastService(it, rpcClients.getRpcClient(Chain.Tron))
            }
        },
    )

    @Provides
    @Singleton
    fun provideSignerClientDispatcher(
        rpcClients: BlockchainRpcManager,
    ): SignerClientDispatcher {
        val dispatchers = Chain.available().map {
            when (it.toChainType()) {
                ChainType.Tron -> TronSigningContextBuilder(
                    it,
                    rpcClients.getRpcClient(it),
                    rpcClients.getRpcClient(it),
                    rpcClients.getRpcClient(it)
                )
            }
        }
        return SignerClientDispatcher(
            nativeTransferClients = dispatchers,
            tokenTransferClients = dispatchers.mapNotNull { it as? TokenTransferPreloader },
            genericPreloaderClients = dispatchers.mapNotNull { it as? GenericTransferPreloader },
        )
    }

    @Provides
    @Singleton
    fun provideBlockchainSignManager(): BlockchainSignManager = BlockchainSignManager(
        blockchains = Chain.available().map {
            when (it.toChainType()) {
                ChainType.Tron -> TronTransactionSigner(it)
            }
        },
    )
}