package com.example.walletcore.model

data class NodeStatusModel(
    val url: String,
    val chainId: String,
    val blockNumber: String,
    val inSync: Boolean,
    val latency: Long,
    val loading: Boolean = false,
)