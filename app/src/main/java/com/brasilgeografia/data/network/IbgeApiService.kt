package com.brasilgeografia.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IbgeApiService {
    @GET("malhas/paises/BR")
    suspend fun getMalhaPais(
        @Query("intrarregiao") intrarregiao: String = "UF"
    ): Response<ResponseBody>
}
