package com.profpay.wallet.data.database.repositories

import com.profpay.wallet.data.database.dao.StatesDao
import com.profpay.wallet.data.database.entities.StatesEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface StatesRepo {
    suspend fun saveData(statesEntity: StatesEntity)

    suspend fun loadData(key: String): String?
}

@Singleton
class StatesRepoImpl
    @Inject
    constructor(
        private val statesDao: StatesDao,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : StatesRepo {
        override suspend fun saveData(statesEntity: StatesEntity) {
            withContext(dispatcher) {
                statesDao.saveData(statesEntity)
            }
        }

        override suspend fun loadData(key: String): String? {
            return withContext(dispatcher) {
                return@withContext statesDao.loadData(key)
            }
        }
    }
