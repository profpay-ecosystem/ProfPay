package com.example.telegramWallet.exceptions.payments

class GrpcServerErrorSendTransactionExcpetion : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
