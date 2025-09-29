package com.example.telegramWallet.data.flow_db

import android.content.Context
import com.example.telegramWallet.tron.Tron
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TronDaggerModule {
    @Provides
    @Singleton
    fun provideTron(
        @ApplicationContext context: Context,
    ): Tron = Tron(context)
}
