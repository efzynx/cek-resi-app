package com.efzyn.cekresiapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // BASE_URL API
    private const val BASE_URL = "https://api.binderbyte.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Set ke Level.BODY hanya untuk debug build, untuk release bisa Level.NONE atau BASIC
        level = HttpLoggingInterceptor.Level.BODY
    }
//    Set timeout ke API
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: BinderByteApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(BinderByteApiService::class.java)
    }
}
