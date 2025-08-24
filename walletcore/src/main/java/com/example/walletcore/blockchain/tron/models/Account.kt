package com.example.walletcore.blockchain.tron.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TronAccountPermission (
    val threshold: Long
)

@Serializable
data class TronVote (
    @SerialName("vote_address")
    val voteAddress: String,
    @SerialName("vote_count")
    val voteCount: Long
)

@Serializable
data class TronFrozen (
    val type: String? = null,
    val amount: Long? = null
)

@Serializable
data class TronUnfrozen (
    @SerialName("unfreeze_amount")
    val unfreezeAmount: Long? = null,
    @SerialName("unfreeze_expire_time")
    val unfreezeExpireTime: Long? = null
)

@Serializable
data class TronAccount (
    val balance: Long? = null,
    val address: String? = null,
    @SerialName("active_permission")
    val activePermission: List<TronAccountPermission>? = null,
    val votes: List<TronVote>? = null,
    val frozenV2: List<TronFrozen>? = null,
    val unfrozenV2: List<TronUnfrozen>? = null
)

@Serializable
data class TronAccountRequest (
    val address: String,
    val visible: Boolean
)

@Serializable
data class TronAccountUsage (
    val freeNetUsed: Long? = null,
    val freeNetLimit: Long? = null,
    @SerialName("EnergyUsed")
    val energyUsed: Long? = null,
    @SerialName("EnergyLimit")
    val energyLimit: Long? = null
)

@Serializable
data class TronEmptyAccount (
    val address: String? = null
)

@Serializable
data class TronReward (
    val reward: Long? = null
)

@Serializable
data class WitnessAccount (
    val address: String,
    val voteCount: Long? = null,
    val url: String,
    val isJobs: Boolean? = null
)

@Serializable
data class WitnessesList (
    val witnesses: List<WitnessAccount>
)

