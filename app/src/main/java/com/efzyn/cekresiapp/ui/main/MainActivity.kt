package com.efzyn.cekresiapp.ui.main

import android.content.Intent
import android.content.res.Configuration
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

    // TAG untuk Logging
    private val TAG = "MainActivity_UTS"

    // Views untuk Konten Utama
    private lateinit var rvCouriers: RecyclerView
    private lateinit var courierAdapter: CourierAdapter
    private lateinit var progressBarCouriers: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchViewCouriers: SearchView
    private lateinit var tvEmptyCouriers: TextView
    private var allCouriersList: List<Courier> = emptyList()

    // Views untuk Input AWB
    private lateinit var tvSelectedCourier: TextView
    private lateinit var etAwbNumber: EditText
    private lateinit var tilAwbNumber: TextInputLayout
    private lateinit var btnTrack: Button

    // Views untuk Navigation Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    // Views di dalam Navigation Drawer (Riwayat)
    private lateinit var rvHistoryDrawer: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var tvHistoryTitleLabelDrawer: TextView
    private lateinit var btnClearHistoryDrawer: Button
    private var navHeaderInlineContainer: LinearLayout? = null
    private lateinit var tvEmptyHistoryDrawer: TextView

    // Variabel State
    private var selectedCourier: Courier? = null
    private val apiKey = BuildConfig.BINDERBYTE_API_KEY
    private var isTrackingInProgress = false


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "--> MainActivity onCreate: MULAI <---")
        super.onCreate(savedInstanceState)
        Log.d(TAG, "--> MainActivity onCreate: setelah super.onCreate()")
        setContentView(R.layout.activity_main)
        Log.d(TAG, "--> MainActivity onCreate: setelah setContentView()")

        initializeViews()
        Log.d(TAG, "--> MainActivity onCreate: setelah initializeViews()")

        setupNavigationDrawer()
        Log.d(TAG, "--> MainActivity onCreate: setelah setupNavigationDrawer()")

        // Terapkan insets setelah layout utama (drawer) siap
        drawerLayout.post {
            Log.d(TAG, "--> MainActivity onCreate (dalam post): Memanggil applyInsetsToNavHeader()")
            applyInsetsToNavHeader()
        }
        Log.d(TAG, "--> MainActivity onCreate: SELESAI post applyInsetsToNavHeader()")

        setupUIComponents()
        Log.d(TAG, "--> MainActivity onCreate: setelah setupUIComponents()")

        fetchInitialData()
        Log.d(TAG, "--> MainActivity onCreate: SELESAI <---")
    }

    /**
     * Menginisialisasi semua view dari layout XML.
     */
    private fun initializeViews() {
        Log.d(TAG, "--> initializeViews: MULAI <---")
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)

        // Views Konten Utama
        rvCouriers = findViewById(R.id.rvCouriers)
        progressBarCouriers = findViewById(R.id.progressBarCouriers)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        searchViewCouriers = findViewById(R.id.searchViewCouriers)
        tvEmptyCouriers = findViewById(R.id.tvEmptyCouriers)

        // Views Input AWB
        tvSelectedCourier = findViewById(R.id.tvSelectedCourier)
        etAwbNumber = findViewById(R.id.etAwbNumber)
        tilAwbNumber = findViewById(R.id.tilAwbNumber)
        btnTrack = findViewById(R.id.btnTrack)

        // Views Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Views di dalam NavigationView (Header dan Riwayat)
        navHeaderInlineContainer = navigationView.findViewById(R.id.nav_header_inline_container)
        if (navHeaderInlineContainer == null) {
            Log.e(TAG, "--> initializeViews: KESALAHAN! navHeaderInlineContainer TIDAK DITEMUKAN.")
        } else {
            Log.d(TAG, "--> initializeViews: navHeaderInlineContainer BERHASIL ditemukan.")
        }
        rvHistoryDrawer = navigationView.findViewById(R.id.rvHistory_drawer)
        tvHistoryTitleLabelDrawer = navigationView.findViewById(R.id.tvHistoryTitleLabel_drawer)
        btnClearHistoryDrawer = navigationView.findViewById(R.id.btnClearHistory_drawer)
        tvEmptyHistoryDrawer = navigationView.findViewById(R.id.tvEmptyHistory_drawer)
        Log.d(TAG, "--> initializeViews: SELESAI <---")
    }

    /**
     * Mengatur semua komponen UI dan listener-nya.
     */
    private fun setupUIComponents() {
        setupCourierRecyclerView()
        setupSwipeRefresh()
        setupSearchView()
        setupTrackButton()
        setupHistoryRecyclerViewInDrawer()
    }

    /**
     * Memuat data awal yang dibutuhkan aplikasi.
     */
    private fun fetchInitialData() {
        fetchCouriers()
        loadHistoryToDrawer()
    }

    /**
     * Mengatur Navigation Drawer beserta ActionBarDrawerToggle.
     */
    private fun setupNavigationDrawer() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarMain)
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        // toggle.syncState() akan dipanggil di onPostCreate

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        Log.d(TAG, "setupNavigationDrawer: Navigation Drawer disiapkan.")

        btnClearHistoryDrawer.setOnClickListener {
            showClearHistoryConfirmationDialog()
        }
    }

    /**
     * Menerapkan WindowInsets pada header Navigation Drawer agar tidak tertimpa status bar.
     */
    private fun applyInsetsToNavHeader() {
        Log.d(TAG, "--> applyInsetsToNavHeader: FUNGSI DIPANGGIL.")
        val headerView = navHeaderInlineContainer
        if (headerView == null) {
            Log.e(TAG, "--> applyInsetsToNavHeader: KESALAHAN! headerView (navHeaderInlineContainer) adalah null.")
            return
        }
        Log.d(TAG, "--> applyInsetsToNavHeader: headerView DITEMUKAN. Memasang listener insets.")

        val initialPaddingLeft = headerView.paddingLeft
        val initialPaddingRight = headerView.paddingRight
        val initialPaddingBottom = headerView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(navigationView) { _, windowInsets -> // Listener pada navigationView
            Log.d(TAG, "--> applyInsetsToNavHeader: LISTENER DIPANGGIL PADA NAVIGATIONVIEW!")
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            Log.d(TAG, "--> applyInsetsToNavHeader: systemBarsInsets.top (dari NavigationView) = ${systemBarsInsets.top}")

            headerView.setPadding(
                initialPaddingLeft,
                systemBarsInsets.top, // Terapkan inset atas untuk status bar
                initialPaddingRight,
                initialPaddingBottom
            )
            Log.d(TAG, "--> applyInsetsToNavHeader: Padding atas headerView di-set ke: ${systemBarsInsets.top}. Padding final di view: ${headerView.paddingTop}")
            windowInsets // Kembalikan insets asli agar NavigationView bisa memprosesnya
        }

        // Minta agar insets diterapkan pada navigationView
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

    /**
     * Mengatur RecyclerView untuk menampilkan daftar kurir.
     */
    private fun setupCourierRecyclerView() {
        courierAdapter = CourierAdapter(emptyList()) { courier ->
            handleCourierSelection(courier)
        }
        rvCouriers.layoutManager = LinearLayoutManager(this)
        rvCouriers.adapter = courierAdapter
        Log.d(TAG, "RecyclerView Kurir disiapkan.")
    }

    /**
     * Menangani logika ketika kurir dipilih dari daftar.
     */
    private fun handleCourierSelection(courier: Courier) {
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

    /**
     * Mengatur SwipeRefreshLayout untuk memuat ulang daftar kurir.
     */
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Swipe refresh kurir dipicu.")
            fetchCouriers()
        }
    }

    /**
     * Mengambil data daftar kurir dari API.
     */
    private fun fetchCouriers() {
        Log.d(TAG, "Memulai fetch daftar kurir...")
        setCourierLoadingState(true)
        tvEmptyCouriers.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getListCourier(apiKey)
                if (response.isSuccessful) {
                    response.body()?.let { couriers ->
                        allCouriersList = couriers
                        courierAdapter.setData(allCouriersList)
                        updateCourierListVisibility(couriers.isEmpty())
                        Log.d(TAG, "Daftar kurir dimuat: ${couriers.size} item.")
                    } ?: run {
                        Log.w(TAG, "Body respons kurir null.")
                        updateCourierListVisibility(true, "Data kurir kosong dari server.")
                    }
                } else {
                    Log.e(TAG, "Gagal fetch kurir. Kode: ${response.code()}, Msg: ${response.message()}")
                    updateCourierListVisibility(true, "Gagal memuat daftar kurir (Kode: ${response.code()}).")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat fetch kurir!", e)
                val errorMessage = if (e is UnknownHostException) {
                    "Error: Tidak dapat tersambung. Pastikan terhubung dengan internet."
                } else {
                    "Error: ${e.localizedMessage ?: "Terjadi kesalahan tidak diketahui"}."
                }
                updateCourierListVisibility(true, errorMessage, e is UnknownHostException)
            } finally {
                setCourierLoadingState(false)
            }
        }
    }

    /**
     * Mengatur visibilitas komponen terkait daftar kurir (RecyclerView, ProgressBar, Pesan Kosong).
     */
    private fun setCourierLoadingState(isLoading: Boolean) {
        progressBarCouriers.visibility = if (isLoading) View.VISIBLE else View.GONE
        swipeRefreshLayout.isRefreshing = isLoading
        if (isLoading) { // Sembunyikan list dan pesan kosong saat loading
            rvCouriers.visibility = View.GONE
            tvEmptyCouriers.visibility = View.GONE
        }
    }

    /**
     * Mengupdate visibilitas daftar kurir dan pesan empty/error state.
     * Jika ada error koneksi, tampilkan dialog.
     */
    private fun updateCourierListVisibility(isEmpty: Boolean, message: String? = null, isConnectionError: Boolean = false) {
        if (isEmpty) {
            rvCouriers.visibility = View.GONE
            tvEmptyCouriers.text = message ?: "Tidak ada data kurir yang tersedia."
            tvEmptyCouriers.visibility = View.VISIBLE
            if (isConnectionError && message != null) { // Tampilkan dialog hanya jika ada pesan error koneksi
                showErrorDialog("Koneksi Bermasalah", message, R.drawable.ic_signal_off)
            } else if (message != null) { // Tampilkan dialog untuk error lain jika ada pesan
                showErrorDialog("Gagal Memuat", message, R.drawable.ic_warning_red)
            }
        } else {
            rvCouriers.visibility = View.VISIBLE
            tvEmptyCouriers.visibility = View.GONE
        }
    }

    /**
     * Mengatur SearchView untuk memfilter daftar kurir.
     */
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
        Log.d(TAG, "SearchView Kurir disiapkan.")
    }

    /**
     * Mengatur tombol "Lacak Paket".
     */
    private fun setupTrackButton() {
        btnTrack.setOnClickListener {
            Log.d(TAG, "Tombol Lacak Paket ditekan.")
            performTracking()
        }
        updateTrackButtonState() // Set state awal
    }

    /**
     * Mengatur RecyclerView untuk menampilkan riwayat pelacakan di dalam Navigation Drawer.
     */
    private fun setupHistoryRecyclerViewInDrawer() {
        historyAdapter = HistoryAdapter(
            mutableListOf(),
            onItemClick = { historyItem ->
                handleHistoryItemClick(historyItem)
            },
            onDeleteClick = { historyItem, position ->
                showDeleteHistoryItemConfirmationDialog(historyItem, position)
            }
        )
        rvHistoryDrawer.layoutManager = LinearLayoutManager(this)
        rvHistoryDrawer.adapter = historyAdapter
        Log.d(TAG, "RecyclerView Riwayat di Drawer disiapkan.")
    }

    /**
     * Menangani klik pada item riwayat di drawer.
     */
    private fun handleHistoryItemClick(historyItem: HistoryItem) {
        Log.d(TAG, "Item riwayat di drawer diklik: ${historyItem.awb}, Kurir: ${historyItem.courierName}")
        val intent = Intent(this, TrackingDetailsActivity::class.java).apply {
            putExtra(TrackingDetailsActivity.EXTRA_AWB, historyItem.awb)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_CODE, historyItem.courierCode)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_NAME, historyItem.courierName)
        }
        startActivity(intent)
        drawerLayout.closeDrawer(GravityCompat.START)
        Toast.makeText(this, "Membuka detail untuk: ${historyItem.awb}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Menampilkan dialog konfirmasi sebelum menghapus item riwayat.
     */
    private fun showDeleteHistoryItemConfirmationDialog(historyItem: HistoryItem, position: Int) {
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

    /**
     * Menampilkan dialog konfirmasi sebelum menghapus semua riwayat.
     */
    private fun showClearHistoryConfirmationDialog() {
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


    /**
     * Memuat data riwayat pelacakan ke Navigation Drawer.
     */
    private fun loadHistoryToDrawer() {
        Log.d(TAG, "Memuat riwayat pelacakan ke drawer...")
        val historyList = HistoryManager.getHistory(this)
        historyAdapter.updateData(historyList) // Adapter akan mengurutkan
        updateHistoryVisibilityInDrawer()
        Log.d(TAG, "Riwayat dimuat ke drawer, jumlah: ${historyList.size}")
    }

    /**
     * Mengupdate visibilitas komponen terkait riwayat di Navigation Drawer.
     */
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
        Log.d(TAG, "Visibilitas riwayat di drawer diperbarui. Item count: $itemCount")
    }

    /**
     * Mengupdate state enabled/disabled tombol "Lacak Paket".
     */
    private fun updateTrackButtonState() {
        val awbNotEmpty = etAwbNumber.text.toString().isNotBlank()
        btnTrack.isEnabled = selectedCourier != null && awbNotEmpty
    }

    /**
     * Memulai proses pelacakan resi.
     */
    private fun performTracking() {
        if (isTrackingInProgress) {
            Toast.makeText(this, "Harap tunggu, pelacakan sebelumnya masih diproses...", Toast.LENGTH_SHORT).show()
            return
        }
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
        setTrackingButtonLoadingState(true)

        // Simpan ke riwayat
        val historyItem = HistoryItem(awbNumberInput, selectedCourier!!.code, selectedCourier!!.name)
        HistoryManager.addHistoryItem(this, historyItem)
        loadHistoryToDrawer() // Update tampilan riwayat di drawer

        val intent = Intent(this, TrackingDetailsActivity::class.java).apply {
            putExtra(TrackingDetailsActivity.EXTRA_AWB, awbNumberInput)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_CODE, selectedCourier!!.code)
            putExtra(TrackingDetailsActivity.EXTRA_COURIER_NAME, selectedCourier!!.name)
        }
        startActivity(intent)
        // State tombol akan direset di onResume
    }

    /**
     * Mengatur tampilan tombol "Lacak Paket" saat proses loading.
     */
    private fun setTrackingButtonLoadingState(isLoading: Boolean) {
        isTrackingInProgress = isLoading
        btnTrack.isEnabled = !isLoading
        btnTrack.text = if (isLoading) "Melacak..." else "Lacak Paket"
    }

    /**
     * Menampilkan dialog error umum.
     */
    private fun showErrorDialog(title: String, message: String, iconResId: Int? = R.drawable.ic_warning_red) {
        if (isFinishing || isDestroyed) {
            Log.w(TAG, "Activity is finishing or destroyed, not showing error dialog.")
            return
        }
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
        iconResId?.let { builder.setIcon(it) }
        builder.show()
    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity dilanjutkan.")
        loadHistoryToDrawer()
        setTrackingButtonLoadingState(false) // Reset state tombol lacak

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("onBackPressedDispatcher.onBackPressed()"))
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
