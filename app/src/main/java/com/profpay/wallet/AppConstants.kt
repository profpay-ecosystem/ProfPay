package com.profpay.wallet

object AppConstants {
    object Network {
        const val GRPC_ENDPOINT = "grpc.wallet-services-srv.com"
        const val GRPC_PORT = 30443

        const val TRON_GRPC_ENDPOINT = "tron01.wallet-services-srv.com:59151"
        const val TRON_GRPC_ENDPOINT_SOLIDITY = "tron01.wallet-services-srv.com:50061"
    }

    object SmartContract {
        const val PUBLISH_ENERGY_REQUIRED = 2_401_828L
        const val PUBLISH_BANDWIDTH_REQUIRED = 14_600L
    }

    object Application {
        const val HASH_ALGORITHM = "SHA-256"
    }
}
