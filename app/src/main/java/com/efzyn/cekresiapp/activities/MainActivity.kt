package com.efzyn.cekresiapp.ui.main // Ganti dengan package name proyekmu

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.efzyn.cekresiapp.BuildConfig
import com.efzyn.cekresiapp.R
import com.efzyn.cekresiapp.adapters.CourierAdapter
import com.efzyn.cekresiapp.adapters.HistoryAdapter
import com.efzyn.cekresiapp.model.Courier
import com.efzyn.cekresiapp.model.HistoryItem
import com.efzyn.cekresiapp.network.RetrofitClient
import com.efzyn.cekresiapp.ui.tracking.TrackingDetailsActivity
import com.efzyn.cekresiapp.utils.HistoryManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    // Views untuk konten utama
    private lateinit var rvCouriers: RecyclerView
    private lateinit var courierAdapter: CourierAdapter
    private lateinit var progressBarCouriers: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchViewCouriers: SearchView
    private var allCouriersList: List<Courier> = emptyList()
    private lateinit var tvEmptyCouriers: TextView

    private lateinit var tvSelectedCourier: TextView
    private lateinit var etAwbNumber: EditText
    private lateinit var tilAwbNumber: TextInputLayout
    private lateinit var btnTrack: Button

    // Views untuk Navigation Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    // Views di dalam Navigation Drawer untuk Riwayat
    private lateinit var rvHistoryDrawer: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var tvHistoryTitleLabelDrawer: TextView
    private lateinit var btnClearHistoryDrawer: Button
    private var navHeaderInlineContainer: LinearLayout? = null
    private lateinit var tvEmptyHistoryDrawer: TextView


    // Variabel state
    private var selectedCourier: Courier? = null
    private val apiKey = BuildConfig.BINDERBYTE_API_KEY
    private val TAG = "MainActivity_UTS"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "--> MainActivity onCreate: MULAI <---")
        super.onCreate(savedInstanceState)
        Log.d(TAG, "--> MainActivity onCreate: setelah super.onCreate()")
        setContentView(R.layout.activity_main)
        Log.d(TAG, "--> MainActivity onCreate: setelah setContentView()")

        initializeMainViews()
        Log.d(TAG, "--> MainActivity onCreate: setelah initializeMainViews()")

        setupNavigationDrawer()
        Log.d(TAG, "--> MainActivity onCreate: setelah setupNavigationDrawer()")

        Log.d(TAG, "--> MainActivity onCreate: Akan mem-post applyInsetsToNavHeader()")
        if (::drawerLayout.isInitialized && ::navigationView.isInitialized) {
            drawerLayout.post {
                Log.d(TAG, "--> MainActivity onCreate (dalam post): Memanggil applyInsetsToNavHeader()")
                applyInsetsToNavHeader()
            }
            Log.d(TAG, "--> MainActivity onCreate: SELESAI post applyInsetsToNavHeader()")
        } else {
            Log.e(TAG, "--> MainActivity onCreate: KESALAHAN! drawerLayout atau navigationView belum diinisialisasi sebelum post.")
        }

        setupCourierRecyclerView()
        setupSwipeRefresh()
        setupSearchView()
        setupTrackButton()
        setupHistoryRecyclerViewInDrawer()

        fetchCouriers()
        loadHistoryToDrawer()
        Log.d(TAG, "--> MainActivity onCreate: SELESAI <---")
    }

    private fun initializeMainViews() {
        Log.d(TAG, "--> initializeMainViews: MULAI <---")
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)

        rvCouriers = findViewById(R.id.rvCouriers)
        progressBarCouriers = findViewById(R.id.progressBarCouriers)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        searchViewCouriers = findViewById(R.id.searchViewCouriers)
        tvEmptyCouriers = findViewById(R.id.tvEmptyCouriers) // Inisialisasi

        tvSelectedCourier = findViewById(R.id.tvSelectedCourier)
        etAwbNumber = findViewById(R.id.etAwbNumber)
        tilAwbNumber = findViewById(R.id.tilAwbNumber)
        btnTrack = findViewById(R.id.btnTrack)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        navHeaderInlineContainer = navigationView.findViewById(R.id.nav_header_inline_container)
        if (navHeaderInlineContainer == null) {
            Log.e(TAG, "--> initializeMainViews: KESALAHAN! navHeaderInlineContainer TIDAK DITEMUKAN.")
        } else {
            Log.d(TAG, "--> initializeMainViews: navHeaderInlineContainer BERHASIL ditemukan.")
        }
        rvHistoryDrawer = navigationView.findViewById(R.id.rvHistory_drawer)
        tvHistoryTitleLabelDrawer = navigationView.findViewById(R.id.tvHistoryTitleLabel_drawer)
        btnClearHistoryDrawer = navigationView.findViewById(R.id.btnClearHistory_drawer)
        tvEmptyHistoryDrawer = navigationView.findViewById(R.id.tvEmptyHistory_drawer) // Inisialisasi
        Log.d(TAG, "--> initializeMainViews: SELESAI <---")
    }

    private fun setupNavigationDrawer() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarMain)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        Log.d(TAG, "setupNavigationDrawer: Navigation Drawer disiapkan.")

        btnClearHistoryDrawer.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Bersihkan Semua Riwayat?")
                .setMessage("Semua riwayat pelacakan akan dihapus secara permanen.")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Bersihkan") { _, _ ->
                    HistoryManager.clearHistory(this)
                    loadHistoryToDrawer()
                    Toast.makeText(this, "Semua riwayat telah dibersihkan.", Toast.LENGTH_SHORT).show()
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                .show()
        }
    }

    private fun applyInsetsToNavHeader() {
        Log.d(TAG, "--> applyInsetsToNavHeader: FUNGSI DIPANGGIL.")
        ViewCompat.setOnApplyWindowInsetsListener(navigationView) { view, windowInsets ->
            Log.d(TAG, "--> applyInsetsToNavHeader: LISTENER DIPANGGIL PADA NAVIGATIONVIEW!")
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            Log.d(TAG, "--> applyInsetsToNavHeader: systemBarsInsets.top (dari NavigationView) = ${systemBarsInsets.top}")

            val headerView = navHeaderInlineContainer
            if (headerView != null) {
                val initialPaddingLeft = headerView.paddingLeft
                val initialPaddingRight = headerView.paddingRight
                val initialPaddingBottom = headerView.paddingBottom
                headerView.setPadding(
                    initialPaddingLeft,
                    systemBarsInsets.top,
                    initialPaddingRight,
                    initialPaddingBottom
                )
                Log.d(TAG, "--> applyInsetsToNavHeader: Padding atas headerView di-set ke: ${systemBarsInsets.top}. Padding final: ${headerView.paddingTop}")
            } else {
                Log.e(TAG, "--> applyInsetsToNavHeader: KESALAHAN! navHeaderInlineContainer adalah null di dalam listener NavigationView.")
            }
            windowInsets
        }
        if (ViewCompat.isAttachedToWindow(navigationView)) {
            Log.d(TAG, "--> applyInsetsToNavHeader: navigationView sudah attached, meminta applyInsets.")
            ViewCompat.requestApplyInsets(navigationView)
        } else {
            Log.d(TAG, "--> applyInsetsToNavHeader: navigationView belum attached, menambahkan OnAttachStateChangeListener.")
            navigationView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    Log.d(TAG, "--> applyInsetsToNavHeader (onViewAttachedToWindow for navView): navigationView sekarang attached, meminta applyInsets.")
                    v.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(v)
                }
                override fun onViewDetachedFromWindow(v: View) = Unit
            })
        }
    }

    private fun setupCourierRecyclerView() {
        courierAdapter = CourierAdapter(emptyList()) { courier ->
            val clickedPositionInFilteredList = courierAdapter.couriersFiltered.indexOf(courier)
            if (clickedPositionInFilteredList != -1) {
                courierAdapter.setSelectedPosition(clickedPositionInFilteredList)
            }
            selectedCourier = courier
            tvSelectedCourier.text = "Kurir: ${courier.name}"
            tilAwbNumber.visibility = View.VISIBLE
            updateTrackButtonState()
            searchViewCouriers.setQuery("", false)
            searchViewCouriers.clearFocus()
            etAwbNumber.requestFocus()
            Log.d(TAG, "Kurir dipilih: ${courier.name} (${courier.code}), Posisi di filter: $clickedPositionInFilteredList")
            Toast.makeText(this, "Kurir dipilih: ${courier.name}", Toast.LENGTH_SHORT).show()
        }
        rvCouriers.layoutManager = LinearLayoutManager(this)
        rvCouriers.adapter = courierAdapter
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Swipe refresh kurir dipicu.")
            fetchCouriers()
        }
    }

    // --- MODIFIKASI FUNGSI fetchCouriers ---
    private fun fetchCouriers() {
        Log.d(TAG, "Memulai fetch daftar kurir...")
        progressBarCouriers.visibility = View.VISIBLE
        swipeRefreshLayout.isRefreshing = true
        rvCouriers.visibility = View.GONE
        tvEmptyCouriers.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getListCourier(apiKey)
                if (response.isSuccessful) {
                    response.body()?.let { couriers ->
                        allCouriersList = couriers
                        courierAdapter.setData(allCouriersList)
                        if (couriers.isEmpty()) {
                            rvCouriers.visibility = View.GONE
                            tvEmptyCouriers.text = "Tidak ada data kurir yang tersedia."
                            tvEmptyCouriers.visibility = View.VISIBLE
                        } else {
                            rvCouriers.visibility = View.VISIBLE
                            tvEmptyCouriers.visibility = View.GONE
                        }
                        Log.d(TAG, "Daftar kurir dimuat: ${couriers.size} item.")
                    } ?: run {
                        Log.w(TAG, "Body respons kurir null.")
                        handleFetchCourierError("Data kurir kosong dari server.")
                    }
                } else {
                    Log.e(TAG, "Gagal fetch kurir. Kode: ${response.code()}, Msg: ${response.message()}")
                    handleFetchCourierError("Gagal memuat daftar kurir (Kode: ${response.code()}).")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat fetch kurir!", e)
                if (e is UnknownHostException) {
                    handleFetchCourierError("Error: Tidak dapat tersambung. Pastikan terhubung dengan internet.", true)
                } else {
                    handleFetchCourierError("Error: ${e.localizedMessage ?: "Terjadi kesalahan tidak diketahui"}.")
                }
            } finally {
                progressBarCouriers.visibility = View.GONE
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun handleFetchCourierError(message: String, isConnectionError: Boolean = false) {
        rvCouriers.visibility = View.GONE
//        Menampilkan pesan eror koneksi
        tvEmptyCouriers.text = message
        tvEmptyCouriers.visibility = View.VISIBLE

        val dialogTitle = if (isConnectionError) "Koneksi Bermasalah" else "Gagal Memuat Data"
        val dialogMessage = if (isConnectionError) "Error: Tidak dapat tersambung. Pastikan terhubung dengan internet." else message

        if (!isFinishing && !isDestroyed) {
            MaterialAlertDialogBuilder(this)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setPositiveButton("OK", null)
                .setIcon(if (isConnectionError) R.drawable.ic_signal_off else R.drawable.ic_warning_red) // Ganti dengan ikon yang sesuai
                .show()
        }
    }



    private fun setupSearchView() {
        searchViewCouriers.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                courierAdapter.filter.filter(query)
                searchViewCouriers.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                courierAdapter.filter.filter(newText)
                return true
            }
        })
        val searchEditText = searchViewCouriers.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                searchViewCouriers.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun setupTrackButton() {
        btnTrack.setOnClickListener {
            Log.d(TAG, "Tombol Lacak Paket ditekan.")
            performTracking()
        }
        updateTrackButtonState()
    }

    private fun setupHistoryRecyclerViewInDrawer() {
        historyAdapter = HistoryAdapter(
            mutableListOf(),
            onItemClick = { historyItem ->
                Log.d(TAG, "Item riwayat di drawer diklik: ${historyItem.awb}, Kurir: ${historyItem.courierName}")
                val intent = Intent(this, TrackingDetailsActivity::class.java).apply {
                    putExtra(TrackingDetailsActivity.EXTRA_AWB, historyItem.awb)
                    putExtra(TrackingDetailsActivity.EXTRA_COURIER_CODE, historyItem.courierCode)
                    putExtra(TrackingDetailsActivity.EXTRA_COURIER_NAME, historyItem.courierName)
                }
                startActivity(intent)
                drawerLayout.closeDrawer(GravityCompat.START)
                Toast.makeText(this, "Membuka detail untuk: ${historyItem.awb}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { historyItem, position ->
                Log.d(TAG, "Tombol hapus riwayat di drawer untuk: ${historyItem.awb}")
                MaterialAlertDialogBuilder(this)
                    .setTitle("Hapus Riwayat?")
                    .setMessage("Anda yakin ingin menghapus resi ${historyItem.awb} (${historyItem.courierName}) dari riwayat?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ ->
                        HistoryManager.removeHistoryItem(this, historyItem)
                        historyAdapter.removeItem(position)
                        updateHistoryVisibilityInDrawer()
                        Toast.makeText(this, "Riwayat ${historyItem.awb} dihapus.", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        )
        rvHistoryDrawer.layoutManager = LinearLayoutManager(this)
        rvHistoryDrawer.adapter = historyAdapter
    }

    private fun loadHistoryToDrawer() {
        Log.d(TAG, "Memuat riwayat pelacakan ke drawer...")
        val historyList = HistoryManager.getHistory(this)
        historyAdapter.updateData(historyList)
        updateHistoryVisibilityInDrawer()
    }

    private fun updateHistoryVisibilityInDrawer() {
        val itemCount = historyAdapter.itemCount
        if (itemCount > 0) {
            tvHistoryTitleLabelDrawer.visibility = View.VISIBLE
            rvHistoryDrawer.visibility = View.VISIBLE
            btnClearHistoryDrawer.visibility = View.VISIBLE
            tvEmptyHistoryDrawer.visibility = View.GONE
        } else {
            tvHistoryTitleLabelDrawer.visibility = View.GONE
            rvHistoryDrawer.visibility = View.GONE
            btnClearHistoryDrawer.visibility = View.GONE
            tvEmptyHistoryDrawer.visibility = View.VISIBLE
        }
    }

    private fun updateTrackButtonState() {
        val awbNotEmpty = etAwbNumber.text.toString().isNotBlank()
        btnTrack.isEnabled = selectedCourier != null && awbNotEmpty
    }

    private fun performTracking() {
        val awbNumberInput = etAwbNumber.text.toString().trim()
        if (selectedCourier == null) {
            Toast.makeText(this, "Silakan pilih kurir dahulu.", Toast.LENGTH_SHORT).show()
            return
        }
        if (awbNumberInput.isEmpty()) {
            tilAwbNumber.error = "Nomor resi tidak boleh kosong!"
            return
        } else {
            tilAwbNumber.error = null
        }
        Log.d(TAG, "Memulai pelacakan AWB: $awbNumberInput, Kurir: ${selectedCourier!!.name}")
        val historyItem = HistoryItem(awbNumberInput, selectedCourier!!.code, selectedCourier!!.name)
        HistoryManager.addHistoryItem(this, historyItem)
        loadHistoryToDrawer()
        val intent = Intent(this, TrackingDetailsActivity::class.java).apply {
            putExtra(TrackingDetailsActivity.EXTRA_AWB, awbNumberInput)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_CODE, selectedCourier!!.code)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_NAME, selectedCourier!!.name)
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity dilanjutkan.")
        loadHistoryToDrawer()
        etAwbNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTrackButtonState()
                if (s.toString().isNotEmpty()) tilAwbNumber.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
