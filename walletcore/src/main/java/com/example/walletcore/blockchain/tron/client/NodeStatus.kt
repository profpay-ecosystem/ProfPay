package com.example.walletcore.blockchain.tron.client

import com.example.walletcore.blockchain.ChainClient
import com.example.walletcore.model.NodeStatusModel
import com.example.walletcore.primitives.Chain

interface NodeStatus : ChainClient {
    suspend fun getNodeStatus(chain: Chain, url: String): NodeStatusModel?
}