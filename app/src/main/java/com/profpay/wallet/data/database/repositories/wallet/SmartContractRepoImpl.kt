package com.profpay.wallet.data.database.repositories.wallet

import androidx.lifecycle.LiveData
import com.profpay.wallet.data.database.dao.wallet.SmartContractDao
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import kotlinx.coroutines.CoroutineDispatcher
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
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : SmartContractRepo {
        override suspend fun insert(addressEntity: SmartContractEntity): Long {
            return withContext(dispatcher) {
                return@withContext smartContractDao.insert(addressEntity)
            }
        }

        override suspend fun getSmartContractLiveData(): LiveData<SmartContractEntity?> {
            return withContext(dispatcher) {
                return@withContext smartContractDao.getSmartContractLiveData()
            }
        }

        override suspend fun getSmartContract(): SmartContractEntity? {
            return withContext(dispatcher) {
                return@withContext smartContractDao.getSmartContract()
            }
        }

        override suspend fun restoreSmartContract(contractAddress: String) {
            return withContext(dispatcher) {
                return@withContext smartContractDao.restoreSmartContract(contractAddress)
            }
        }
    }
