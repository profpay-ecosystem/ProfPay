package com.example.telegramWallet.data.database.entities.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "smart_contracts",
    indices = [
        Index(
            value = ["contract_address"],
            name = "smart_contracts_ind_contract_address",
            unique = true,
        ),
    ],
)
data class SmartContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "contract_address") val contractAddress: String,
    @ColumnInfo(name = "owner_address") val ownerAddress: String,
    @ColumnInfo(name = "open_deals_count") val openDealsCount: Long? = 0,
    @ColumnInfo(name = "closed_deals_count") val closedDealsCount: Long? = 0,
)
