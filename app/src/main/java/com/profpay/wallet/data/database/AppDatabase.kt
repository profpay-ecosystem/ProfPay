package com.profpay.wallet.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
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
import com.profpay.wallet.data.database.entities.ProfileEntity
import com.profpay.wallet.data.database.entities.SettingsEntity
import com.profpay.wallet.data.database.entities.StatesEntity
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.entities.wallet.ExchangeRatesEntity
import com.profpay.wallet.data.database.entities.wallet.PendingAmlTransactionEntity
import com.profpay.wallet.data.database.entities.wallet.PendingTransactionEntity
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.database.entities.wallet.TradingInsightsEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.WalletProfileEntity

// Создание Базы Данных
@Database(
    version = 1,
    entities = [
        AddressEntity::class,
        TokenEntity::class,
        WalletProfileEntity::class,

        ProfileEntity::class,
        TransactionEntity::class,
        SettingsEntity::class,
        StatesEntity::class,
        CentralAddressEntity::class,
        SmartContractEntity::class,
        ExchangeRatesEntity::class,
        TradingInsightsEntity::class,
        PendingTransactionEntity::class,
        PendingAmlTransactionEntity::class,
    ],
    exportSchema = true,
)
@TypeConverters(DateConverter::class, BigIntegerConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAddressDao(): AddressDao

    abstract fun getTokenDao(): TokenDao

    abstract fun getWalletProfileDao(): WalletProfileDao

    abstract fun getProfileDao(): ProfileDao

    abstract fun getSettingsDao(): SettingsDao

    abstract fun getStatesDao(): StatesDao

    abstract fun getTransactionsDao(): TransactionsDao

    abstract fun getCentralAddressDao(): CentralAddressDao

    abstract fun getSmartContractDao(): SmartContractDao

    abstract fun getExchangeRatesDao(): ExchangeRatesDao

    abstract fun getTradingInsightsDao(): TradingInsightsDao

    abstract fun getPendingTransactionDao(): PendingTransactionDao

    abstract fun getPendingAmlTransactionDao(): PendingAmlTransactionDao
}
