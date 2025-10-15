package com.profpay.wallet.data.di.module

import com.profpay.wallet.backend.grpc.GrpcClientFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface GrpcClientFactoryEntryPoint {
    val grpcClientFactory: GrpcClientFactory
}
