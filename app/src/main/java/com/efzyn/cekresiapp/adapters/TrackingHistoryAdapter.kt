package com.efzyn.cekresiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.TrackingHistoryItem // Menggunakan model dari API

class TrackingHistoryAdapter(
    private var historyItems: List<TrackingHistoryItem>
) : RecyclerView.Adapter<TrackingHistoryAdapter.HistoryViewHolder>() {

    fun updateData(newHistory: List<TrackingHistoryItem>) {
        this.historyItems = newHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyItems[position]
        holder.bind(historyItem)
    }

    override fun getItemCount(): Int = historyItems.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvHistoryDescription)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvHistoryLocation)

        fun bind(history: TrackingHistoryItem) {
            tvDate.text = history.date // Anda bisa memformat tanggal ini jika mau
            tvDescription.text = history.desc
            if (history.location.isNullOrEmpty()) {
                tvLocation.visibility = View.GONE
            } else {
                tvLocation.visibility = View.VISIBLE
                tvLocation.text = "Lokasi: ${history.location}"
            }
        }
    }
}
