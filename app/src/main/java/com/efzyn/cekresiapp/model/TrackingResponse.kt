package com.efzyn.cekresiapp.model

import com.google.gson.annotations.SerializedName

/**
 * Kelas data utama yang merepresentasikan keseluruhan respons JSON dari API pelacakan.
 */
data class TrackingResponse(
    @SerializedName("status")
    val status: Int,          // Kode status dari respons API
    @SerializedName("message")
    val message: String?,     // Pesan dari API (bisa null).
    @SerializedName("data")
    val data: TrackData?      // Objek yang berisi detail pelacakan jika sukses (bisa null jika error).
)

/**
 * Kelas data yang membungkus informasi utama pelacakan (summary, detail, dan history).
 */
data class TrackData(
    @SerializedName("summary")
    val summary: TrackingSummary, // Ringkasan status paket saat ini.
    @SerializedName("detail")
    val detail: TrackingDetail,   // Detail pengirim, penerima, asal, dan tujuan.
    @SerializedName("history")
    val history: List<TrackingHistoryItem> // Daftar riwayat perjalanan paket.
)

/**
 * Kelas data untuk ringkasan status paket terkini.
 */
data class TrackingSummary(
    @SerializedName("awb")
    val awb: String,          // Nomor resi (Air Waybill).
    @SerializedName("courier")
    val courier: String,      // Nama kurir yang menangani paket.
    @SerializedName("service")
    val service: String?,     // Jenis layanan pengiriman (bisa null).
    @SerializedName("status")
    val status: String,       // Status terakhir paket (misalnya, "DELIVERED", "ON PROCESS").
    @SerializedName("date")
    val date: String,         // Tanggal dan waktu dari status terakhir.
    @SerializedName("desc")
    val desc: String?,        // Deskripsi singkat mengenai status terakhir (bisa null atau kosong).
    @SerializedName("amount")
    val amount: String?,      // Informasi biaya, seringkali null atau kosong.
    @SerializedName("weight")
    val weight: String?       // Informasi berat paket, seringkali null atau kosong.
)

/**
 * Kelas data untuk detail pengiriman seperti pengirim, penerima, asal, dan tujuan.
 * Semua field bisa null karena API mungkin tidak selalu menyediakannya.
 */
data class TrackingDetail(
    @SerializedName("origin")
    val origin: String?,      // Kota atau lokasi asal pengiriman.
    @SerializedName("destination")
    val destination: String?, // Kota atau lokasi tujuan pengiriman.
    @SerializedName("shipper")
    val shipper: String?,     // Nama pengirim.
    @SerializedName("receiver")
    val receiver: String?      // Nama penerima.
)

/**
 * Kelas data untuk setiap entri dalam riwayat perjalanan paket yang diterima dari API.
 */
data class TrackingHistoryItem(
    @SerializedName("date")
    val date: String,         // Tanggal dan waktu kapan status riwayat ini terjadi.
    @SerializedName("desc")
    val desc: String,         // Deskripsi dari status pada titik riwayat ini.
    @SerializedName("location")
    val location: String?     // Lokasi di mana status riwayat ini tercatat (bisa null atau kosong).
)
