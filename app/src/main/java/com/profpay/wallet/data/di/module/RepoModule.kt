package com.profpay.wallet.data.di.module

import com.profpay.wallet.data.repository.SettingsAccountRepo
import com.profpay.wallet.data.repository.SettingsAccountRepoImpl
import com.profpay.wallet.data.repository.WalletAddedRepo
import com.profpay.wallet.data.repository.WalletAddedRepoImpl
import com.profpay.wallet.data.repository.WalletInfoRepo
import com.profpay.wallet.data.repository.WalletInfoRepoImpl
import com.profpay.wallet.data.repository.WalletSotRepo
import com.profpay.wallet.data.repository.WalletSotRepoImpl
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepo
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepoImpl
import com.profpay.wallet.data.repository.flow.BlockingAppRepo
import com.profpay.wallet.data.repository.flow.BlockingAppRepoImpl
import com.profpay.wallet.data.repository.flow.SendFromWalletRepo
import com.profpay.wallet.data.repository.flow.SendFromWalletRepoImpl
import com.profpay.wallet.data.repository.flow.SmartContractRepo
import com.profpay.wallet.data.repository.flow.SmartContractRepoImpl
import com.profpay.wallet.data.repository.flow.TXDetailsRepo
import com.profpay.wallet.data.repository.flow.TXDetailsRepoImpl
import com.profpay.wallet.data.repository.flow.ThemeAppRepo
import com.profpay.wallet.data.repository.flow.ThemeAppRepoImpl
import com.profpay.wallet.data.repository.flow.WalletAddressRepo
import com.profpay.wallet.data.repository.flow.WalletAddressRepoImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds
    @Singleton
    abstract fun bindAddressAndMnemonicRepo(addressAndMnemonicRepoImpl: AddressAndMnemonicRepoImpl): AddressAndMnemonicRepo

    @Binds
    @Singleton
    abstract fun bindThemeAppRepo(themeAppRepoImpl: ThemeAppRepoImpl): ThemeAppRepo

    @Binds
    @Singleton
    abstract fun bindBlockingAppRepo(blockingAppRepoImpl: BlockingAppRepoImpl): BlockingAppRepo

    @Binds
    @Singleton
    abstract fun bindSmartContractRepo(smartContractRepoImpl: SmartContractRepoImpl): SmartContractRepo

    @Binds
    @Singleton
    abstract fun bindSendFromWalletRepo(sendFromWalletRepoImp: SendFromWalletRepoImpl): SendFromWalletRepo

    @Binds
    @Singleton
    abstract fun bindTXDetailsRepo(txDetailsRepoImpl: TXDetailsRepoImpl): TXDetailsRepo

    @Binds
    @Singleton
    abstract fun bindWalletSotRepo(walletSotRepoImpl: WalletSotRepoImpl): WalletSotRepo

    @Binds
    @Singleton
    abstract fun bindSettingsAccountRepo(settingsAccountRepo: SettingsAccountRepoImpl): SettingsAccountRepo

    @Binds
    @Singleton
    abstract fun bindWalletAddressRepo(walletAddressRepo: WalletAddressRepoImpl): WalletAddressRepo

    @Binds
    @Singleton
    abstract fun bindWalletInfoRepo(walletInfoRepo: WalletInfoRepoImpl): WalletInfoRepo

    @Binds
    @Singleton
    abstract fun bindWalletAddedRepo(walletAddedRepoImpl: WalletAddedRepoImpl): WalletAddedRepo
}
