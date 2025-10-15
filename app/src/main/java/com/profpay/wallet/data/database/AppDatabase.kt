package com.profpay.wallet.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.profpay.wallet.bridge.viewmodel.dto.BlockchainName
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
import com.profpay.wallet.data.database.entities.ProfileEntity
import com.profpay.wallet.data.database.entities.SettingsEntity
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
import java.math.BigInteger

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

    abstract fun getTransactionsDao(): TransactionsDao

    abstract fun getCentralAddressDao(): CentralAddressDao

    abstract fun getSmartContractDao(): SmartContractDao

    abstract fun getExchangeRatesDao(): ExchangeRatesDao

    abstract fun getTradingInsightsDao(): TradingInsightsDao

    abstract fun getPendingTransactionDao(): PendingTransactionDao

    abstract fun getPendingAmlTransactionDao(): PendingAmlTransactionDao

    @Transaction
    open suspend fun insertWalletWithAddressesAndTokens(
        walletProfile: WalletProfileEntity,
        addresses: List<AddressEntity>,
    ) {
        val number = getWalletProfileDao().getCountRecords() + 1
        val entityWithName = walletProfile.copy(name = "Wallet $number")

        val walletId = getWalletProfileDao().insert(entityWithName)

        val defaultTokens =
            BlockchainName.entries
                .flatMap { blockchain -> blockchain.tokens }
                .map { it.tokenName }

        addresses.forEach { address ->
            val addressId = getAddressDao().insert(address.copy(walletId = walletId))
            val tokenEntities = defaultTokens.map { TokenEntity(addressId = addressId, tokenName = it, balance = BigInteger.ZERO) }
            getTokenDao().insertAll(tokenEntities)
        }
    }
}
