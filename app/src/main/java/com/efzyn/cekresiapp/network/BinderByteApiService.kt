package com.efzyn.cekresiapp.network

import com.efzyn.cekresiapp.model.Courier
import com.efzyn.cekresiapp.model.TrackingResponse
// Import library Retrofit untuk anotasi HTTP request
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface yang mendefinisikan endpoint-endpoint API dari BinderByte.
 */
interface BinderByteApiService {

    /**
     * Mengambil daftar semua kurir yang didukung.
     */
    @GET("v1/list_courier") // Mendefinisikan path endpoint relatif terhadap BASE_URL (misalnya, "https://api.binderbyte.com/v1/list_courier")
    suspend fun getListCourier(
        @Query("api_key") apiKey: String
    ): Response<List<Courier>>

    /**
     * Fungsi untuk melacak status pengiriman berdasarkan nomor resi (AWB) dan kode kurir.
     */
    @GET("v1/track") // Mendefinisikan path endpoint relatif, contoh: "https://api.binderbyte.com/v1/track"
    suspend fun trackShipment(
        @Query("api_key") apiKey: String,
        @Query("courier") courierCode: String,
        @Query("awb") awbNumber: String
    ): Response<TrackingResponse>
}
