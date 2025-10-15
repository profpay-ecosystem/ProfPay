package com.profpay.wallet.utils

import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.Tron

suspend fun resolvePrivateKey(
    walletId: Long,
    addressEntity: AddressEntity,
    resolvePrivateKeyDeps: ResolvePrivateKeyDeps,
): ByteArray {
    val generalAddress = resolvePrivateKeyDeps.addressRepo.getGeneralAddressByWalletId(walletId)
    val cipherData = resolvePrivateKeyDeps.walletProfileRepo.getWalletCipherData(walletId)

    val entropy =
        resolvePrivateKeyDeps.keystoreCryptoManager.decrypt(
            alias = generalAddress,
            iv = cipherData.iv,
            cipherText = cipherData.cipherText,
        )

    return resolvePrivateKeyDeps.tron.addressUtilities.derivePrivateKeyFromEntropy(
        entropy,
        addressEntity.sotDerivationIndex,
    )
}

data class ResolvePrivateKeyDeps(
    val addressRepo: AddressRepo,
    val walletProfileRepo: WalletProfileRepo,
    val keystoreCryptoManager: KeystoreCryptoManager,
    val tron: Tron,
)
