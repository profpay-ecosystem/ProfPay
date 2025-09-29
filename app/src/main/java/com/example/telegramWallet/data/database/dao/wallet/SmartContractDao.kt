package com.example.telegramWallet.data.database.dao.wallet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.telegramWallet.data.database.entities.wallet.SmartContractEntity

@Dao
interface SmartContractDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(addressEntity: SmartContractEntity): Long

    @Query("SELECT * FROM smart_contracts")
    fun getSmartContractLiveData(): LiveData<SmartContractEntity?>

    @Query("SELECT * FROM smart_contracts")
    fun getSmartContract(): SmartContractEntity?

    @Query("UPDATE smart_contracts SET contract_address = :contractAddress")
    fun restoreSmartContract(contractAddress: String)
}
