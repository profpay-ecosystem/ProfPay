package com.example.telegramWallet.data.flow_db

import com.example.telegramWallet.data.flow_db.token.SharedPrefsTokenProvider
import com.example.telegramWallet.data.flow_db.token.TokenProvider
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
