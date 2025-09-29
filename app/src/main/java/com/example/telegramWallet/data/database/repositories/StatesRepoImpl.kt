package com.example.telegramWallet.data.database.repositories

import com.example.telegramWallet.data.database.dao.StatesDao
import com.example.telegramWallet.data.database.entities.StatesEntity
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
    ) : StatesRepo {
        override suspend fun saveData(statesEntity: StatesEntity) {
            withContext(Dispatchers.IO) {
                statesDao.saveData(statesEntity)
            }
        }

        override suspend fun loadData(key: String): String? {
            return withContext(Dispatchers.IO) {
                return@withContext statesDao.loadData(key)
            }
        }
    }
