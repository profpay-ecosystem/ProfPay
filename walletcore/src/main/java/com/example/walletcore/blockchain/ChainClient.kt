package com.example.walletcore.blockchain

import com.example.walletcore.primitives.Chain

interface ChainClient {
    fun supported(chain: Chain): Boolean
}

fun <T : ChainClient> List<T>.getClient(chain: Chain): T? =
    firstOrNull { it.supported(chain) }