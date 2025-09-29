package com.profpay.wallet.data.pusher

import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.flow_db.repo.SmartContractRepo

class PusherDI(
    val smartContractStorage: SmartContractRepo,
    val profileStorage: ProfileRepo,
) : SmartContractRepo by smartContractStorage,
    ProfileRepo by profileStorage
