package com.profpay.wallet.data.di.module

import android.content.Context
import com.profpay.wallet.tron.Tron
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
