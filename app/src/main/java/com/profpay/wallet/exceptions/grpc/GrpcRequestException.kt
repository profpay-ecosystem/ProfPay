package com.profpay.wallet.exceptions.grpc

class GrpcRequestException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
