package com.example.walletcore.blockchain.tron.services

import com.example.walletcore.blockchain.tron.models.TronBlock
import com.example.walletcore.blockchain.tron.models.TronChainParameters
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface TronNodeStatusService {
    @GET("/wallet/getchainparameters")
    suspend fun getChainParameters(): Result<TronChainParameters>

    @POST("/wallet/getnowblock")
    suspend fun nowBlock(): Result<TronBlock>

    @POST//("/wallet/getnowblock")
    suspend fun nowBlock(@Url url: String): Response<TronBlock>
}