package com.profpay.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.profpay.wallet.data.database.entities.StatesEntity

@Dao
interface StatesDao {
    @Insert(entity = StatesEntity::class)
    fun saveData(states: StatesEntity)

    @Query("SELECT data FROM states WHERE channel = :key")
    fun loadData(key: String): String?
}
