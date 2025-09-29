package com.example.telegramWallet.data.pusher

import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.flow_db.repo.SmartContractRepo

class PusherDI(
    val smartContractStorage: SmartContractRepo,
    val profileStorage: ProfileRepo,
) : SmartContractRepo by smartContractStorage,
    ProfileRepo by profileStorage
