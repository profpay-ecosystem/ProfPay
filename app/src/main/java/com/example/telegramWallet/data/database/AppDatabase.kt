package com.example.telegramWallet.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.example.telegramWallet.data.database.dao.ProfileDao
import com.example.telegramWallet.data.database.dao.SettingsDao
import com.example.telegramWallet.data.database.dao.StatesDao
import com.example.telegramWallet.data.database.dao.TransactionsDao
import com.example.telegramWallet.data.database.dao.wallet.AddressDao
import com.example.telegramWallet.data.database.dao.wallet.CentralAddressDao
import com.example.telegramWallet.data.database.dao.wallet.ExchangeRatesDao
import com.example.telegramWallet.data.database.dao.wallet.PendingAmlTransactionDao
import com.example.telegramWallet.data.database.dao.wallet.PendingTransactionDao
import com.example.telegramWallet.data.database.dao.wallet.SmartContractDao
import com.example.telegramWallet.data.database.dao.wallet.TokenDao
import com.example.telegramWallet.data.database.dao.wallet.TradingInsightsDao
import com.example.telegramWallet.data.database.dao.wallet.WalletProfileDao
import com.example.telegramWallet.data.database.entities.ProfileEntity
import com.example.telegramWallet.data.database.entities.SettingsEntity
import com.example.telegramWallet.data.database.entities.StatesEntity
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.entities.wallet.CentralAddressEntity
import com.example.telegramWallet.data.database.entities.wallet.ExchangeRatesEntity
import com.example.telegramWallet.data.database.entities.wallet.PendingAmlTransactionEntity
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.entities.wallet.SmartContractEntity
import com.example.telegramWallet.data.database.entities.wallet.TokenEntity
import com.example.telegramWallet.data.database.entities.wallet.TradingInsightsEntity
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.entities.wallet.WalletProfileEntity

// Создание Базы Данных
@Database(
    version = 35,
    autoMigrations = [
        AutoMigration(from = 25, to = 26, spec = AppDatabase.AutoMigrationFrom25To26::class),
    ],
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

    @DeleteColumn.Entries(
        DeleteColumn(tableName = "central_address", columnName = "trx_balance"),
        DeleteColumn(tableName = "tokens", columnName = "balance"),
        DeleteColumn(tableName = "tokens", columnName = "frozen_balance"),
        DeleteColumn(tableName = "transactions", columnName = "amount"),
    )
    @RenameColumn.Entries(
        RenameColumn(tableName = "tokens", fromColumnName = "new_balance", toColumnName = "balance"),
        RenameColumn(tableName = "tokens", fromColumnName = "new_frozen_balance", toColumnName = "frozen_balance"),
        RenameColumn(tableName = "transactions", fromColumnName = "new_amount", toColumnName = "amount"),
    )
    class AutoMigrationFrom25To26 : AutoMigrationSpec
}
