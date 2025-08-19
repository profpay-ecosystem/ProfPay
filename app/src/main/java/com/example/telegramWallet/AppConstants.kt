package com.example.telegramWallet

object AppConstants {
    object Network {
        const val GRPC_ENDPOINT = "grpc.wallet-services-srv.com"
        const val GRPC_PORT = 8443
    }

    object SmartContract {
        const val PUBLISH_ENERGY_REQUIRED = 2_401_828L
        const val PUBLISH_BANDWIDTH_REQUIRED = 14_600L
    }
}