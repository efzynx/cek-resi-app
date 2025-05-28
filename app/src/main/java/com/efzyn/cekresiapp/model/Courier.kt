package com.efzyn.cekresiapp.model

import com.google.gson.annotations.SerializedName

data class Courier(
    @SerializedName("code")
    val code: String,
    @SerializedName("description") // Atau "name", sesuaikan dengan field dari API
    val name: String
    // Tambahkan field lain jika ada dari API, misal "logo_url"
)