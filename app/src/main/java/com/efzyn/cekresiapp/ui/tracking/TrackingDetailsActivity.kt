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
import java.net.UnknownHostException

class TrackingDetailsActivity : AppCompatActivity() {

    // Konstanta untuk Intent Extras dan Logging
    companion object {
        const val EXTRA_AWB = "extra_awb"
        const val EXTRA_COURIER_CODE = "extra_courier_code"
        const val EXTRA_COURIER_NAME = "extra_courier_name" // Nama kurir yang ditampilkan, bisa dari MainActivity
        private const val TAG = "TrackingDetails_UTS"
    }

    // Data yang diterima dari Intent
    private lateinit var awbNumber: String
    private lateinit var courierCode: String
    private var courierNameDisplay: String? = null

    // Views
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
    private lateinit var tvErrorDetails: TextView // Untuk menampilkan pesan error jika dialog tidak digunakan
    private lateinit var historyAdapter: TrackingHistoryAdapter

    // Lainnya
    private val apiKey = BuildConfig.BINDERBYTE_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: MULAI <---")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_details)
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: setelah setContentView()")

        setupToolbar()
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: setelah setupToolbar()")

        if (!getIntentData()) {
            // Jika data intent tidak valid, activity akan di-finish dari dalam getIntentData()
            return
        }
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: setelah getIntentData()")

        initializeViews()
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: setelah initializeViews()")

        setupRecyclerView()
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: setelah setupRecyclerView()")

        fetchTrackingDetails()
        Log.d(TAG, "--> TrackingDetailsActivity onCreate: SELESAI <---")
    }

    /**
     * Mengatur Toolbar untuk activity ini.
     */
    private fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbarDetails)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pelacakan Resi" // Judul yang lebih spesifik
    }

    /**
     * Mengambil dan memvalidasi data yang dikirim melalui Intent.
     * @return true jika data valid, false jika tidak dan activity akan di-finish.
     */
    private fun getIntentData(): Boolean {
        awbNumber = intent.getStringExtra(EXTRA_AWB) ?: ""
        courierCode = intent.getStringExtra(EXTRA_COURIER_CODE) ?: ""
        courierNameDisplay = intent.getStringExtra(EXTRA_COURIER_NAME)

        Log.d(TAG, "getIntentData: AWB='$awbNumber', KodeKurir='$courierCode', NamaKurirTampilan='$courierNameDisplay'")

        if (awbNumber.isEmpty() || courierCode.isEmpty()) {
            Log.e(TAG, "getIntentData: AWB atau Kode Kurir KOSONG! Mengakhiri activity.")
            Toast.makeText(this, "Data AWB atau kurir tidak valid.", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        return true
    }

    /**
     * Menginisialisasi semua view dari layout XML.
     */
    private fun initializeViews() {
        Log.d(TAG, "--> initializeViews: MULAI <---")
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
        tvErrorDetails = findViewById(R.id.tvErrorDetails) // Meskipun mungkin tidak selalu dipakai jika dialog digunakan
        Log.d(TAG, "--> initializeViews: SELESAI <---")
    }

    /**
     * Mengatur RecyclerView untuk menampilkan riwayat perjalanan paket dari API.
     */
    private fun setupRecyclerView() {
        historyAdapter = TrackingHistoryAdapter(emptyList())
        rvTrackingHistory.layoutManager = LinearLayoutManager(this)
        rvTrackingHistory.adapter = historyAdapter
        Log.d(TAG, "setupRecyclerView: RecyclerView Riwayat API disiapkan.")
    }

    /**
     * Mengambil detail pelacakan dari API BinderByte.
     */
    private fun fetchTrackingDetails() {
        Log.d(TAG, "fetchTrackingDetails: Memulai untuk AWB: $awbNumber, Kurir: $courierCode")
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.trackShipment(apiKey, courierCode, awbNumber)
                Log.d(TAG, "fetchTrackingDetails: Respons API Mentah: ${response.raw()}")
                if (response.isSuccessful && response.body()?.data != null) {
                    val trackData = response.body()!!.data!!
                    Log.d(TAG, "fetchTrackingDetails: Data pelacakan diterima: $trackData")
                    displayTrackingData(trackData)
                } else {
                    handleApiError(response)
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchTrackingDetails: Exception terjadi!", e)
                val errorMessage = if (e is UnknownHostException) {
                    "Error: Tidak dapat tersambung. Pastikan terhubung dengan internet."
                } else {
                    "Error Aplikasi: ${e.localizedMessage ?: "Terjadi kesalahan tidak diketahui"}."
                }
                showErrorDialog(if (e is UnknownHostException) "Koneksi Bermasalah" else "Error Aplikasi", errorMessage, if (e is UnknownHostException) R.drawable.ic_signal_off else R.drawable.ic_warning_red)
            } finally {
                setLoadingState(false)
            }
        }
    }

    /**
     * Menangani error dari respons API yang tidak sukses atau body data null.
     */
    private fun <T> handleApiError(response: retrofit2.Response<T>) {
        var specificErrorMessage = "Gagal mendapatkan detail pelacakan." // Pesan default
        val errorBodyString = response.errorBody()?.string()

        if (!errorBodyString.isNullOrEmpty()) {
            Log.e(TAG, "handleApiError: Error Body: $errorBodyString")
            try {
                val jsonError = JSONObject(errorBodyString)
                specificErrorMessage = jsonError.optString("message", specificErrorMessage)
            } catch (e: Exception) {
                Log.e(TAG, "handleApiError: Gagal parsing JSON error body: $errorBodyString", e)
                specificErrorMessage = when (response.code()) {
                    400 -> "Nomor resi atau kurir tidak valid. Periksa kembali input Anda."
                    401, 403 -> "Autentikasi gagal. Periksa API Key Anda."
                    404 -> "Data tidak ditemukan. Nomor resi mungkin salah, belum terdaftar, atau sudah kedaluwarsa."
                    500, 502, 503, 504 -> "Server sedang bermasalah. Coba lagi nanti."
                    else -> "Gagal mendapatkan detail (Kode: ${response.code()})."
                }
            }
        } else if (response.message().isNotEmpty()) {
            specificErrorMessage = response.message()
        }

        Log.e(TAG, "handleApiError: Gagal fetch. Kode: ${response.code()}, Pesan API: '$specificErrorMessage'")
        showErrorDialog("Error ${response.code()}", specificErrorMessage)
    }

    /**
     * Mengatur visibilitas komponen UI saat loading atau setelah selesai.
     */
    private fun setLoadingState(isLoading: Boolean) {
        Log.d(TAG, "setLoadingState: isLoading = $isLoading")
        progressBarDetails.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            cardSummary.visibility = View.GONE
            tvHistoryTitle.visibility = View.GONE
            rvTrackingHistory.visibility = View.GONE
            tvErrorDetails.visibility = View.GONE // Sembunyikan TextView error default
        }
        // Visibilitas cardSummary dan lainnya akan diatur oleh displayTrackingData atau showErrorDialog
    }

    /**
     * Menampilkan data pelacakan yang berhasil diterima ke UI.
     */
    private fun displayTrackingData(data: TrackData) {
        Log.d(TAG, "displayTrackingData: Menampilkan data...")
        cardSummary.visibility = View.VISIBLE
        tvErrorDetails.visibility = View.GONE // Pastikan pesan error lama hilang

        tvAwbNumberDetail.text = "No. Resi: ${data.summary.awb}"
        val finalCourierName = courierNameDisplay ?: data.summary.courier // Prioritaskan nama dari intent jika ada
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
            Log.d(TAG, "displayTrackingData: Menampilkan riwayat API: ${data.history.size} item.")
            tvHistoryTitle.visibility = View.VISIBLE
            rvTrackingHistory.visibility = View.VISIBLE
            historyAdapter.updateData(data.history)
        } else {
            Log.d(TAG, "displayTrackingData: Tidak ada riwayat dari API.")
            tvHistoryTitle.visibility = View.GONE
            rvTrackingHistory.visibility = View.GONE
        }
    }

    /**
     * Menampilkan dialog error kepada pengguna.
     */
    private fun showErrorDialog(title: String, message: String, iconResId: Int? = R.drawable.ic_warning_red) {
        Log.e(TAG, "showErrorDialog: Title: '$title', Message: '$message'")
        if (isFinishing || isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed, not showing error dialog.")
            return
        }
        // Sembunyikan konten utama jika terjadi error
        cardSummary.visibility = View.GONE
        tvHistoryTitle.visibility = View.GONE
        rvTrackingHistory.visibility = View.GONE

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Pertimbangkan untuk finish() jika errornya membuat halaman tidak berguna
                // finish()
            }
        iconResId?.let { builder.setIcon(it) }
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Menggunakan onBackPressedDispatcher untuk kompatibilitas yang lebih baik
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
