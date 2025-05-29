package com.efzyn.cekresiapp.utils

import android.content.Context
import android.util.Log
import com.efzyn.cekresiapp.model.HistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object HistoryManager {
    private const val PREFS_NAME = "CekResiAppPrefs" // Nama file SharedPreferences
    private const val KEY_HISTORY = "tracking_history_list" // Key untuk menyimpan list riwayat
    private const val MAX_HISTORY_ITEMS = 20 // Batas maksimal item riwayat
    private const val TAG = "HistoryManager"

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHistory(context: Context): MutableList<HistoryItem> {
        val prefs = getSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString(KEY_HISTORY, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<MutableList<HistoryItem>>() {}.type
                gson.fromJson(json, type) ?: mutableListOf() // Kembalikan list kosong jika parsing gagal
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing history JSON", e)
                mutableListOf() // Kembalikan list kosong jika ada error
            }
        } else {
            mutableListOf()
        }
    }

    fun addHistoryItem(context: Context, item: HistoryItem) {
        val historyList = getHistory(context)

        // Hapus item yang sama (berdasarkan AWB dan kode kurir) jika sudah ada, untuk memindahkannya ke atas
        val existingItemIndex = historyList.indexOfFirst { it.awb == item.awb && it.courierCode == item.courierCode }
        if (existingItemIndex != -1) {
            historyList.removeAt(existingItemIndex)
        }

        // Tambahkan item baru di awal list (paling atas)
        historyList.add(0, item.copy(timestamp = System.currentTimeMillis())) // Update timestamp

        // Batasi jumlah item
        while (historyList.size > MAX_HISTORY_ITEMS) {
            historyList.removeAt(historyList.size - 1) // Hapus item terlama (paling bawah)
        }

        saveHistory(context, historyList)
        Log.d(TAG, "Item riwayat ditambahkan/diperbarui: ${item.awb}, Total: ${historyList.size}")
    }

    fun removeHistoryItem(context: Context, itemToRemove: HistoryItem): MutableList<HistoryItem> {
        val historyList = getHistory(context)
        val removed = historyList.removeAll { it.awb == itemToRemove.awb && it.courierCode == itemToRemove.courierCode }
        if (removed) {
            saveHistory(context, historyList)
            Log.d(TAG, "Item riwayat dihapus: ${itemToRemove.awb}, Sisa: ${historyList.size}")
        }
        return historyList
    }

    private fun saveHistory(context: Context, historyList: List<HistoryItem>) {
        val prefs = getSharedPreferences(context)
        val gson = Gson()
        val json = gson.toJson(historyList)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit().remove(KEY_HISTORY).apply()
        Log.d(TAG, "Semua riwayat dihapus.")
    }
}
