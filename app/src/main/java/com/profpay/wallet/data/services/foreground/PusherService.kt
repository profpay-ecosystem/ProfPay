package com.profpay.wallet.data.services.foreground

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.profpay.wallet.MainActivity
import com.profpay.wallet.R
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.scheduler.transfer.tron.rollbackFrozenTransactions
import com.profpay.wallet.data.services.AmlProcessorService
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.AndroidEntryPoint
import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class PusherService :
    Service(),
    CoroutineScope {
    private var job: Job = Job()

    @Inject lateinit var profileRepo: ProfileRepo

    @Inject lateinit var addressRepo: AddressRepo

    @Inject lateinit var transactionsRepo: TransactionsRepo

    @Inject lateinit var tokenRepo: TokenRepo

    @Inject lateinit var centralAddressRepo: CentralAddressRepo

    @Inject lateinit var tron: Tron

    @Inject lateinit var pendingTransactionRepo: PendingTransactionRepo

    @Inject lateinit var grpcClientFactory: GrpcClientFactory

    @Inject lateinit var amlProcessorService: AmlProcessorService

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onBind(intent: Intent): IBinder? = null

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (!isRunning) {
            startForeground()
            isRunning = true

            launch {
                scheduleAll()
            }
        }

        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val uniqueNotificationID = System.currentTimeMillis().toInt() // Использование текущего времени для идентификатора
        val notification =
            Notification
                .Builder(this, CHANNEL_ID)
                .setContentTitle("Сервис уведомлений")
                .setContentText("Сервис уведомлений запущен! Данное уведомление невозможно скрыть.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(uniqueNotificationID, notification)
    }

    // Необходимая для сервиса функций уведомлений, Android по умолчанию выключает уведомления.
    private fun createNotificationChannel() {
        val serviceChannel =
            NotificationChannel(
                CHANNEL_ID,
                "Pusher Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        val manager =
            getSystemService(
                NotificationManager::class.java,
            )
        manager.createNotificationChannel(serviceChannel)
    }

    private suspend fun scheduleAll() =
        coroutineScope {
            val kronFastScheduler =
                buildSchedule {
                    seconds {
                        0 every 45
                    }
                }

            launch {
                kronFastScheduler.doInfinity {
                    rollbackFrozenTransactions(pendingTransactionRepo = pendingTransactionRepo, transactionsRepo = transactionsRepo)
                }
            }
        }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    companion object {
        @Volatile
        var isRunning = false
        private const val CHANNEL_ID = "PUSHER_SERVICE_CHANNEL"
    }
}
