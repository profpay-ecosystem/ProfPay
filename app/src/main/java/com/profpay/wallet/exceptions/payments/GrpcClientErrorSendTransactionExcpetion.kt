package com.profpay.wallet.exceptions.payments

class GrpcClientErrorSendTransactionExcpetion : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
