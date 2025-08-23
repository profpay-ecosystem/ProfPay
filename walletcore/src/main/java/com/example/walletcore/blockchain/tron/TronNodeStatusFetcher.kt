package com.example.walletcore.blockchain.tron

import com.example.walletcore.model.NodeStatus
import com.example.walletcore.primitives.Chain
import com.example.walletcore.rpc.getLatency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TronNodeStatusFetcher(
    private val chain: Chain,
    private val rpcClient: ITronRpcClient,
) : INodeStatus {
    override suspend fun getNodeStatus(chain: Chain, url: String): NodeStatus? = withContext(Dispatchers.IO) {
        val resp = rpcClient.nowBlock("$url/wallet/getnowblock")
        NodeStatus(
            url = url,
            blockNumber = resp.body()?.block_header?.raw_data?.number?.toString() ?: return@withContext null,
            inSync = true,
            chainId = "",
            latency = resp.getLatency(),
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}