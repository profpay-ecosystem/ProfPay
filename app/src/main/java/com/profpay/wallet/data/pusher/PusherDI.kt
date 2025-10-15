package com.profpay.wallet.data.pusher

import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.repository.flow.SmartContractRepo

class PusherDI(
    val smartContractStorage: SmartContractRepo,
    val profileStorage: ProfileRepo,
) : SmartContractRepo by smartContractStorage,
    ProfileRepo by profileStorage
