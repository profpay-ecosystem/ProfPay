package com.profpay.wallet.tron

import com.profpay.wallet.AppConstants

data class TronNode(val grpc: String, val solidityGrpc: String)

object TronNodeManager {
    private val nodes = listOf(
        TronNode(AppConstants.Network.TRON_GRPC_ENDPOINT, AppConstants.Network.TRON_GRPC_ENDPOINT_SOLIDITY),
        TronNode("grpc.trongrid.io:50051", "grpc.trongrid.io:50052"),
        TronNode("tron-grpc.publicnode.com:443", "tron-solidity-grpc.publicnode.com:443"),
        TronNode("3.225.171.164:50051", "3.225.171.164:50061"),
        TronNode("18.133.82.227:50051", "18.133.82.227:50061"),
        TronNode("15.207.144.3:50051", "15.207.144.3:50061"),
        TronNode("15.222.19.181:50051", "15.222.19.181:50061"),
        TronNode("18.209.42.127:50051", "18.209.42.127:50061"),
        TronNode("3.218.137.187:50051", "3.218.137.187:50061"),
        TronNode("34.237.210.82:50051", "34.237.210.82:50061"),
        TronNode("13.228.119.63:50051", "13.228.119.63:50061"),
        TronNode("18.139.193.235:50051", "18.139.193.235:50061"),
        TronNode("18.141.79.38:50051", "18.141.79.38:50061"),
        TronNode("18.139.248.26:50051", "18.139.248.26:50061"),
        TronNode("52.8.46.215:50051", "52.8.46.215:50061"),
        TronNode("3.12.212.122:50051", "3.12.212.122:50061"),
        TronNode("52.24.128.7:50051", "52.24.128.7:50061"),
        TronNode("3.39.38.55:50051", "3.39.38.55:50061"),
        TronNode("3.79.71.167:50051", "3.79.71.167:50061"),
        TronNode("108.128.110.16:50051", "108.128.110.16:50061"),
        TronNode("35.180.81.133:50051", "35.180.81.133:50061"),
        TronNode("13.210.151.5:50051", "13.210.151.5:50061"),
    )
    // Индекс текущей ноды
    @Volatile
    private var currentIndex = 0

    // 10 RPS per node
    private const val MAX_RPS = 10

    private var lastSecond = 0L
    private var requestsInCurrentSecond = 0

    @Synchronized
    private fun rateLimit() {
        // Отключаем rateLimit для первой ноды
        if (currentIndex == 0) return

        val now = System.currentTimeMillis() / 1000

        if (now != lastSecond) {
            lastSecond = now
            requestsInCurrentSecond = 0
        }

        if (requestsInCurrentSecond >= MAX_RPS) {
            val sleepMs = 1000 - (System.currentTimeMillis() % 1000)
            Thread.sleep(sleepMs)
        }

        requestsInCurrentSecond++
    }

    fun getCurrentNode(): TronNode = nodes[currentIndex]

    @Synchronized
    private fun switchNode(): TronNode {
        currentIndex = (currentIndex + 1) % nodes.size
        return nodes[currentIndex]
    }

    fun <T> executeWithFailover(block: (TronNode) -> T): T {
        val startIndex = currentIndex
        do {
            val node = getCurrentNode()
            try {
                rateLimit()
                return block(node)
            } catch (e: Exception) {
                println("Ошибка при работе с нодой ${node.grpc}: ${e.message}, пробуем следующую...")
                switchNode()
            }
        } while (currentIndex != startIndex)

        throw RuntimeException("Все ноды недоступны!")
    }

    suspend fun <T> executeWithFailoverSuspend(block: suspend (TronNode) -> T): T {
        val startIndex = currentIndex
        do {
            val node = getCurrentNode()
            try {
                return block(node)
            } catch (e: Exception) {
                println("Ошибка при работе с нодой ${node.grpc}: ${e.message}, пробуем следующую...")
                switchNode()
            }
        } while (currentIndex != startIndex)

        throw RuntimeException("Все ноды недоступны!")
    }
}
