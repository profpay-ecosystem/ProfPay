package com.profpay.wallet.data.database.modules

import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.ProfileRepoImpl
import com.profpay.wallet.data.database.repositories.SettingsRepo
import com.profpay.wallet.data.database.repositories.SettingsRepoImpl
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.TransactionsRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.profpay.wallet.data.database.repositories.wallet.ExchangeRatesRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingAmlTransactionRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.SmartContractRepo
import com.profpay.wallet.data.database.repositories.wallet.SmartContractRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.TradingInsightsRepo
import com.profpay.wallet.data.database.repositories.wallet.TradingInsightsRepoImpl
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
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
