package com.profpay.wallet.bridge.viewmodel.smartcontract

enum class SmartContractCreateTypes(
    typeId: Int,
) {
    CREATE(0),
    CONFIRM_OWNER(1),
    CONFIRM_RECEIVER(2),
    CONFIRM_ADMIN(3),
    TRANSFER(4),
    TERMINATION(5),
}
