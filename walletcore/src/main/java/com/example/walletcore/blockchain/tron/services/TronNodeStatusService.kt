package com.example.walletcore.blockchain.tron.services

import com.example.walletcore.blockchain.tron.client.NodeStatus
import com.example.walletcore.blockchain.tron.client.TronRpcClient
import com.example.walletcore.model.NodeStatusModel
import com.example.walletcore.primitives.Chain
import com.example.walletcore.rpc.getLatency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TronNodeStatusService(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : NodeStatus {
    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatusModel? =
        withContext(Dispatchers.IO) {
            val resp = rpcClient.nowBlock("$url/wallet/getnowblock")
            NodeStatusModel(
                url = url,
                blockNumber = resp.body()?.blockHeader?.rawData?.number?.toString()
                    ?: return@withContext null,
                inSync = true,
                chainId = "",
                latency = resp.getLatency(),
            )
        }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}