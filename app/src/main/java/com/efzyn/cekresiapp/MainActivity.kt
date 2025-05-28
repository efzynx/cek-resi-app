package com.efzyn.cekresiapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.efzyn.cekresiapp.BuildConfig
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.Courier
import com.efzyn.cekresiapp.network.RetrofitClient
import com.efzyn.cekresiapp.ui.tracking.TrackingDetailsActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var rvCouriers: RecyclerView
    private lateinit var courierAdapter: CourierAdapter
    private lateinit var progressBarCouriers: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout // [cite: 3]

    private lateinit var tvSelectedCourier: TextView
    private lateinit var etAwbNumber: EditText
    private lateinit var tilAwbNumber: TextInputLayout
    private lateinit var btnTrack: Button

    private var selectedCourier: Courier? = null
    private val apiKey = BuildConfig.BINDERBYTE_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Views
        rvCouriers = findViewById(R.id.rvCouriers)
        progressBarCouriers = findViewById(R.id.progressBarCouriers)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        tvSelectedCourier = findViewById(R.id.tvSelectedCourier)
        etAwbNumber = findViewById(R.id.etAwbNumber)
        tilAwbNumber = findViewById(R.id.tilAwbNumber)
        btnTrack = findViewById(R.id.btnTrack)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup Swipe to Refresh [cite: 3]
        swipeRefreshLayout.setOnRefreshListener {
            fetchCouriers()
        }

        // Fetch initial data
        fetchCouriers()

        // Setup Track Button
        btnTrack.setOnClickListener {
            performTracking()
        }
        updateTrackButtonState()
    }

    private fun setupRecyclerView() {
        courierAdapter = CourierAdapter(emptyList()) { courier ->
            // Handle courier item click
            selectedCourier = courier
            tvSelectedCourier.text = "Kurir: ${courier.name}"
            tilAwbNumber.visibility = View.VISIBLE // Show AWB input
            updateTrackButtonState()
            Toast.makeText(this, "Kurir dipilih: ${courier.name}", Toast.LENGTH_SHORT).show()
        }
        rvCouriers.layoutManager = LinearLayoutManager(this)
        rvCouriers.adapter = courierAdapter
    }

    private fun fetchCouriers() {
        progressBarCouriers.visibility = View.VISIBLE
        swipeRefreshLayout.isRefreshing = true // Show refresh indicator [cite: 3]

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getListCourier(apiKey)
                if (response.isSuccessful) {
                    response.body()?.let {
                        courierAdapter.updateData(it)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal memuat daftar kurir: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            } finally {
                progressBarCouriers.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false // Hide refresh indicator [cite: 3]
            }
        }
    }

    private fun updateTrackButtonState() {
        btnTrack.isEnabled = selectedCourier != null && etAwbNumber.text.toString().isNotBlank()
    }

    private fun performTracking() {
        val awbNumber = etAwbNumber.text.toString().trim()

        if (selectedCourier == null) {
            Toast.makeText(this, "Silakan pilih kurir terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        if (awbNumber.isEmpty()) {
            tilAwbNumber.error = "Nomor resi tidak boleh kosong"
            return
        } else {
            tilAwbNumber.error = null
        }

        val intent = Intent(this, TrackingDetailsActivity::class.java).apply {
            putExtra(TrackingDetailsActivity.EXTRA_AWB, awbNumber)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_CODE, selectedCourier!!.code)
            // Jika ingin mengirim nama kurir juga untuk ditampilkan di detail:
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_NAME, selectedCourier!!.name)
        }
        startActivity(intent)
    }

    // Panggil updateTrackButtonState saat teks di EditText berubah
    override fun onResume() {
        super.onResume()
        etAwbNumber.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTrackButtonState()
                if (s.toString().isNotEmpty()) tilAwbNumber.error = null
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
}