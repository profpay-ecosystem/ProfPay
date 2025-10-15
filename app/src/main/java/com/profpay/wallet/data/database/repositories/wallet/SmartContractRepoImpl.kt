package com.profpay.wallet.data.database.repositories.wallet

import com.profpay.wallet.data.database.dao.wallet.SmartContractDao
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface SmartContractRepo {
    suspend fun insert(addressEntity: SmartContractEntity): Long

    fun getSmartContractFlow(): Flow<SmartContractEntity?>

    suspend fun getSmartContract(): SmartContractEntity?

    suspend fun restoreSmartContract(contractAddress: String)
}

@Singleton
class SmartContractRepoImpl
    @Inject
    constructor(
        private val smartContractDao: SmartContractDao,
    ) : SmartContractRepo {
        override suspend fun insert(addressEntity: SmartContractEntity): Long = smartContractDao.insert(addressEntity)

        override fun getSmartContractFlow(): Flow<SmartContractEntity?> = smartContractDao.getSmartContractFlow()

        override suspend fun getSmartContract(): SmartContractEntity? = smartContractDao.getSmartContract()

        override suspend fun restoreSmartContract(contractAddress: String) = smartContractDao.restoreSmartContract(contractAddress)
    }
