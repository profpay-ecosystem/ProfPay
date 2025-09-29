package com.example.telegramWallet.backend.grpc

import com.example.telegramWallet.data.flow_db.token.SharedPrefsTokenProvider
import com.example.telegramWallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.address.CryptoAddressProto
import org.example.protobuf.address.CryptoAddressServiceGrpc

class CryptoAddressGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
) {
    private val stub: CryptoAddressServiceGrpc.CryptoAddressServiceBlockingStub = CryptoAddressServiceGrpc.newBlockingStub(channel)

    suspend fun addCryptoAddress(
        appId: String,
        address: String,
        pubKey: String,
        derivedIndices: Iterable<Int>,
    ): Result<CryptoAddressProto.AddCryptoAddressResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.IO) {
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
