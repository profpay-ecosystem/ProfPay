package com.example.telegramWallet.data.pusher

import com.pusher.client.channel.PusherEvent

interface PusherEventProcessor {
    fun processEvent(
        eventData: PusherEvent,
        notificationFunction: (String, String) -> Unit,
        di: PusherDI,
    )
}
