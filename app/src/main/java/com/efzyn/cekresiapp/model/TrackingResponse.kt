package com.efzyn.cekresiapp.model

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
    val detail: TrackingDetail,
    @SerializedName("history")
    val history: List<TrackingHistoryItem> // Menggunakan nama yang lebih jelas
)

data class TrackingSummary(
    @SerializedName("awb")
    val awb: String,
    @SerializedName("courier")
    val courier: String, // Nama kurir dari API tracking
    @SerializedName("service")
    val service: String?,
    @SerializedName("status")
    val status: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String?,
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("weight")
    val weight: String?
)

data class TrackingDetail(
    @SerializedName("origin")
    val origin: String?,
    @SerializedName("destination")
    val destination: String?,
    @SerializedName("shipper")
    val shipper: String?,
    @SerializedName("receiver")
    val receiver: String?
)

// Mengganti nama dari TrackingHistory menjadi TrackingHistoryItem agar tidak bentrok
// dengan model HistoryItem untuk SharedPreferences, meskipun fieldnya sama.
// Atau, jika fieldnya identik, kamu bisa menggunakan satu model saja.
// Untuk contoh ini, saya bedakan untuk kejelasan konteks API vs Lokal.
data class TrackingHistoryItem( // Ini adalah item riwayat dari API BinderByte
    @SerializedName("date")
    val date: String,
    @SerializedName("desc")
    val desc: String,
    @SerializedName("location")
    val location: String?
)
