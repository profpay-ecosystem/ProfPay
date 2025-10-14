package com.profpay.wallet.exceptions.grpc

class GrpcResponseException(message: String, cause: Throwable? = null) : Exception(message, cause)