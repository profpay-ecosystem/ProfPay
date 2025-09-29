package com.profpay.wallet.data.database.dao.wallet

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.tron.Tron
import java.math.BigInteger

@Dao
interface CentralAddressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNewCentralAddress(addressEntity: CentralAddressEntity): Long

    @Query("SELECT * FROM central_address LIMIT 1")
    fun getCentralAddress(): CentralAddressEntity?

    @Transaction
    fun insertIfNotExists(tron: Tron): CentralAddressEntity? {
        val existingEntity = getCentralAddress()
        return if (existingEntity != null) {
            existingEntity
        } else {
            val address = tron.addressUtilities.generateAddressAndMnemonic()
            insertNewCentralAddress(
                CentralAddressEntity(
                    address =
                        address.addressesWithKeysForM.addresses
                            .get(0)
                            .address,
                    publicKey =
                        address.addressesWithKeysForM.addresses
                            .get(0)
                            .publicKey,
                    privateKey =
                        address.addressesWithKeysForM.addresses
                            .get(0)
                            .privateKey,
                ),
            )
            getCentralAddress()
        }
    }

    @Query("UPDATE central_address SET balance = :value")
    fun updateTrxBalance(value: BigInteger)

    @Query("UPDATE central_address SET address = :address, public_key = :publicKey, private_key = :privateKey, balance = 0")
    fun changeCentralAddress(
        address: String,
        publicKey: String,
        privateKey: String,
    )

    @Query("SELECT * FROM central_address LIMIT 1")
    fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?>
}
