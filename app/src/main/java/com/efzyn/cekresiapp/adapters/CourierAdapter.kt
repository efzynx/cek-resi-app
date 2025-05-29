package com.efzyn.cekresiapp.adapters // Ganti dengan package-mu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.Courier
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class CourierAdapter(
    private var couriersFull: List<Courier>, // Daftar asli untuk filter
    private val onItemClick: (Courier) -> Unit
) : RecyclerView.Adapter<CourierAdapter.CourierViewHolder>(), Filterable {

    var couriersFiltered: MutableList<Courier> = ArrayList(couriersFull)
    private var selectedPosition = RecyclerView.NO_POSITION

    fun setData(newCouriers: List<Courier>) {
        this.couriersFull = newCouriers
        this.couriersFiltered = ArrayList(newCouriers)
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val previousSelectedPosition = selectedPosition
        selectedPosition = position
        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_courier, parent, false)
        return CourierViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourierViewHolder, position: Int) {
        val courier = couriersFiltered[position]
        holder.bind(courier, position == selectedPosition)
    }

    override fun getItemCount(): Int = couriersFiltered.size

    inner class CourierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourierName: TextView = itemView.findViewById(R.id.tvCourierName)
        private val itemCardView: MaterialCardView = itemView as MaterialCardView

        fun bind(courier: Courier, isSelected: Boolean) {
            tvCourierName.text = courier.name

            val context = itemView.context
            // Mengambil nilai dp dari dimens.xml jika ada, atau hardcode
            val strokeWidthSelectedPx = (2 * context.resources.displayMetrics.density).toInt() // 2dp
            val strokeWidthDefaultPx = (1 * context.resources.displayMetrics.density).toInt()  // 1dp

            if (isSelected) {
                itemCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.selected_courier_background_tint))
                itemCardView.strokeColor = ContextCompat.getColor(context, R.color.selected_courier_outline_color)
                itemCardView.strokeWidth = strokeWidthSelectedPx // Atur ketebalan outline saat dipilih
                tvCourierName.setTextColor(ContextCompat.getColor(context, R.color.selected_courier_text))
            } else {
                itemCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.default_courier_background))
                itemCardView.strokeColor = ContextCompat.getColor(context, R.color.default_courier_outline_color) // Outline default
                itemCardView.strokeWidth = strokeWidthDefaultPx // Outline tipis untuk default
                tvCourierName.setTextColor(ContextCompat.getColor(context, R.color.default_courier_text))
            }

            itemView.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(couriersFiltered[currentPosition])
                    // Pemanggilan setSelectedPosition akan dilakukan oleh MainActivity
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""
                val filteredList = if (charString.isEmpty()) {
                    ArrayList(couriersFull)
                } else {
                    couriersFull.filter {
                        it.name.lowercase(Locale.getDefault()).contains(charString) ||
                                it.code.lowercase(Locale.getDefault()).contains(charString)
                    }.toMutableList()
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val oldFilteredListSize = couriersFiltered.size
                val oldSelectedCourierCode = if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < oldFilteredListSize && couriersFiltered.isNotEmpty()) {
                    // Tambahkan pengecekan couriersFiltered tidak kosong
                    couriersFiltered[selectedPosition].code
                } else {
                    null
                }

                couriersFiltered = results?.values as? MutableList<Courier> ?: ArrayList()

                if (oldSelectedCourierCode != null) {
                    val newSelectedPosition = couriersFiltered.indexOfFirst { it.code == oldSelectedCourierCode }
                    selectedPosition = if (newSelectedPosition != -1) {
                        newSelectedPosition
                    } else {
                        RecyclerView.NO_POSITION
                    }
                } else {
                    selectedPosition = RecyclerView.NO_POSITION
                }
                notifyDataSetChanged()
            }
        }
    }
}
