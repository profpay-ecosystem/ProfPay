package com.profpay.wallet.data.database.repositories.wallet

import androidx.lifecycle.LiveData
import com.profpay.wallet.data.database.dao.wallet.CentralAddressDao
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.tron.Tron
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface CentralAddressRepo {
    suspend fun insertNewCentralAddress(addressEntity: CentralAddressEntity): Long

    suspend fun insertIfNotExists(): CentralAddressEntity?

    suspend fun getCentralAddress(): CentralAddressEntity?

    suspend fun updateTrxBalance(value: BigInteger)

    suspend fun changeCentralAddress(
        address: String,
        publicKey: String,
        privateKey: String,
    )

    suspend fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?>
}

@Singleton
class CentralAddressRepoImpl
    @Inject
    constructor(
        private val centralAddressDao: CentralAddressDao,
        private val tron: Tron,
    ) : CentralAddressRepo {
        override suspend fun insertNewCentralAddress(addressEntity: CentralAddressEntity): Long =
            centralAddressDao.insertNewCentralAddress(addressEntity)

        override suspend fun insertIfNotExists(): CentralAddressEntity? = centralAddressDao.insertIfNotExists(tron)

        override suspend fun getCentralAddress(): CentralAddressEntity? {
            return withContext(Dispatchers.IO) {
                return@withContext centralAddressDao.getCentralAddress()
            }
        }

        override suspend fun updateTrxBalance(value: BigInteger) = centralAddressDao.updateTrxBalance(value)

        override suspend fun changeCentralAddress(
            address: String,
            publicKey: String,
            privateKey: String,
        ) = centralAddressDao.changeCentralAddress(address = address, publicKey = publicKey, privateKey = privateKey)

        override suspend fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?> {
            return withContext(Dispatchers.IO) {
                return@withContext centralAddressDao.getCentralAddressLiveData()
            }
        }
    }
