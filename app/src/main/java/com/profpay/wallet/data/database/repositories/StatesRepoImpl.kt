package com.profpay.wallet.data.database.repositories

import com.profpay.wallet.data.database.dao.StatesDao
import com.profpay.wallet.data.database.entities.StatesEntity
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : StatesRepo {
        override suspend fun saveData(statesEntity: StatesEntity) {
            withContext(ioDispatcher) {
                statesDao.saveData(statesEntity)
            }
        }

        override suspend fun loadData(key: String): String? {
            return withContext(ioDispatcher) {
                return@withContext statesDao.loadData(key)
            }
        }
    }
