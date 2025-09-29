package com.profpay.wallet.data.database.modules

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.profpay.wallet.data.database.AppDatabase
import com.profpay.wallet.data.database.dao.ProfileDao
import com.profpay.wallet.data.database.dao.SettingsDao
import com.profpay.wallet.data.database.dao.StatesDao
import com.profpay.wallet.data.database.dao.TransactionsDao
import com.profpay.wallet.data.database.dao.wallet.AddressDao
import com.profpay.wallet.data.database.dao.wallet.CentralAddressDao
import com.profpay.wallet.data.database.dao.wallet.ExchangeRatesDao
import com.profpay.wallet.data.database.dao.wallet.PendingAmlTransactionDao
import com.profpay.wallet.data.database.dao.wallet.PendingTransactionDao
import com.profpay.wallet.data.database.dao.wallet.SmartContractDao
import com.profpay.wallet.data.database.dao.wallet.TokenDao
import com.profpay.wallet.data.database.dao.wallet.TradingInsightsDao
import com.profpay.wallet.data.database.dao.wallet.WalletProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppDatabaseModule {
    @Provides
    fun provideAddressDao(appDatabase: AppDatabase): AddressDao = appDatabase.getAddressDao()

    @Provides
    fun provideTokenDao(appDatabase: AppDatabase): TokenDao = appDatabase.getTokenDao()

    @Provides
    fun provideWalletProfileDao(appDatabase: AppDatabase): WalletProfileDao = appDatabase.getWalletProfileDao()

    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao = appDatabase.getSettingsDao()

    @Provides
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao = appDatabase.getProfileDao()

    @Provides
    fun provideStatesDao(appDatabase: AppDatabase): StatesDao = appDatabase.getStatesDao()

    @Provides
    fun provideTransactionsDao(appDatabase: AppDatabase): TransactionsDao = appDatabase.getTransactionsDao()

    @Provides
    fun provideCentralAddressDao(appDatabase: AppDatabase): CentralAddressDao = appDatabase.getCentralAddressDao()

    @Provides
    fun proviceSmartContractDao(appDatabase: AppDatabase): SmartContractDao = appDatabase.getSmartContractDao()

    @Provides
    fun provideExchangeRatesDao(appDatabase: AppDatabase): ExchangeRatesDao = appDatabase.getExchangeRatesDao()

    @Provides
    fun provideTradingInsightsDao(appDatabase: AppDatabase): TradingInsightsDao = appDatabase.getTradingInsightsDao()

    @Provides
    fun providePendingTransactionDao(appDatabase: AppDatabase): PendingTransactionDao = appDatabase.getPendingTransactionDao()

    @Provides
    fun providePendingAmlTransactionDao(appDatabase: AppDatabase): PendingAmlTransactionDao = appDatabase.getPendingAmlTransactionDao()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
    ): AppDatabase {
        return Room
            .databaseBuilder(
                appContext,
                AppDatabase::class.java,
                "room_crypto_wallet.db",
            )
            .build()
    }
}
