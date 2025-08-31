package com.example.walletcore.extension

import com.example.walletcore.primitives.Chain
import com.example.walletcore.primitives.ChainType

fun Chain.toChainType(): ChainType {
    return when (this) {
        Chain.Tron -> ChainType.Tron
    }
}

fun Chain.Companion.exclude() = setOf(Chain.Tron)

fun Chain.Companion.available() = (Chain.entries.toSet() - exclude())