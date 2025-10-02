package com.profpay.wallet.data.flow_db.module

import android.content.Context
import com.profpay.wallet.security.KeystoreCryptoManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    fun provideKeyStoreHelper(@ApplicationContext context: Context): KeystoreCryptoManager {
        return KeystoreCryptoManager(context)
    }
}