package com.profpay.wallet.data.database.dao.wallet

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

@Dao
interface CentralAddressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewCentralAddress(addressEntity: CentralAddressEntity): Long

    @Query("SELECT * FROM central_address LIMIT 1")
    suspend fun getCentralAddress(): CentralAddressEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM central_address)")
    suspend fun isCentralAddressExists(): Boolean

    @Query("UPDATE central_address SET balance = :value")
    suspend fun updateTrxBalance(value: BigInteger)

    @Query("UPDATE central_address SET address = :address, public_key = :publicKey, private_key = :privateKey, balance = 0")
    suspend fun changeCentralAddress(
        address: String,
        publicKey: String,
        privateKey: String,
    )

    @Query("SELECT * FROM central_address LIMIT 1")
    fun getCentralAddressFlow(): Flow<CentralAddressEntity?>
}
