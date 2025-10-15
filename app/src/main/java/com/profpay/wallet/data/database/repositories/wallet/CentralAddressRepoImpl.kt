package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.CentralAddressDao
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

interface CentralAddressRepo {
    suspend fun insertNewCentralAddress(addressEntity: CentralAddressEntity): Long

    suspend fun getCentralAddress(): CentralAddressEntity?

    suspend fun updateTrxBalance(value: BigInteger)

    suspend fun changeCentralAddress(
        address: String,
        publicKey: String,
        privateKey: String,
    )

    fun getCentralAddressFlow(): Flow<CentralAddressEntity?>

    suspend fun isCentralAddressExists(): Boolean
}

@Singleton
class CentralAddressRepoImpl
    @Inject
    constructor(
        private val centralAddressDao: CentralAddressDao,
    ) : CentralAddressRepo {
        override suspend fun insertNewCentralAddress(addressEntity: CentralAddressEntity): Long =
            centralAddressDao.insertNewCentralAddress(addressEntity)

        override suspend fun getCentralAddress(): CentralAddressEntity? = centralAddressDao.getCentralAddress()

        override suspend fun updateTrxBalance(value: BigInteger) = centralAddressDao.updateTrxBalance(value)

        override suspend fun changeCentralAddress(
            address: String,
            publicKey: String,
            privateKey: String,
        ) = centralAddressDao.changeCentralAddress(address = address, publicKey = publicKey, privateKey = privateKey)

        override fun getCentralAddressFlow(): Flow<CentralAddressEntity?> = centralAddressDao.getCentralAddressFlow()

        override suspend fun isCentralAddressExists(): Boolean = centralAddressDao.isCentralAddressExists()
    }
