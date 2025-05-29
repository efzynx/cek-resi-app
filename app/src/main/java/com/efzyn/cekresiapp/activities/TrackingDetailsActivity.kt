package com.efzyn.cekresiapp.ui.tracking // Ganti dengan package-mu

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.efzyn.cekresiapp.BuildConfig
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.model.TrackData
// import com.efzyn.cekresiapp.model.TrackingHistoryItem // Sudah ada di model.TrackingResponse
import com.efzyn.cekresiapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TrackingDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AWB = "extra_awb"
        const val EXTRA_COURIER_CODE = "extra_courier_code"
        const val EXTRA_COURIER_NAME = "extra_courier_name"
        private const val TAG = "TrackingDetails_UTS"
    }

    private lateinit var awbNumber: String
    private lateinit var courierCode: String
    private var courierNameDisplay: String? = null

    private lateinit var progressBarDetails: ProgressBar
    private lateinit var cardSummary: MaterialCardView
    private lateinit var tvAwbNumberDetail: TextView
    private lateinit var tvCourierNameDetail: TextView
    private lateinit var tvStatusDetail: TextView
    private lateinit var tvServiceDetail: TextView
    private lateinit var tvLastUpdateDesc: TextView
    private lateinit var tvShipperReceiver: TextView
    private lateinit var tvOriginDestination: TextView
    private lateinit var tvHistoryTitle: TextView
    private lateinit var rvTrackingHistory: RecyclerView
    private lateinit var tvErrorDetails: TextView
    private lateinit var historyAdapter: TrackingHistoryAdapter // Adapter untuk riwayat dari API

    private val apiKey = BuildConfig.BINDERBYTE_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_details)
        Log.d(TAG, "onCreate: Activity Dibuat")

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarDetails)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pelacakan"

        awbNumber = intent.getStringExtra(EXTRA_AWB) ?: ""
        courierCode = intent.getStringExtra(EXTRA_COURIER_CODE) ?: ""
        courierNameDisplay = intent.getStringExtra(EXTRA_COURIER_NAME)

        Log.d(TAG, "Intent Extras: AWB='$awbNumber', KurirKode='$courierCode', NamaKurir='$courierNameDisplay', APIKey='$apiKey'")

        if (awbNumber.isEmpty() || courierCode.isEmpty()) {
            Log.e(TAG, "AWB atau Kode Kurir kosong. Mengakhiri activity.")
            Toast.makeText(this, "AWB atau kode kurir tidak valid.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initializeViews()
        Log.d(TAG, "Views diinisialisasi.")
        setupRecyclerView()
        fetchTrackingDetails()
    }

    private fun initializeViews() {
        progressBarDetails = findViewById(R.id.progressBarDetails)
        cardSummary = findViewById(R.id.cardSummary)
        tvAwbNumberDetail = findViewById(R.id.tvAwbNumberDetail)
        tvCourierNameDetail = findViewById(R.id.tvCourierNameDetail)
        tvStatusDetail = findViewById(R.id.tvStatusDetail)
        tvServiceDetail = findViewById(R.id.tvServiceDetail)
        tvLastUpdateDesc = findViewById(R.id.tvLastUpdateDesc)
        tvShipperReceiver = findViewById(R.id.tvShipperReceiver)
        tvOriginDestination = findViewById(R.id.tvOriginDestination)
        tvHistoryTitle = findViewById(R.id.tvHistoryTitle)
        rvTrackingHistory = findViewById(R.id.rvTrackingHistory)
        tvErrorDetails = findViewById(R.id.tvErrorDetails)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        historyAdapter = TrackingHistoryAdapter(emptyList()) // Adapter untuk TrackingHistoryItem dari API
        rvTrackingHistory.layoutManager = LinearLayoutManager(this)
        rvTrackingHistory.adapter = historyAdapter
        Log.d(TAG, "RecyclerView Riwayat API disiapkan.")
    }

    private fun fetchTrackingDetails() {
        Log.d(TAG, "Memulai fetch detail pelacakan untuk AWB: $awbNumber, Kurir: $courierCode")
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.trackShipment(apiKey, courierCode, awbNumber)
                Log.d(TAG, "Respons API Detail: ${response.raw()}")
                if (response.isSuccessful && response.body()?.data != null) {
                    val trackData = response.body()!!.data!!
                    Log.d(TAG, "Data pelacakan diterima: $trackData")
                    displayTrackingData(trackData)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = response.body()?.message ?: response.message() ?: "Gagal melacak resi (Kode: ${response.code()})"
                    Log.e(TAG, "Gagal fetch detail. Kode: ${response.code()}, Msg: $errorMsg, ErrorBody: $errorBody")
                    showError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat fetch detail!", e)
                showError("Error Aplikasi: ${e.localizedMessage ?: "Terjadi kesalahan"}")
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressBarDetails.visibility = View.VISIBLE
            cardSummary.visibility = View.GONE
            tvHistoryTitle.visibility = View.GONE
            rvTrackingHistory.visibility = View.GONE
            tvErrorDetails.visibility = View.GONE
        } else {
            progressBarDetails.visibility = View.GONE
        }
    }

    private fun displayTrackingData(data: TrackData) {
        Log.d(TAG, "Menampilkan data pelacakan...")
        cardSummary.visibility = View.VISIBLE
        tvAwbNumberDetail.text = "No. Resi: ${data.summary.awb}"

        val finalCourierName = courierNameDisplay ?: data.summary.courier // Prioritaskan nama dari intent
        tvCourierNameDetail.text = "Kurir: $finalCourierName"

        tvStatusDetail.text = "Status: ${data.summary.status}"
        tvServiceDetail.text = if (data.summary.service.isNullOrEmpty()) "Layanan: -" else "Layanan: ${data.summary.service}"

        val lastUpdateText = "${data.summary.desc?.takeIf { it.isNotBlank() } ?: "Status terakhir"} (${data.summary.date})"
        tvLastUpdateDesc.text = "Update: $lastUpdateText"

        val shipperText = data.detail.shipper?.takeIf { it.isNotBlank() } ?: "-"
        val receiverText = data.detail.receiver?.takeIf { it.isNotBlank() } ?: "-"
        tvShipperReceiver.text = "Pengirim: $shipperText \nKepada: $receiverText"

        val originText = data.detail.origin?.takeIf { it.isNotBlank() } ?: "-"
        val destinationText = data.detail.destination?.takeIf { it.isNotBlank() } ?: "-"
        tvOriginDestination.text = "Asal: $originText \nTujuan: $destinationText"

        if (data.history.isNotEmpty()) {
            Log.d(TAG, "Menampilkan riwayat API: ${data.history.size} item.")
            tvHistoryTitle.visibility = View.VISIBLE
            rvTrackingHistory.visibility = View.VISIBLE
            historyAdapter.updateData(data.history)
        } else {
            Log.d(TAG, "Tidak ada riwayat dari API.")
            tvHistoryTitle.visibility = View.GONE
            rvTrackingHistory.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        Log.e(TAG, "Menampilkan error: $message")
        cardSummary.visibility = View.GONE
        tvHistoryTitle.visibility = View.GONE
        rvTrackingHistory.visibility = View.GONE
        tvErrorDetails.visibility = View.VISIBLE
        tvErrorDetails.text = message
        if (message.isNotBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}
