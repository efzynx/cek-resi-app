package com.efzyn.cekresiapp.model // Ganti dengan package-mu

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Untuk kemudahan mengirim antar activity jika perlu
data class HistoryItem( // Ini adalah item riwayat yang disimpan lokal
    val awb: String,
    val courierCode: String,
    val courierName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
