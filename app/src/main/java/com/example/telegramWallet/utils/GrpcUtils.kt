package com.example.telegramWallet.utils

import com.example.telegramWallet.data.flow_db.token.TokenProvider
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object GrpcUtils {
    private val mutex = Mutex()

    suspend fun <T> safeCall(
        tokenProvider: TokenProvider,
        block: suspend () -> Result<T>,
    ): Result<T> =
        try {
            runCatching { block().getOrThrow() }.recoverCatching {
                val ex = it
                if (ex is StatusRuntimeException && ex.status.code == Status.Code.UNAUTHENTICATED) {
                    mutex.withLock {
                        tokenProvider.refreshTokensIfNeeded()
                    }
                    block().getOrThrow()
                } else {
                    throw ex
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}

suspend fun <T> TokenProvider.safeGrpcCall(block: suspend () -> Result<T>): Result<T> = GrpcUtils.safeCall(this, block)
