package com.profpay.wallet.data.pusher.smart

import com.profpay.wallet.data.pusher.PusherDI
import com.profpay.wallet.data.pusher.PusherEventProcessor
import com.pusher.client.channel.PusherEvent

class SmartContractCreateDealProcessor : PusherEventProcessor {
    override fun processEvent(
        eventData: PusherEvent,
        notificationFunction: (String, String) -> Unit,
        di: PusherDI,
    ) {
    }
}
