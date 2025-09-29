package com.profpay.wallet.backend.grpc

import com.profpay.wallet.backend.grpc.interceptor.JwtAuthInterceptor
import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrpcClientFactory
    @Inject
    constructor(
        private val token: SharedPrefsTokenProvider,
    ) {
        private val channels = mutableMapOf<String, ManagedChannel>()
        private val clients = mutableMapOf<String, Any>()

        private fun getOrCreateChannel(
            host: String,
            port: Int,
        ): ManagedChannel =
            channels.getOrPut("$host:$port") {
                ManagedChannelBuilder
                    .forAddress(host, port)
                    .useTransportSecurity()
                    .intercept(JwtAuthInterceptor(token))
                    .build()
            }

        @Suppress("UNCHECKED_CAST")
        fun <T> getGrpcClient(
            clientClass: Class<T>,
            host: String,
            port: Int,
        ): T {
            val key = "${clientClass.simpleName}@$host:$port"

            return clients.getOrPut(key) {
                val channel = getOrCreateChannel(host, port)
                when (clientClass) {
                    TransferGrpcClient::class.java -> TransferGrpcClient(channel, token)
                    AmlGrpcClient::class.java -> AmlGrpcClient(channel, token)
                    CryptoAddressGrpcClient::class.java -> CryptoAddressGrpcClient(channel, token)
                    SmartContractGrpcClient::class.java -> SmartContractGrpcClient(channel, token)
                    UserGrpcClient::class.java -> UserGrpcClient(channel, token)
                    ProfPayServerGrpcClient::class.java -> ProfPayServerGrpcClient(channel, token)
                    AuthGrpcClient::class.java -> AuthGrpcClient(channel, token)
                    else -> throw IllegalArgumentException("Неизвестный gRPC-клиент: ${clientClass.simpleName}")
                }
            } as T
        }

        fun shutdownAll() {
            clients.clear()
            channels.values.forEach { it.shutdown() }
            channels.clear()
        }
    }
