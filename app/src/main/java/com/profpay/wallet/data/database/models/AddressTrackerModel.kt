package com.profpay.wallet.data.database.models

data class AddressTrackerListModel(
    val id: Long,
    val target_address: String,
    val min_incoming_value: Long,
    val min_outgoing_value: Long,
    val is_active: Boolean,
)
