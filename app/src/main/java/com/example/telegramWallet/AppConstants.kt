package com.example.telegramWallet

object AppConstants {
    object Network {
        const val GRPC_ENDPOINT = "grpc.wallet-services-srv.com"
        const val GRPC_PORT = 8443

        const val TRON_GRPC_ENDPOINT = "45.137.213.192:59151"
        const val TRON_GRPC_ENDPOINT_SOLIDITY = "45.137.213.192:50061"
    }

    object SmartContract {
        const val PUBLISH_ENERGY_REQUIRED = 2_401_828L
        const val PUBLISH_BANDWIDTH_REQUIRED = 14_600L
    }
}