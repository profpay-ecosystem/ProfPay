package com.profpay.wallet.data.di.module

import com.profpay.wallet.data.di.token.SharedPrefsTokenProvider
import com.profpay.wallet.data.di.token.TokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenDaggerModule {
    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: SharedPrefsTokenProvider): TokenProvider
}
