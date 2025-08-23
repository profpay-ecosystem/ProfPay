package com.example.walletcore.extension

import com.example.walletcore.primitives.AssetId
import com.example.walletcore.primitives.AssetSubtype
import com.example.walletcore.primitives.Chain

fun AssetId.toIdentifier() = "${chain.string}${if (tokenId.isNullOrEmpty()) "" else "_${tokenId}"}"

fun AssetId.type() = if (tokenId.isNullOrEmpty()) AssetSubtype.NATIVE else AssetSubtype.TOKEN

fun String.toAssetId(): AssetId? {
    val components = split("_", limit = 2)
    val chainId = components.firstOrNull() ?: return null
    val chain = Chain.entries.firstOrNull { it.string == chainId } ?: return null
    val token = if (components.size > 1) components[1] else null
    return AssetId(chain, token)
}