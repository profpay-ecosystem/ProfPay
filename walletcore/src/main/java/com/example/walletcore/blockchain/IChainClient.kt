package com.example.walletcore.blockchain

import com.example.walletcore.primitives.Chain

interface IChainClient {
    fun supported(chain: Chain): Boolean
}

fun <T : IChainClient> List<T>.getClient(chain: Chain): T? =
    firstOrNull { it.supported(chain) }