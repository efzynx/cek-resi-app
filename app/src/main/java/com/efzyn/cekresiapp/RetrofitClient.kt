package com.efzyn.cekresiapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.binderbyte.com/" // Pastikan BASE_URL benar

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log level BODY untuk melihat detail request/response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Tambahkan interceptor hanya untuk debug build
        .build()

    val instance: BinderByteApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Gunakan OkHttpClient yang sudah dikonfigurasi
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(BinderByteApiService::class.java)
    }
}