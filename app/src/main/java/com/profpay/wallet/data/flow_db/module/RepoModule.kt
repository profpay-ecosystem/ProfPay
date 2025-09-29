package com.profpay.wallet.data.flow_db.module

import com.profpay.wallet.data.flow_db.repo.AddressAndMnemonicRepo
import com.profpay.wallet.data.flow_db.repo.AddressAndMnemonicRepoImpl
import com.profpay.wallet.data.flow_db.repo.BlockingAppRepo
import com.profpay.wallet.data.flow_db.repo.BlockingAppRepoImpl
import com.profpay.wallet.data.flow_db.repo.SendFromWalletRepo
import com.profpay.wallet.data.flow_db.repo.SendFromWalletRepoImpl
import com.profpay.wallet.data.flow_db.repo.SettingsAccountRepo
import com.profpay.wallet.data.flow_db.repo.SettingsAccountRepoImpl
import com.profpay.wallet.data.flow_db.repo.SmartContractRepo
import com.profpay.wallet.data.flow_db.repo.SmartContractRepoImpl
import com.profpay.wallet.data.flow_db.repo.TXDetailsRepo
import com.profpay.wallet.data.flow_db.repo.TXDetailsRepoImpl
import com.profpay.wallet.data.flow_db.repo.ThemeAppRepo
import com.profpay.wallet.data.flow_db.repo.ThemeAppRepoImpl
import com.profpay.wallet.data.flow_db.repo.WalletAddressRepo
import com.profpay.wallet.data.flow_db.repo.WalletAddressRepoImpl
import com.profpay.wallet.data.flow_db.repo.WalletInfoRepo
import com.profpay.wallet.data.flow_db.repo.WalletInfoRepoImpl
import com.profpay.wallet.data.flow_db.repo.WalletSotRepo
import com.profpay.wallet.data.flow_db.repo.WalletSotRepoImpl
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
}
