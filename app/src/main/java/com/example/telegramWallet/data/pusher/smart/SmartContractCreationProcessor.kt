package com.example.telegramWallet.data.pusher.smart

import android.util.Log
import com.example.telegramWallet.data.pusher.PusherDI
import com.example.telegramWallet.data.pusher.PusherEventProcessor
import com.pusher.client.channel.PusherEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartContractCreationProcessor : PusherEventProcessor {
    override fun processEvent(
        eventData: PusherEvent,
        notificationFunction: (String, String) -> Unit,
        di: PusherDI,
    ) {
//        try {
//            val gson = Gson()
//            val contractData: SmartContractCreateModel =
//                gson.fromJson(eventData.data, SmartContractCreateModel::class.java)
//
//            notificationFunction(
//                "Новый контракт",
//                "ID: ${contractData.id}\n" +
//                        "Сумма: ${contractData.amount.toTokenAmount()}"
//            );
//        } catch (e: Exception) {
//            Log.e("Pusher Error", e.toString())
//            return
//        }

        CoroutineScope(Dispatchers.Default).launch {
            di.smartContractStorage.getMyContractDeals()
        }
        Log.i("SmartContractProcessor", "Processing smart contract creation with data: $eventData")
    }
}
