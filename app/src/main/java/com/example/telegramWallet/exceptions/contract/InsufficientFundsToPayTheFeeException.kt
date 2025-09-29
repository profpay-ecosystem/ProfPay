package com.example.telegramWallet.exceptions.contract

class InsufficientFundsToPayTheFeeException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
