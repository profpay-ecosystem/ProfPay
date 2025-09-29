package com.example.telegramWallet.data.database.repositories.wallet

import androidx.lifecycle.LiveData
import com.example.telegramWallet.data.database.dao.wallet.SmartContractDao
import com.example.telegramWallet.data.database.entities.wallet.SmartContractEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface SmartContractRepo {
    suspend fun insert(addressEntity: SmartContractEntity): Long

    suspend fun getSmartContractLiveData(): LiveData<SmartContractEntity?>

    suspend fun getSmartContract(): SmartContractEntity?

    suspend fun restoreSmartContract(contractAddress: String)
}

@Singleton
class SmartContractRepoImpl
    @Inject
    constructor(
        private val smartContractDao: SmartContractDao,
    ) : SmartContractRepo {
        override suspend fun insert(addressEntity: SmartContractEntity): Long {
            return withContext(Dispatchers.IO) {
                return@withContext smartContractDao.insert(addressEntity)
            }
        }

        override suspend fun getSmartContractLiveData(): LiveData<SmartContractEntity?> {
            return withContext(Dispatchers.IO) {
                return@withContext smartContractDao.getSmartContractLiveData()
            }
        }

        override suspend fun getSmartContract(): SmartContractEntity? {
            return withContext(Dispatchers.IO) {
                return@withContext smartContractDao.getSmartContract()
            }
        }

        override suspend fun restoreSmartContract(contractAddress: String) {
            return withContext(Dispatchers.IO) {
                return@withContext smartContractDao.restoreSmartContract(contractAddress)
            }
        }
    }
