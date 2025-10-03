package com.profpay.wallet.data.database.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressTrackerListModel(
    val id: Long,
    @SerialName("target_address") val targetAddress: String,
    @SerialName("min_incoming_value") val minIncomingValue: Long,
    @SerialName("min_outgoing_value") val minOutgoingValue: Long,
    @SerialName("is_active") val isActive: Boolean,
)
