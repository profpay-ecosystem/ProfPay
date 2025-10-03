package com.profpay.wallet.data.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.protobuf.ByteString
import com.profpay.wallet.MainActivity
import com.profpay.wallet.data.database.entities.wallet.SmartContractEntity
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.SmartContractRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.models.pushy.AmlPaymentErrorMessage
import com.profpay.wallet.models.pushy.AmlPaymentSuccessfullyMessage
import com.profpay.wallet.models.pushy.PushyDeployContractSuccessfullyMessage
import com.profpay.wallet.models.pushy.PushyTransferErrorMessage
import com.profpay.wallet.models.pushy.PushyTransferSuccessfullyMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.pushy.sdk.Pushy
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class PushReceiver :
    BroadcastReceiver(),
    CoroutineScope {
    @Inject lateinit var tokenRepo: TokenRepo

    @Inject lateinit var transactionRepo: TransactionsRepo

    @Inject lateinit var addressRepo: AddressRepo

    @Inject lateinit var smartContractRepo: SmartContractRepo

    @Inject lateinit var profileRepo: ProfileRepo

    @Inject lateinit var pendingTransactionRepo: PendingTransactionRepo

    @Inject lateinit var pendingAmlTransactionRepo: PendingAmlTransactionRepo

    private val localJson = Json { ignoreUnknownKeys = false }

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO + job

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val notificationTitle =
            if (intent.getStringExtra("title") !=
                null
            ) {
                intent.getStringExtra("title")
            } else {
                context.packageManager.getApplicationLabel(context.applicationInfo).toString()
            }
        val notificationText = if (intent.getStringExtra("message") != null) intent.getStringExtra("message") else "Test notification"
        val transferErrorMessage =
            if (intent.getStringExtra("transferErrorMessage") !=
                null
            ) {
                intent.getStringExtra("transferErrorMessage")
            } else {
                null
            }
        val pushyTransferSuccessfullyMessage =
            if (intent.getStringExtra("pushyTransferSuccessfullyMessage") !=
                null
            ) {
                intent.getStringExtra("pushyTransferSuccessfullyMessage")
            } else {
                null
            }
        val transferSuccessfullyMessage =
            if (intent.getStringExtra("transferSuccessfullyMessage") !=
                null
            ) {
                intent.getStringExtra("transferSuccessfullyMessage")
            } else {
                null
            }

        val pushyDeployContractSuccessfullyMessage =
            if (intent.getStringExtra("pushyDeployContractSuccessfullyMessage") !=
                null
            ) {
                intent.getStringExtra("pushyDeployContractSuccessfullyMessage")
            } else {
                null
            }
        var pushyDeployContractErrorMessage =
            if (intent.getStringExtra("pushyDeployContractErrorMessage") !=
                null
            ) {
                intent.getStringExtra("pushyDeployContractErrorMessage")
            } else {
                null
            }

        var amlPaymentSuccessfullyMessage =
            if (intent.getStringExtra("amlPaymentSuccessfullyMessage") !=
                null
            ) {
                intent.getStringExtra("amlPaymentSuccessfullyMessage")
            } else {
                null
            }
        var amlPaymentErrorMessage =
            if (intent.getStringExtra("amlPaymentErrorMessage") !=
                null
            ) {
                intent.getStringExtra("amlPaymentErrorMessage")
            } else {
                null
            }

        if (amlPaymentSuccessfullyMessage != null) {
            val pushyObj = localJson.decodeFromString<AmlPaymentSuccessfullyMessage>(amlPaymentSuccessfullyMessage)
            launch {
                pendingAmlTransactionRepo.markAsSuccessful(pushyObj.transactionId)
            }
        }

        if (amlPaymentErrorMessage != null) {
            val pushyObj = localJson.decodeFromString<AmlPaymentErrorMessage>(amlPaymentErrorMessage)
            launch {
                pendingAmlTransactionRepo.markAsError(pushyObj.transactionId)
            }
        }

        if (transferErrorMessage != null) {
            val pushyObj = localJson.decodeFromString<PushyTransferErrorMessage>(transferErrorMessage)
            launch {
                val address = addressRepo.getAddressEntityByAddress(pushyObj.senderAddress)
                pendingTransactionRepo.deletePendingTransactionByTxId(pushyObj.transactionId)
                transactionRepo.deleteTransactionByTxId(pushyObj.transactionId)

                if (address?.addressId != null) {
                    transactionRepo.transactionSetProcessedUpdateFalseByTxId(pushyObj.transactionId)
                }
            }
        }

        if (pushyDeployContractSuccessfullyMessage != null) {
            val pushyObj = localJson.decodeFromString<PushyDeployContractSuccessfullyMessage>(pushyDeployContractSuccessfullyMessage)
            launch {
                if (smartContractRepo.getSmartContract() == null) {
                    smartContractRepo.insert(
                        SmartContractEntity(
                            contractAddress = pushyObj.contractAddress,
                            ownerAddress = pushyObj.address,
                        ),
                    )
                } else {
                    smartContractRepo.restoreSmartContract(pushyObj.contractAddress)
                }
            }
        }

        if (transferSuccessfullyMessage != null) {
            val pushyObj = localJson.decodeFromString<PushyTransferSuccessfullyMessage>(transferSuccessfullyMessage)
            launch {
                pendingTransactionRepo.deletePendingTransactionByTxId(pushyObj.txid)
            }
        }

        if (pushyTransferSuccessfullyMessage == null) {
            val channelId = "PUSHY_SERVICE_CHANNEL"
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val channelName = "Pushy Channel Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(0, 400, 250, 400)
            notificationManager.createNotificationChannel(notificationChannel)

            val builder =
                NotificationCompat
                    .Builder(context, channelId)
                    .setAutoCancel(true)
                    .setSmallIcon(android.R.drawable.stat_notify_chat)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setLights(Color.RED, 1000, 1000)
                    .setVibrate(longArrayOf(0, 400, 250, 400))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(
                        PendingIntent.getActivity(
                            context,
                            0,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        ),
                    )

            Pushy.setNotificationChannel(builder, context)
            notificationManager.notify((Math.random() * 100000).toInt(), builder.build())
        }
    }

    fun ByteString.toHex(): String {
        val hexString = StringBuilder()
        for (byte in this) {
            val hex = String.format("%02x", byte)
            hexString.append(hex)
        }
        return hexString.toString()
    }
}
