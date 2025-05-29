package com.efzyn.cekresiapp.adapters

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
    private var selectedPosition = RecyclerView.NO_POSITION // Menyimpan posisi item yang dipilih

    fun setData(newCouriers: List<Courier>) {
        this.couriersFull = newCouriers
        this.couriersFiltered = ArrayList(newCouriers)
        // Reset selectedPosition saat data baru dimuat jika perlu,
        // atau biarkan jika ingin mempertahankan seleksi antar pencarian/refresh
        // selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    // Metode untuk mengupdate posisi yang dipilih
    fun setSelectedPosition(position: Int) {
        val previousSelectedPosition = selectedPosition
        selectedPosition = position
        // Refresh item yang sebelumnya dipilih (jika ada) untuk menghilangkan highlight
        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition)
        }
        // Refresh item yang baru dipilih untuk menambahkan highlight
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
        holder.bind(courier, position == selectedPosition) // Kirim status terpilih ke binder
    }

    override fun getItemCount(): Int = couriersFiltered.size

    inner class CourierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourierName: TextView = itemView.findViewById(R.id.tvCourierName)
        // Jika root item_courier.xml adalah CardView atau MaterialCardView, ambil referensinya
        private val itemCardView: MaterialCardView = itemView as MaterialCardView // Sesuaikan dengan root view Anda

        fun bind(courier: Courier, isSelected: Boolean) {
            tvCourierName.text = courier.name

            // Ubah background atau properti lain jika item terpilih
            if (isSelected) {
                // Gunakan warna dari resources jika memungkinkan untuk tema yang lebih baik
                itemCardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.selected_courier_background))
                // Atau warna hardcode jika sederhana:
                // itemCardView.setCardBackgroundColor(Color.LTGRAY)
                tvCourierName.setTextColor(ContextCompat.getColor(itemView.context, R.color.selected_courier_text))
            } else {
                // Kembali ke warna default
                itemCardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.default_courier_background))
                // Atau warna hardcode:
                // itemCardView.setCardBackgroundColor(Color.WHITE)
                tvCourierName.setTextColor(ContextCompat.getColor(itemView.context, R.color.default_courier_text))
            }

            itemView.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick(couriersFiltered[currentPosition])
                    // Panggil setSelectedPosition dari MainActivity setelah onItemClick
                    // agar MainActivity yang mengontrol state terpilih
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""
                val filteredList = if (charString.isEmpty()) {
                    // Jika query kosong, reset juga selectedPosition jika diinginkan
                    // selectedPosition = RecyclerView.NO_POSITION (opsional, tergantung behavior yang diinginkan)
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
                val oldSelectedCourierCode = if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < oldFilteredListSize) {
                    couriersFiltered[selectedPosition].code
                } else {
                    null
                }

                couriersFiltered = results?.values as? MutableList<Courier> ?: ArrayList()

                // Setelah filtering, coba pertahankan seleksi jika item yang dipilih masih ada di daftar baru
                if (oldSelectedCourierCode != null) {
                    val newSelectedPosition = couriersFiltered.indexOfFirst { it.code == oldSelectedCourierCode }
                    if (newSelectedPosition != -1) {
                        selectedPosition = newSelectedPosition
                    } else {
                        selectedPosition = RecyclerView.NO_POSITION // Item yang dipilih hilang setelah filter
                    }
                } else {
                    selectedPosition = RecyclerView.NO_POSITION
                }
                notifyDataSetChanged()
            }
        }
    }
}
