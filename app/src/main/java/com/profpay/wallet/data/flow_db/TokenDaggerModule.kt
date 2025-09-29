package com.profpay.wallet.data.flow_db

import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import com.profpay.wallet.data.flow_db.token.TokenProvider
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
