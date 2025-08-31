package com.example.walletcore

import com.example.walletcore.primitives.Chain

class BlockchainRpcManager {
    private val clientRegistry = mutableMapOf<Chain, Any>()

    /**
     * Регистрируем клиента для указанной блокчейн-сети.
     */
    fun registerClient(chain: Chain, rpcClient: Any): BlockchainRpcManager {
        clientRegistry[chain] = rpcClient
        return this
    }

    /**
     * Возвращаем зарегистрированного клиента для указанной блокчейн-сети.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getRpcClient(chain: Chain): T {
        return clientRegistry[chain] as T
    }

    /**
     * Проверяем, зарегистрирован ли клиент для сети.
     */
    fun hasClient(chain: Chain): Boolean {
        return chain in clientRegistry
    }
}