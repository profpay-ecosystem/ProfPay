package com.example.telegramWallet.data.pusher.smart

import com.example.telegramWallet.data.pusher.PusherDI
import com.example.telegramWallet.data.pusher.PusherEventProcessor
import com.pusher.client.channel.PusherEvent

class SmartContractCreatedProcessor : PusherEventProcessor {
    override fun processEvent(
        eventData: PusherEvent,
        notificationFunction: (String, String) -> Unit,
        di: PusherDI
    ) {

    }
}