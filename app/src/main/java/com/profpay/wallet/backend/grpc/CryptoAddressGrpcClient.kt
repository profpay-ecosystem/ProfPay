package com.profpay.wallet.backend.grpc

import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import com.profpay.wallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.address.CryptoAddressProto
import org.example.protobuf.address.CryptoAddressServiceGrpc

class CryptoAddressGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val stub: CryptoAddressServiceGrpc.CryptoAddressServiceBlockingStub = CryptoAddressServiceGrpc.newBlockingStub(channel)

    suspend fun addCryptoAddress(
        appId: String,
        address: String,
        pubKey: String,
        derivedIndices: Iterable<Int>,
    ): Result<CryptoAddressProto.AddCryptoAddressResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val request =
                    CryptoAddressProto.AddCryptoAddressRequest
                        .newBuilder()
                        .setAppId(appId)
                        .setAddress(address)
                        .setPubKey(pubKey)
                        .addAllDerivedIndices(derivedIndices)
                        .build()
                val response = stub.addCryptoAddress(request)
                Result.success(response)
            }
        }

    suspend fun updateDerivedIndex(
        appId: String,
        oldIndex: Long,
        newIndex: Long,
        generalAddress: String,
    ): Result<CryptoAddressProto.UpdateDerivedIndexResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val request =
                    CryptoAddressProto.UpdateDerivedIndexRequest
                        .newBuilder()
                        .setAppId(appId)
                        .setGeneralAddress(generalAddress)
                        .setOldIndex(oldIndex)
                        .setNewIndex(newIndex)
                        .build()
                val response = stub.updateDerivedIndex(request)
                Result.success(response)
            }
        }

    suspend fun setDerivedIndex(
        userId: Long,
        generalAddress: String,
        derivedIndices: Iterable<Int>,
    ): Result<CryptoAddressProto.SetDerivedIndicesResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val request =
                    CryptoAddressProto.SetDerivedIndicesRequest
                        .newBuilder()
                        .setUserId(userId)
                        .setGeneralAddress(generalAddress)
                        .addAllDerivedIndices(derivedIndices)
                        .build()
                val response = stub.setDerivedIndices(request)
                Result.success(response)
            }
        }

    suspend fun getWalletData(address: String): Result<CryptoAddressProto.GetWalletDataResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val request =
                    CryptoAddressProto.GetWalletDataRequest
                        .newBuilder()
                        .setAddress(address)
                        .build()
                val response = stub.getWalletData(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}
