package com.efzyn.cekresiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.HistoryItem

class HistoryAdapter(
    private var historyList: MutableList<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryItem, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun updateData(newHistoryList: List<HistoryItem>) {
        this.historyList.clear()
        this.historyList.addAll(newHistoryList.sortedByDescending { it.timestamp }) // Urutkan berdasarkan yang terbaru
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < historyList.size) {
            historyList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.bind(historyItem)
    }

    override fun getItemCount(): Int = historyList.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAwb: TextView = itemView.findViewById(R.id.tvHistoryAwb)
        private val tvCourierName: TextView = itemView.findViewById(R.id.tvHistoryCourierName)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteHistory)

        fun bind(historyItem: HistoryItem) {
            tvAwb.text = historyItem.awb
            tvCourierName.text = historyItem.courierName

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(historyList[position])
                }
            }
            btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(historyList[position], position)
                }
            }
        }
    }
}
