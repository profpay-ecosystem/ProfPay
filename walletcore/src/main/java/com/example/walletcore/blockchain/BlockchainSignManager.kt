package com.example.walletcore.blockchain

import com.example.walletcore.primitives.Chain

class BlockchainSignManager(private val blockchains: List<BlockchainSigner>) {
    fun supported(chain: Chain): Boolean {
        return blockchains.getClient(chain) != null
    }
}