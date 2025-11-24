package com.profpay.wallet.data.services.pushy

import android.content.Intent
import kotlinx.serialization.json.Json

class PushEventMapper() {
    private val localJson = Json { ignoreUnknownKeys = false }

    fun map(intent: Intent): PushEvent? {
        intent.getStringExtra("amlPaymentSuccessfullyMessage")?.let {
            return localJson.decodeFromString<PushEvent.AmlPaymentSuccess>(it)
        }
        intent.getStringExtra("amlPaymentErrorMessage")?.let {
            return localJson.decodeFromString<PushEvent.AmlPaymentError>(it)
        }
        intent.getStringExtra("transferErrorMessage")?.let {
            return localJson.decodeFromString<PushEvent.TransferError>(it)
        }
        intent.getStringExtra("transferSuccessfullyMessage")?.let {
            return localJson.decodeFromString<PushEvent.TransferSuccess>(it)
        }
        intent.getStringExtra("pushyDeployContractSuccessfullyMessage")?.let {
            return localJson.decodeFromString<PushEvent.DeployContractSuccess>(it)
        }
        intent.getStringExtra("newTransactionMessage")?.let {
            return localJson.decodeFromString<PushEvent.NewTransaction>(it)
        }

        return null
    }
}
