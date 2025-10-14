package com.profpay.wallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartContractDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(addressEntity: SmartContractEntity): Long

    @Query("SELECT * FROM smart_contracts")
    fun getSmartContractFlow(): Flow<SmartContractEntity?>

    @Query("SELECT * FROM smart_contracts")
    suspend fun getSmartContract(): SmartContractEntity?

    @Query("UPDATE smart_contracts SET contract_address = :contractAddress")
    suspend fun restoreSmartContract(contractAddress: String)
}
