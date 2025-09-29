package com.example.telegramWallet.data.database.modules

import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.ProfileRepoImpl
import com.example.telegramWallet.data.database.repositories.SettingsRepo
import com.example.telegramWallet.data.database.repositories.SettingsRepoImpl
import com.example.telegramWallet.data.database.repositories.StatesRepo
import com.example.telegramWallet.data.database.repositories.StatesRepoImpl
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingAmlTransactionRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.SmartContractRepo
import com.example.telegramWallet.data.database.repositories.wallet.SmartContractRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.TradingInsightsRepo
import com.example.telegramWallet.data.database.repositories.wallet.TradingInsightsRepoImpl
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.WalletProfileRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {
    @Binds
    abstract fun bindAddressRepo(addressRepoImpl: AddressRepoImpl): AddressRepo

    @Binds
    abstract fun bindTokenRepo(tokenRepoImpl: TokenRepoImpl): TokenRepo

    @Binds
    abstract fun bindWalletProfileRepo(walletProfileRepoImpl: WalletProfileRepoImpl): WalletProfileRepo

    @Binds
    abstract fun bindProfileRepo(profileRepoImpl: ProfileRepoImpl): ProfileRepo

    @Binds
    abstract fun bindSettingsRepo(settingsRepoImpl: SettingsRepoImpl): SettingsRepo

    @Binds
    abstract fun bindStatesRepo(statesRepoImpl: StatesRepoImpl): StatesRepo

    @Binds
    abstract fun bindTransactionsRepo(transactionsRepoImpl: TransactionsRepoImpl): TransactionsRepo

    @Binds
    abstract fun bindCentralAddressRepo(centralAddressRepoImpl: CentralAddressRepoImpl): CentralAddressRepo

    @Binds
    abstract fun bindSmartContractRepo(smartContractRepoImpl: SmartContractRepoImpl): SmartContractRepo

    @Binds
    abstract fun bindExchangeRatesRepo(exchangeRatesRepoImpl: ExchangeRatesRepoImpl): ExchangeRatesRepo

    @Binds
    abstract fun bindTradingInsightsRepo(tradingInsightsRepoIml: TradingInsightsRepoImpl): TradingInsightsRepo

    @Binds
    abstract fun bindPendingTransactionRepo(pendingTransactionRepoImpl: PendingTransactionRepoImpl): PendingTransactionRepo

    @Binds
    abstract fun bindPendingAmlTransactionRepo(pendingAmlTransactionRepoImpl: PendingAmlTransactionRepoImpl): PendingAmlTransactionRepo
}
