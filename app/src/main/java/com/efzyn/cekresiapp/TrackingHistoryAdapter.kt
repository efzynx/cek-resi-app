package com.efzyn.cekresiapp.ui.tracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.TrackingHistory

class TrackingHistoryAdapter(
    private var historyItems: List<TrackingHistory>
) : RecyclerView.Adapter<TrackingHistoryAdapter.HistoryViewHolder>() {

    fun updateData(newHistory: List<TrackingHistory>) {
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

        fun bind(history: TrackingHistory) {
            tvDate.text = history.date // Format tanggal jika perlu
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