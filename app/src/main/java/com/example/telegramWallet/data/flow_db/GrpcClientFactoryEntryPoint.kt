package com.example.telegramWallet.data.flow_db

import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GrpcClientFactoryEntryPoint {
    val grpcClientFactory: GrpcClientFactory
}
