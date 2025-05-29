package com.efzyn.cekresiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.TrackingHistoryItem

// Constructor hanya menerima daftar item riwayat dari API
class TrackingHistoryAdapter(
    private var historyItems: List<TrackingHistoryItem>
) : RecyclerView.Adapter<TrackingHistoryAdapter.HistoryViewHolder>() {

    /**
     * Mengupdate data riwayat di adapter dan memberitahu RecyclerView untuk me-refresh tampilannya.
     * @param newHistory List baru dari TrackingHistoryItem.
     */
    fun updateData(newHistory: List<TrackingHistoryItem>) {
        this.historyItems = newHistory // Ganti list lama dengan yang baru
        notifyDataSetChanged() // Beritahu RecyclerView bahwa seluruh data berubah
    }

    /**
     * Membuat ViewHolder baru.
     * Meng-inflate layout R.layout.item_tracking_history untuk setiap item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tracking_history, parent, false)
        return HistoryViewHolder(view)
    }

    /**
     * Mengikat data dari objek TrackingHistoryItem ke view di dalam ViewHolder.
     */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyItems[position] // Ambil data pada posisi saat ini
        holder.bind(historyItem) // Panggil fungsi bind di ViewHolder
    }

    /**
     * Mengembalikan jumlah total item riwayat.
     */
    override fun getItemCount(): Int = historyItems.size

    /**
     * ViewHolder untuk setiap item riwayat perjalanan paket.
     * Menyimpan referensi ke TextView untuk tanggal, deskripsi, dan lokasi.
     */
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvHistoryDescription)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvHistoryLocation)

        /**
         * Mengisi data dari objek TrackingHistoryItem ke TextViews.
         * Bagian yang sedikit lebih "rumit" di sini adalah logika untuk menampilkan
         * atau menyembunyikan tvLocation berdasarkan apakah data lokasi ada atau tidak.
         */
        fun bind(history: TrackingHistoryItem) {
            tvDate.text = history.date         // Tampilkan tanggal
            tvDescription.text = history.desc  // Tampilkan deskripsi status

            // Cek apakah data lokasi ada dan tidak kosong
            if (history.location.isNullOrEmpty()) {
                tvLocation.visibility = View.GONE // Sembunyikan TextView lokasi jika data kosong
            } else {
                tvLocation.visibility = View.VISIBLE // Tampilkan TextView lokasi
                tvLocation.text = "Lokasi: ${history.location}" // Set teks lokasi
            }
        }
    }
}