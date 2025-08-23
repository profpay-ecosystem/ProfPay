package com.example.walletcore.blockchain.tron

import com.example.walletcore.blockchain.IChainClient
import com.example.walletcore.model.NodeStatus
import com.example.walletcore.primitives.Chain

interface INodeStatus : IChainClient {
    suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus?
}