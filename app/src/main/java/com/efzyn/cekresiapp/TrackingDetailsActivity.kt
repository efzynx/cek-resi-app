package com.efzyn.cekresiapp.ui.tracking // Pastikan package sesuai dengan proyekmu

import android.os.Bundle
import android.util.Log // Import Log
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
import com.efzyn.cekresiapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TrackingDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AWB = "extra_awb"
        const val EXTRA_COURIER_CODE = "extra_courier_code"
        const val EXTRA_COURIER_NAME = "extra_courier_name" // Opsional
        private const val TAG = "TrackingDetails" // Tag untuk Logcat
    }

    private lateinit var awbNumber: String
    private lateinit var courierCode: String
    private var courierName: String? = null // Opsional

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
    private lateinit var historyAdapter: TrackingHistoryAdapter

    private val apiKey = BuildConfig.BINDERBYTE_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: Memasuki onCreate SEBELUM setContentView") // Log paling awal
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: super.onCreate() selesai")
        setContentView(R.layout.activity_tracking_details)
        Log.d(TAG, "onCreate: setContentView() selesai, Activity Dibuat") // Log setelah setContentView
//        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_details)
        Log.d(TAG, "onCreate: Activity Dibuat")

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarDetails)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        awbNumber = intent.getStringExtra(EXTRA_AWB) ?: ""
        courierCode = intent.getStringExtra(EXTRA_COURIER_CODE) ?: ""
        courierName = intent.getStringExtra(EXTRA_COURIER_NAME)

        Log.d(TAG, "onCreate: AWB Diterima: '$awbNumber'")
        Log.d(TAG, "onCreate: Kode Kurir Diterima: '$courierCode'")
        Log.d(TAG, "onCreate: Nama Kurir Diterima: '$courierName'")
        Log.d(TAG, "onCreate: API Key: '$apiKey'")


        if (awbNumber.isEmpty() || courierCode.isEmpty()) {
            Log.e(TAG, "onCreate: AWB atau Kode Kurir KOSONG! Mengakhiri activity.")
            Toast.makeText(this, "AWB atau kode kurir tidak valid.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize Views
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
        Log.d(TAG, "onCreate: Semua views diinisialisasi.")

        setupRecyclerView()
        fetchTrackingDetails()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Tombol Home ditekan")
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        historyAdapter = TrackingHistoryAdapter(emptyList())
        rvTrackingHistory.layoutManager = LinearLayoutManager(this)
        rvTrackingHistory.adapter = historyAdapter
        Log.d(TAG, "setupRecyclerView: RecyclerView dan Adapter disiapkan.")
    }

    private fun fetchTrackingDetails() {
        Log.d(TAG, "fetchTrackingDetails: Memulai pengambilan detail pelacakan...")
        Log.d(TAG, "fetchTrackingDetails: Parameter: AWB='$awbNumber', Kurir='$courierCode'")

        progressBarDetails.visibility = View.VISIBLE
        cardSummary.visibility = View.GONE
        tvHistoryTitle.visibility = View.GONE
        rvTrackingHistory.visibility = View.GONE
        tvErrorDetails.visibility = View.GONE

        lifecycleScope.launch {
            Log.d(TAG, "fetchTrackingDetails: Coroutine diluncurkan.")
            try {
                val response = RetrofitClient.instance.trackShipment(apiKey, courierCode, awbNumber)
                Log.d(TAG, "fetchTrackingDetails: Panggilan API selesai.")
                Log.d(TAG, "fetchTrackingDetails: Respons API Mentah: ${response.raw()}")
                Log.d(TAG, "fetchTrackingDetails: Kode Respons: ${response.code()}, Pesan: ${response.message()}")
                Log.d(TAG, "fetchTrackingDetails: Body Respons: ${response.body()?.toString()}")


                if (response.isSuccessful) {
                    Log.d(TAG, "fetchTrackingDetails: Respons API SUKSES (isSuccessful=true).")
                    val responseBody = response.body()
                    if (responseBody?.data != null) {
                        Log.d(TAG, "fetchTrackingDetails: Data dalam body TIDAK NULL.")
                        val trackData = responseBody.data!! // Kita sudah cek null
                        Log.d(TAG, "fetchTrackingDetails: TrackData berhasil diparsing: $trackData")
                        displayTrackingData(trackData)
                    } else {
                        Log.e(TAG, "fetchTrackingDetails: Respons sukses TAPI responseBody atau responseBody.data adalah NULL.")
                        Log.e(TAG, "fetchTrackingDetails: responseBody: $responseBody")
                        showError(responseBody?.message ?: "Data tidak ditemukan dalam respons.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "fetchTrackingDetails: Respons API GAGAL (isSuccessful=false). Kode: ${response.code()}, ErrorBody: $errorBody")
                    val errorMsg = response.body()?.message ?: response.message() ?: "Gagal melacak resi (Error Server)."
                    showError("Gagal: $errorMsg (Kode: ${response.code()})")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchTrackingDetails: Exception terjadi!", e)
                showError("Error Aplikasi: ${e.localizedMessage ?: "Terjadi kesalahan tidak diketahui"}")
                // e.printStackTrace() // Log.e sudah mencakup stack trace
            } finally {
                Log.d(TAG, "fetchTrackingDetails: Blok finally dieksekusi, menyembunyikan ProgressBar.")
                progressBarDetails.visibility = View.GONE
            }
        }
    }

    private fun displayTrackingData(data: TrackData) {
        Log.d(TAG, "displayTrackingData: Mulai menampilkan data: $data")
        cardSummary.visibility = View.VISIBLE
        tvAwbNumberDetail.text = "AWB: ${data.summary.awb}"

        val displayCourierName = courierName ?: data.summary.courier
        tvCourierNameDetail.text = "Kurir: $displayCourierName"
        Log.d(TAG, "displayTrackingData: Nama Kurir Ditampilkan: $displayCourierName")

        tvStatusDetail.text = "Status: ${data.summary.status}"
        tvServiceDetail.text = if (data.summary.service.isNullOrEmpty()) "Layanan: -" else "Layanan: ${data.summary.service}"

        val lastUpdateDescription = "${data.summary.desc ?: "Tidak ada deskripsi"} (${data.summary.date})"
        tvLastUpdateDesc.text = "Update Terakhir: $lastUpdateDescription"

        val shipperReceiverText = "Pengirim: ${data.detail.shipper?.takeIf { it.isNotBlank() } ?: "-"} \nKepada: ${data.detail.receiver?.takeIf { it.isNotBlank() } ?: "-"}"
        tvShipperReceiver.text = shipperReceiverText

        val originDestText = "Asal: ${data.detail.origin?.takeIf { it.isNotBlank() } ?: "-"} \nTujuan: ${data.detail.destination?.takeIf { it.isNotBlank() } ?: "-"}"
        tvOriginDestination.text = originDestText
        Log.d(TAG, "displayTrackingData: Detail summary ditampilkan.")

        if (data.history.isNotEmpty()) {
            Log.d(TAG, "displayTrackingData: Riwayat ditemukan, jumlah: ${data.history.size}")
            tvHistoryTitle.visibility = View.VISIBLE
            rvTrackingHistory.visibility = View.VISIBLE
            historyAdapter.updateData(data.history)
        } else {
            Log.d(TAG, "displayTrackingData: Tidak ada riwayat.")
            tvHistoryTitle.visibility = View.GONE
            rvTrackingHistory.visibility = View.GONE
        }
        Log.d(TAG, "displayTrackingData: Selesai menampilkan data.")
    }

    private fun showError(message: String) {
        Log.e(TAG, "showError: Menampilkan pesan error: '$message'")
        cardSummary.visibility = View.GONE
        tvHistoryTitle.visibility = View.GONE
        rvTrackingHistory.visibility = View.GONE
        tvErrorDetails.visibility = View.VISIBLE
        tvErrorDetails.text = message
        if (message.isNotBlank()) { // Hanya tampilkan Toast jika ada pesan
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Terjadi kesalahan.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "showError: Pesan error kosong, menampilkan Toast default.")
        }
    }
}
