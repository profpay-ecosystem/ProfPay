package com.profpay.wallet.data.database.modules

import com.profpay.wallet.data.database.AppDatabase
import com.profpay.wallet.data.database.dao.ProfileDao
import com.profpay.wallet.data.database.dao.SettingsDao
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
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides fun provideAddressDao(db: AppDatabase): AddressDao = db.getAddressDao()

    @Provides fun provideTokenDao(db: AppDatabase): TokenDao = db.getTokenDao()

    @Provides fun provideWalletProfileDao(db: AppDatabase): WalletProfileDao = db.getWalletProfileDao()

    @Provides fun provideSettingsDao(db: AppDatabase): SettingsDao = db.getSettingsDao()

    @Provides fun provideProfileDao(db: AppDatabase): ProfileDao = db.getProfileDao()

    @Provides fun provideTransactionsDao(db: AppDatabase): TransactionsDao = db.getTransactionsDao()

    @Provides fun provideCentralAddressDao(db: AppDatabase): CentralAddressDao = db.getCentralAddressDao()

    @Provides fun provideSmartContractDao(db: AppDatabase): SmartContractDao = db.getSmartContractDao()

    @Provides fun provideExchangeRatesDao(db: AppDatabase): ExchangeRatesDao = db.getExchangeRatesDao()

    @Provides fun provideTradingInsightsDao(db: AppDatabase): TradingInsightsDao = db.getTradingInsightsDao()

    @Provides fun providePendingTransactionDao(db: AppDatabase): PendingTransactionDao = db.getPendingTransactionDao()

    @Provides fun providePendingAmlTransactionDao(db: AppDatabase): PendingAmlTransactionDao = db.getPendingAmlTransactionDao()
}
