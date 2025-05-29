package com.efzyn.cekresiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.HistoryItem

// Deklarasi kelas HistoryAdapter
class HistoryAdapter(
    // Constructor menerima:
    // 1. historyList
    private var historyList: MutableList<HistoryItem>,
    // 2. onItemClick
    private val onItemClick: (HistoryItem) -> Unit,
    // 3. onDeleteClick
    private val onDeleteClick: (HistoryItem, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    /**
     * Fungsi untuk mengupdate data di dalam adapter.
     */
    fun updateData(newHistoryList: List<HistoryItem>) {
        this.historyList.clear() // Bersihkan data lama
        // Tambahkan data baru dan urutkan berdasarkan timestamp (terbaru dulu)
        this.historyList.addAll(newHistoryList.sortedByDescending { it.timestamp })
        notifyDataSetChanged() // Beritahu adapter bahwa seluruh dataset berubah
    }

    /**
     * Fungsi untuk menghapus item dari list pada posisi tertentu.
     */
    fun removeItem(position: Int) {
        // Pastikan posisi valid sebelum mencoba menghapus
        if (position >= 0 && position < historyList.size) {
            historyList.removeAt(position) // Hapus item dari list data
            notifyItemRemoved(position)    // Beritahu adapter bahwa item di posisi ini dihapus (untuk animasi)
        }
    }

    /**
     * Dipanggil ketika RecyclerView perlu membuat ViewHolder baru untuk menampilkan item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Meng-inflate layout XML (R.layout.item_history) untuk satu item riwayat
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        // Membuat dan mengembalikan instance HistoryViewHolder dengan view yang sudah di-inflate
        return HistoryViewHolder(view)
    }

    /**
     * Dipanggil oleh RecyclerView untuk menampilkan data pada posisi tertentu.
     */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position] // Ambil data item riwayat pada posisi saat ini
        holder.bind(historyItem) // Panggil fungsi bind di ViewHolder untuk mengisi data ke view
    }

    /**
     * Mengembalikan jumlah total item dalam dataset yang dipegang oleh adapter.
     */
    override fun getItemCount(): Int = historyList.size

    /**
     * Inner class yang mendefinisikan ViewHolder untuk item riwayat.
     */
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Inisialisasi view-view dari layout item_history.xml
        private val tvAwb: TextView = itemView.findViewById(R.id.tvHistoryAwb)
        private val tvCourierName: TextView = itemView.findViewById(R.id.tvHistoryCourierName)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteHistory)

        /**
         * Fungsi untuk mengikat (bind) data dari objek HistoryItem ke view-view di dalam ViewHolder.
         * Juga mengatur listener klik untuk seluruh item dan tombol hapus.
         */
        fun bind(historyItem: HistoryItem) {
            // Set teks untuk AWB dan nama kurir
            tvAwb.text = historyItem.awb
            tvCourierName.text = historyItem.courierName

            // Set listener untuk klik pada seluruh item view
            itemView.setOnClickListener {
                // Dapatkan posisi adapter saat ini (untuk menghindari masalah jika data berubah)
                val position = adapterPosition
                // Pastikan posisi valid (bukan RecyclerView.NO_POSITION)
                if (position != RecyclerView.NO_POSITION) {
                    // Panggil lambda onItemClick yang diberikan dari MainActivity,
                    // kirim objek HistoryItem yang sesuai dengan posisi yang diklik.
                    onItemClick(historyList[position])
                }
            }

            // Set listener untuk klik pada tombol hapus
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Panggil lambda onDeleteClick yang diberikan dari MainActivity,
                    // kirim objek HistoryItem dan posisinya.
                    onDeleteClick(historyList[position], position)
                }
            }
        }
    }
}