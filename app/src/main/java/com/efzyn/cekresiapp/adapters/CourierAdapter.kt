package com.efzyn.cekresiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable // Impor Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.Courier
import java.util.Locale // Impor Locale

class CourierAdapter(
    private var couriersFull: List<Courier>, // Simpan daftar asli untuk filter
    private val onItemClick: (Courier) -> Unit
) : RecyclerView.Adapter<CourierAdapter.CourierViewHolder>(), Filterable { // Implementasi Filterable

    // Daftar yang akan ditampilkan (bisa hasil filter)
    private var couriersFiltered: MutableList<Courier> = couriersFull.toMutableList()

    fun setData(newCouriers: List<Courier>) {
        this.couriersFull = newCouriers
        this.couriersFiltered = newCouriers.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_courier, parent, false)
        return CourierViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourierViewHolder, position: Int) {
        val courier = couriersFiltered[position] // Gunakan couriersFiltered
        holder.bind(courier)
    }

    override fun getItemCount(): Int = couriersFiltered.size // Gunakan couriersFiltered

    inner class CourierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourierName: TextView = itemView.findViewById(R.id.tvCourierName)

        fun bind(courier: Courier) {
            tvCourierName.text = courier.name
            itemView.setOnClickListener { onItemClick(courier) }
        }
    }

    // Implementasi Filterable
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString()?.lowercase(Locale.getDefault()) ?: ""
                val filteredList = if (charString.isEmpty()) {
                    couriersFull.toMutableList()
                } else {
                    couriersFull.filter {
                        it.name.lowercase(Locale.getDefault()).contains(charString)
                        // Kamu juga bisa filter berdasarkan courier.code jika mau
                        // || it.code.lowercase(Locale.getDefault()).contains(charString)
                    }.toMutableList()
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                couriersFiltered = results?.values as? MutableList<Courier> ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }
}