package com.efzyn.cekresiapp.model // Ganti com.example.cekresi_uts dengan package-mu

import com.google.gson.annotations.SerializedName

data class TrackingResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: TrackData?
)

data class TrackData(
    @SerializedName("summary")
    val summary: TrackingSummary,
    @SerializedName("detail")
    val detail: TrackingDetail, // Detail pengirim, penerima, asal, tujuan
    @SerializedName("history")
    val history: List<TrackingHistory>
)

data class TrackingSummary(
    @SerializedName("awb")
    val awb: String,
    @SerializedName("courier") // Nama kurir dari API
    val courier: String,
    @SerializedName("service")
    val service: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("date")
    val date: String, // Tanggal status terakhir
    @SerializedName("desc")
    val desc: String?, // Deskripsi status terakhir (bisa kosong)
    @SerializedName("amount")
    val amount: String?, // Tambahan dari JSON output
    @SerializedName("weight")
    val weight: String?  // Tambahan dari JSON output
)

data class TrackingDetail(
    @SerializedName("origin")
    val origin: String?, // Bisa kosong
    @SerializedName("destination")
    val destination: String?, // Bisa kosong
    @SerializedName("shipper")
    val shipper: String?, // Bisa kosong
    @SerializedName("receiver")
    val receiver: String? // Bisa kosong
)

data class TrackingHistory(
    @SerializedName("date")
    val date: String,
    @SerializedName("desc") // Deskripsi histori
    val desc: String,
    @SerializedName("location")
    val location: String? // Bisa kosong
)
