package com.efzyn.cekresiapp.ui.tracking

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
import com.efzyn.cekresiapp.adapters.TrackingHistoryAdapter
import com.efzyn.cekresiapp.model.TrackData
import com.efzyn.cekresiapp.network.RetrofitClient
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    private lateinit var historyAdapter: TrackingHistoryAdapter

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
        tvErrorDetails = findViewById(R.id.tvErrorDetails) // Tetap inisialisasi, tapi mungkin tidak dipakai
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed() // Gunakan ini untuk API 33+
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        historyAdapter = TrackingHistoryAdapter(emptyList())
        rvTrackingHistory.layoutManager = LinearLayoutManager(this)
        rvTrackingHistory.adapter = historyAdapter
        Log.d(TAG, "RecyclerView Riwayat API disiapkan.")
    }

    private fun fetchTrackingDetails() {
        Log.d(TAG, "Memulai fetch detail pelacakan untuk AWB: $awbNumber, Kurir: $courierCode")
        setLoadingState(true)
        tvErrorDetails.visibility = View.GONE // Sembunyikan TextView error default

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.trackShipment(apiKey, courierCode, awbNumber)
                Log.d(TAG, "Respons API Detail Mentah: ${response.raw()}") // Log respons mentah
                if (response.isSuccessful && response.body()?.data != null) {
                    val trackData = response.body()!!.data!!
                    Log.d(TAG, "Data pelacakan diterima: $trackData")
                    displayTrackingData(trackData)
                } else {
                    // --- MODIFIKASI PENANGANAN ERROR ---
                    var specificErrorMessage = "Gagal melacak resi." // Pesan default
                    val errorBodyString = response.errorBody()?.string()
                    if (!errorBodyString.isNullOrEmpty()) {
                        Log.e(TAG, "Error Body: $errorBodyString")
                        try {
                            // Coba parsing JSON dari error body
                            val jsonError = JSONObject(errorBodyString)
                            specificErrorMessage = jsonError.optString("message", specificErrorMessage) // Ambil field "message"
                        } catch (e: Exception) {
                            Log.e(TAG, "Gagal parsing JSON error body: $errorBodyString", e)
                            // Jika parsing gagal, gunakan pesan dari kode status jika ada, atau pesan default
                            specificErrorMessage = when (response.code()) {
                                400 -> "Nomor resi atau kurir tidak valid. Periksa kembali input Anda."
                                401, 403 -> "Autentikasi gagal. Periksa API Key Anda."
                                404 -> "Data tidak ditemukan. Nomor resi mungkin salah atau belum terdaftar."
                                500, 502, 503, 504 -> "Server sedang bermasalah. Coba lagi nanti."
                                else -> specificErrorMessage // Gunakan pesan default jika kode tidak dikenal
                            }
                        }
                    } else if (response.body()?.message != null && response.body()?.message?.isNotEmpty() == true ) {
                        // Jika error body kosong tapi ada message di body sukses
                        specificErrorMessage = response.body()!!.message!!
                    } else if (response.message().isNotEmpty()){
                        specificErrorMessage = response.message()
                    }

                    Log.e(TAG, "Gagal fetch detail. Kode: ${response.code()}, Pesan API: '$specificErrorMessage'")
                    showErrorDialog("Error ${response.code()}", specificErrorMessage)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat fetch detail!", e)
                showErrorDialog("Error Aplikasi", "Terjadi kesalahan: ${e.localizedMessage ?: "Tidak diketahui"}")
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
        val finalCourierName = courierNameDisplay ?: data.summary.courier
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

    // Ganti showError dengan showErrorDialog
    private fun showErrorDialog(title: String, message: String) {
        Log.e(TAG, "showErrorDialog: Title: '$title', Message: '$message'")
        if (isFinishing || isDestroyed) {
            // Hindari menampilkan dialog jika activity sudah tidak aktif
            Log.w(TAG, "Activity is finishing or destroyed, not showing error dialog.")
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(R.drawable.ic_warning_red)
            .show()
    }
}
