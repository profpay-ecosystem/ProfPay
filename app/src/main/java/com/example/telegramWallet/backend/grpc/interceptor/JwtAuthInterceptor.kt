package com.example.telegramWallet.backend.grpc.interceptor

import com.example.telegramWallet.data.flow_db.token.TokenProvider
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor

class JwtAuthInterceptor(
    private val tokenProvider: TokenProvider,
) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        val accessToken = tokenProvider.getAccessToken()

        val headersAuthorization =
            Metadata().apply {
                put(
                    Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
                    "Bearer $accessToken",
                )
            }

        val call = next.newCall(method, callOptions)
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            override fun start(
                responseListener: Listener<RespT>,
                headers: Metadata,
            ) {
                headers.merge(headersAuthorization)
                super.start(responseListener, headers)
            }
        }
    }
}
