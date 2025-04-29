package com.example.telegramWallet.data.database.modules

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.telegramWallet.data.database.AppDatabase
import com.example.telegramWallet.data.database.dao.ProfileDao
import com.example.telegramWallet.data.database.dao.SettingsDao
import com.example.telegramWallet.data.database.dao.StatesDao
import com.example.telegramWallet.data.database.dao.TransactionsDao
import com.example.telegramWallet.data.database.dao.wallet.AddressDao
import com.example.telegramWallet.data.database.dao.wallet.CentralAddressDao
import com.example.telegramWallet.data.database.dao.wallet.ExchangeRatesDao
import com.example.telegramWallet.data.database.dao.wallet.SmartContractDao
import com.example.telegramWallet.data.database.dao.wallet.TokenDao
import com.example.telegramWallet.data.database.dao.wallet.TradingInsightsDao
import com.example.telegramWallet.data.database.dao.wallet.WalletProfileDao
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
    fun provideAddressDao(appDatabase: AppDatabase): AddressDao {
        return appDatabase.getAddressDao()
    }
    @Provides
    fun provideTokenDao(appDatabase: AppDatabase): TokenDao {
        return appDatabase.getTokenDao()
    }

    @Provides
    fun provideWalletProfileDao(appDatabase: AppDatabase): WalletProfileDao {
        return appDatabase.getWalletProfileDao()
    }

    @Provides
    fun provideSettingsDao(appDatabase: AppDatabase): SettingsDao {
        return appDatabase.getSettingsDao()
    }

    @Provides
    fun provideProfileDao(appDatabase: AppDatabase): ProfileDao {
        return appDatabase.getProfileDao()
    }

    @Provides
    fun provideStatesDao(appDatabase: AppDatabase): StatesDao {
        return appDatabase.getStatesDao()
    }

    @Provides
    fun provideTransactionsDao(appDatabase: AppDatabase): TransactionsDao {
        return appDatabase.getTransactionsDao()
    }

    @Provides
    fun provideCentralAddressDao(appDatabase: AppDatabase): CentralAddressDao {
        return appDatabase.getCentralAddressDao()
    }

    @Provides
    fun proviceSmartContractDao(appDatabase: AppDatabase): SmartContractDao {
        return appDatabase.getSmartContractDao()
    }

    @Provides
    fun provideExchangeRatesDao(appDatabase: AppDatabase): ExchangeRatesDao {
        return appDatabase.getExchangeRatesDao()
    }

    @Provides
    fun provideTradingInsightsDao(appDatabase: AppDatabase): TradingInsightsDao {
        return appDatabase.getTradingInsightsDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        // Определите миграцию из версии 1 в версию 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание новой таблицы new_transactions
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `new_transactions` (" +
                            "`transaction_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "`sender_address_id` INTEGER, " +
                            "`sender_address` TEXT NOT NULL, " +
                            "`wallet_id` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`token_id` INTEGER NOT NULL, " +
                            "`receiver_address_id` INTEGER, " +
                            "`receiver_address` TEXT NOT NULL, " +
                            "`tx_id` TEXT NOT NULL UNIQUE, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`status` TEXT NOT NULL, " +
                            "FOREIGN KEY(`receiver_address_id`) REFERENCES `addresses`(`address_id`) ON DELETE CASCADE, " +
                            "FOREIGN KEY(`sender_address_id`) REFERENCES `addresses`(`address_id`) ON DELETE CASCADE " +
                            ")"
                )

                // Создание индексов для новой таблицы new_transactions
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_token_id` ON `new_transactions` (`token_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_receiver_address_id` ON `new_transactions` (`receiver_address_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_sender_address_id` ON `new_transactions` (`sender_address_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_tx_id` ON `new_transactions` (`tx_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_timestamp` ON `new_transactions` (`timestamp`)")

                // Удаление старой таблицы transactions
                db.execSQL("DROP TABLE IF EXISTS `transactions`")

                // Переименование новой таблицы new_transactions в transactions
                db.execSQL("ALTER TABLE `new_transactions` RENAME TO `transactions`")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание новой таблицы new_transactions
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `new_transactions` (" +
                            "`transaction_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "`sender_address_id` INTEGER, " +
                            "`sender_address` TEXT NOT NULL, " +
                            "`wallet_id` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`token_name` TEXT NOT NULL, " +
                            "`receiver_address_id` INTEGER, " +
                            "`receiver_address` TEXT NOT NULL, " +
                            "`tx_id` TEXT NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`status` TEXT NOT NULL, " +
                            "FOREIGN KEY(`receiver_address_id`) REFERENCES `addresses`(`address_id`) ON DELETE CASCADE, " +
                            "FOREIGN KEY(`sender_address_id`) REFERENCES `addresses`(`address_id`) ON DELETE CASCADE " +
                            ")"
                )

                // Создание индексов для новой таблицы new_transactions
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_receiver_address_id` ON `new_transactions` (`receiver_address_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_sender_address_id` ON `new_transactions` (`sender_address_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_new_tx_id` ON `new_transactions` (`tx_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_timestamp` ON `new_transactions` (`timestamp`)")

                // Удаление старой таблицы transactions
                db.execSQL("DROP TABLE IF EXISTS `transactions`")

                // Переименование новой таблицы new_transactions в transactions
                db.execSQL("ALTER TABLE `new_transactions` RENAME TO `transactions`")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание новой таблицы new_transactions
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `new_transactions` (" +
                            "`transaction_id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "`sender_address_id` INTEGER, " +
                            "`sender_address` TEXT NOT NULL, " +
                            "`wallet_id` INTEGER NOT NULL, " +
                            "`amount` INTEGER NOT NULL, " +
                            "`token_name` TEXT NOT NULL, " +
                            "`receiver_address_id` INTEGER, " +
                            "`receiver_address` TEXT NOT NULL, " +
                            "`tx_id` TEXT NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL, " +
                            "`status` TEXT NOT NULL, " +
                            "FOREIGN KEY(`receiver_address_id`) REFERENCES `addresses`(`address_id`) ON DELETE CASCADE, " +
                            "FOREIGN KEY(`sender_address_id`) REFERENCES `addresses`(`address_id`) ON DELETE CASCADE " +
                            ")"
                )

                // Создание индексов для новой таблицы new_transactions
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_receiver_address_id` ON `new_transactions` (`receiver_address_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_sender_address_id` ON `new_transactions` (`sender_address_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_tx_id` ON `new_transactions` (`tx_id`,`sender_address`,`receiver_address`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_timestamp` ON `new_transactions` (`timestamp`)")

                // Удаление старой таблицы transactions
                db.execSQL("DROP TABLE IF EXISTS `transactions`")

                // Переименование новой таблицы new_transactions в transactions
                db.execSQL("ALTER TABLE `new_transactions` RENAME TO `transactions`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN is_processed INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN server_response_received INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_transactions_tx_id`");
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_tx_id` ON `transactions` (`tx_id`, `sender_address`, `receiver_address`, `wallet_id`)");
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание таблицы
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS central_address (
                        central_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        address TEXT NOT NULL,
                        public_key TEXT NOT NULL, 
                        private_key TEXT NOT NULL, 
                        trx_balance INTEGER NOT NULL, 
                        UNIQUE(address), 
                        UNIQUE(public_key), 
                        UNIQUE(private_key)
                    )
                """)

                // Создание индексов
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS central_address_ind_address ON central_address(address)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS central_address_ind_public_key ON central_address(public_key)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS central_address_ind_private_key ON central_address(private_key)"
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tokens ADD COLUMN frozen_balance INTEGER DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Удаление старого индекса, если он существует
                db.execSQL("DROP INDEX IF EXISTS `index_transactions_tx_id`")

                // Создание нового уникального индекса на столбцы tx_id, wallet_id
                db.execSQL("CREATE UNIQUE INDEX `index_transactions_tx_id_wallet_id` ON `transactions` (`tx_id`, `wallet_id`)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN is_send INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM transactions")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM transactions")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM transactions")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions RENAME COLUMN is_send TO type ")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE transactions SET is_processed = 0, server_response_received = 0")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE tokens SET frozen_balance = 0")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE tokens SET frozen_balance = 0")
                db.execSQL("UPDATE transactions SET is_processed = 1, server_response_received = 1")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE transactions SET is_processed = 0, server_response_received = 0 WHERE tx_id = '4e9884d100687858fedbb838a35deac2a3989cabfbacb64f80381afe0e4df4a6'")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE tokens SET frozen_balance = 0")
                db.execSQL("UPDATE transactions SET is_processed = 0, server_response_received = 0 WHERE tx_id = '4e9884d100687858fedbb838a35deac2a3989cabfbacb64f80381afe0e4df4a6'")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание таблицы
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS smart_contracts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT, 
                        contract_address TEXT NOT NULL,
                        owner_address TEXT NOT NULL,
                        UNIQUE(contract_address)
                    )
                """)

                // Создание индексов
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS smart_contracts_ind_contract_address ON smart_contracts(contract_address)"
                )
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE smart_contracts ADD COLUMN open_deals_count INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE smart_contracts ADD COLUMN closed_deals_count INTEGER DEFAULT 0")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание таблицы
                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS exchange_rates (
                            id INTEGER PRIMARY KEY AUTOINCREMENT, 
                            symbol TEXT NOT NULL,
                            value REAL NOT NULL,
                            UNIQUE(symbol)
                        )
                        """
                )

                // Создание индекса на столбец symbol
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS exchange_data_index_symbol ON exchange_rates(symbol)"
                )
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создание таблицы
                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS trading_insights (
                            id INTEGER PRIMARY KEY AUTOINCREMENT, 
                            symbol TEXT NOT NULL,
                            price_change_percentage_24h REAL NOT NULL,
                            UNIQUE(symbol)
                        )
                        """
                )

                // Создание индекса на столбец symbol
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS trading_insights_index_symbol ON trading_insights(symbol)"
                )
            }
        }

        val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE central_address ADD COLUMN balance TEXT NOT NULL DEFAULT '0'")
                db.execSQL("UPDATE central_address SET balance = CAST(trx_balance AS TEXT)")

                db.execSQL("ALTER TABLE tokens ADD COLUMN new_balance TEXT NOT NULL DEFAULT '0'")
                db.execSQL("ALTER TABLE tokens ADD COLUMN new_frozen_balance TEXT DEFAULT '0'")
                db.execSQL("UPDATE tokens SET new_balance = CAST(balance AS TEXT)")
                db.execSQL("UPDATE tokens SET new_frozen_balance = CAST(frozen_balance AS TEXT)")

                db.execSQL("ALTER TABLE transactions ADD COLUMN new_amount TEXT NOT NULL DEFAULT '0'")
                db.execSQL("UPDATE transactions SET new_amount = CAST(amount AS TEXT)")
            }
        }

        val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profile ADD COLUMN device_token TEXT")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_profile_device_token ON profile(device_token)")
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE wallet_profile ADD COLUMN entropy BLOB DEFAULT NULL")
            }
        }

        return Room.databaseBuilder(
            appContext, AppDatabase::class.java,
            "room_crypto_wallet.db"
        )
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addMigrations(MIGRATION_7_8)
            .addMigrations(MIGRATION_8_9)
            .addMigrations(MIGRATION_9_10)
            .addMigrations(MIGRATION_10_11)
            .addMigrations(MIGRATION_11_12)
            .addMigrations(MIGRATION_12_13)
            .addMigrations(MIGRATION_13_14)
            .addMigrations(MIGRATION_14_15)
            .addMigrations(MIGRATION_15_16)
            .addMigrations(MIGRATION_16_17)
            .addMigrations(MIGRATION_17_18)
            .addMigrations(MIGRATION_18_19)
            .addMigrations(MIGRATION_19_20)
            .addMigrations(MIGRATION_20_21)
            .addMigrations(MIGRATION_21_22)
            .addMigrations(MIGRATION_22_23)
            .addMigrations(MIGRATION_23_24)
            .addMigrations(MIGRATION_24_25)
            .addMigrations(MIGRATION_26_27)
            .addMigrations(MIGRATION_27_28)
            .build()
    }
}

