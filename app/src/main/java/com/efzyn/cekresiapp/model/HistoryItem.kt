package com.efzyn.cekresiapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryItem( // Item riwayat yang disimpan lokal
    val awb: String,
    val courierCode: String,
    val courierName: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
