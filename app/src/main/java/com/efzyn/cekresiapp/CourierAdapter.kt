package com.efzyn.cekresiapp.ui.main // Ganti com.example.cekresi_uts dengan package-mu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.Courier

class CourierAdapter(
    private var couriers: List<Courier>,
    private val onItemClick: (Courier) -> Unit
) : RecyclerView.Adapter<CourierAdapter.CourierViewHolder>() {

    fun updateData(newCouriers: List<Courier>) {
        this.couriers = newCouriers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_courier, parent, false)
        return CourierViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourierViewHolder, position: Int) {
        val courier = couriers[position]
        holder.bind(courier)
    }

    override fun getItemCount(): Int = couriers.size

    inner class CourierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourierName: TextView = itemView.findViewById(R.id.tvCourierName)
        // private val ivCourierLogo: ImageView = itemView.findViewById(R.id.ivCourierLogo) // Jika ada logo

        fun bind(courier: Courier) {
            tvCourierName.text = courier.name
            // Jika ada URL logo dan pakai Glide:
            // Glide.with(itemView.context).load(courier.logoUrl).into(ivCourierLogo)
            itemView.setOnClickListener { onItemClick(courier) }
        }
    }
}