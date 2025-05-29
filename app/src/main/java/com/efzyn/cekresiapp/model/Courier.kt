package com.efzyn.cekresiapp.model

import com.google.gson.annotations.SerializedName

data class Courier(
    @SerializedName("code") // Sesuaikan dengan field dari API
    val code: String,
    @SerializedName("description") // Sesuaikan dengan field dari API
    val name: String
)