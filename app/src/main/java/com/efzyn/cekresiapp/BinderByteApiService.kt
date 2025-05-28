package com.efzyn.cekresiapp.network



import com.efzyn.cekresiapp.model.Courier
import com.efzyn.cekresiapp.model.TrackingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BinderByteApiService {

    @GET("v1/list_courier") // Pastikan endpoint ini benar
    suspend fun getListCourier(
        @Query("api_key") apiKey: String
    ): Response<List<Courier>> // Asumsi responsnya adalah List dari Courier

    @GET("v1/track")
    suspend fun trackShipment(
        @Query("api_key") apiKey: String,
        @Query("courier") courierCode: String,
        @Query("awb") awbNumber: String
    ): Response<TrackingResponse>
}