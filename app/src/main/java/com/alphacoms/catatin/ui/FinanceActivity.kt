package com.alphacoms.catatin.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.alphacoms.catatin.MainActivity
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.FinanceRecord
import com.alphacoms.catatin.data.PreferenceHelper
import com.alphacoms.catatin.data.TransactionType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FinanceActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var fabAddFinance: FloatingActionButton
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvYear: TextView
    private lateinit var tvName: TextView
    private lateinit var tvRole: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var btnToggleAmount: ImageView
    private lateinit var transactionsContainer: LinearLayout

    private lateinit var cardFabIncome: LinearLayout
    private lateinit var cardFabExpense: LinearLayout
    private lateinit var fabIncome: LinearLayout
    private lateinit var fabExpense: LinearLayout
    private var isFabOpen = false
    
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var allRecords: List<FinanceRecord> = emptyList()
    
    // Track expanded date sections
    private val expandedDates = mutableSetOf<String>()
    
    // Toggle show/hide amounts
    private var isAmountVisible = true
    private var cachedIncome = 0.0
    private var cachedExpense = 0.0
    private var cachedBalance = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        preferenceHelper = PreferenceHelper(this)
        initViews()
        setupDatabase()
        setupClickListeners()
        loadFinanceData()
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        tvName.text = preferenceHelper.getUserName()
        tvRole.text = preferenceHelper.getUserRole()
        loadProfileImage()
    }
    
    private fun loadProfileImage() {
        val profileImagePath = preferenceHelper.getProfileImagePath()
        if (profileImagePath.isNotEmpty()) {
            val file = File(profileImagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(profileImagePath)
                imgProfile.setImageBitmap(bitmap)
                return
            }
        }
        imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
    }

    private fun initViews() {
        fabAddFinance = findViewById(R.id.fabAddFinance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvBalance = findViewById(R.id.tvBalance)
        tvYear = findViewById(R.id.tvYear)
        tvName = findViewById(R.id.tvName)
        tvRole = findViewById(R.id.tvRole)
        imgProfile = findViewById(R.id.imgProfile)
        btnToggleAmount = findViewById(R.id.btnToggleAmount)
        transactionsContainer = findViewById(R.id.transactionsContainer)

        cardFabIncome = findViewById(R.id.cardFabIncome)
        cardFabExpense = findViewById(R.id.cardFabExpense)
        fabIncome = findViewById(R.id.fabIncome)
        fabExpense = findViewById(R.id.fabExpense)
        
        tvYear.text = selectedYear.toString()
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupClickListeners() {
        fabAddFinance.setOnClickListener {
            toggleFab()
        }

        // Click on entire FAB row for income
        cardFabIncome.setOnClickListener {
            toggleFab()
            showAddFinanceBottomSheet(TransactionType.INCOME)
        }
        
        fabIncome.setOnClickListener {
            toggleFab()
            showAddFinanceBottomSheet(TransactionType.INCOME)
        }

        // Click on entire FAB row for expense
        cardFabExpense.setOnClickListener {
            toggleFab()
            showAddFinanceBottomSheet(TransactionType.EXPENSE)
        }
        
        fabExpense.setOnClickListener {
            toggleFab()
            showAddFinanceBottomSheet(TransactionType.EXPENSE)
        }
        
        // Year navigation
        findViewById<View>(R.id.btnPrevYear).setOnClickListener {
            selectedYear--
            tvYear.text = selectedYear.toString()
            filterByYear()
        }
        
        findViewById<View>(R.id.btnNextYear).setOnClickListener {
            selectedYear++
            tvYear.text = selectedYear.toString()
            filterByYear()
        }
        
        // Toggle show/hide amounts
        btnToggleAmount.setOnClickListener {
            isAmountVisible = !isAmountVisible
            updateAmountVisibility()
        }
        
        // Header buttons
        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        findViewById<View>(R.id.btnNotif).setOnClickListener {
            Toast.makeText(this, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<View>(R.id.imgProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Bottom Navigation with transitions
        findViewById<View>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        findViewById<View>(R.id.navNotes).setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        findViewById<View>(R.id.navTodo).setOnClickListener {
            startActivity(Intent(this, ToDoListActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        findViewById<View>(R.id.navFinance).setOnClickListener {
            // Already in Finance
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    
    private fun updateAmountVisibility() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
        
        if (isAmountVisible) {
            tvTotalIncome.text = formatter.format(cachedIncome)
            tvTotalExpense.text = formatter.format(cachedExpense)
            tvBalance.text = formatter.format(cachedBalance)
            btnToggleAmount.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            tvTotalIncome.text = "••••••••"
            tvTotalExpense.text = "••••••••"
            tvBalance.text = "••••••••"
            btnToggleAmount.setImageResource(android.R.drawable.ic_secure)
        }
    }
    
    private fun showAddFinanceBottomSheet(type: TransactionType) {
        val bottomSheet = AddFinanceBottomSheet.newInstance(type)
        bottomSheet.setOnSaveListener { 
            // Data will be automatically refreshed via LiveData observer
        }
        bottomSheet.show(supportFragmentManager, "AddFinanceBottomSheet")
    }

    private fun loadFinanceData() {
        database.financeDao().getAllFinanceRecords().observe(this) { records ->
            allRecords = records
            filterByYear()
        }
    }
    
    private fun filterByYear() {
        val filtered = allRecords.filter { record ->
            val cal = Calendar.getInstance()
            cal.time = record.createdAt
            cal.get(Calendar.YEAR) == selectedYear
        }
        renderTransactions(filtered)
        updateTotalsForYear(filtered)
    }
    
    private fun updateTotalsForYear(records: List<FinanceRecord>) {
        cachedIncome = records.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        cachedExpense = records.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        cachedBalance = cachedIncome - cachedExpense
        
        updateAmountVisibility()
        
        tvBalance.setTextColor(
            if (cachedBalance >= 0) Color.parseColor("#536DFE") else Color.parseColor("#F44336")
        )
    }
    
    private fun renderTransactions(records: List<FinanceRecord>) {
        transactionsContainer.removeAllViews()
        
        if (records.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Belum ada transaksi di tahun $selectedYear"
                textSize = 14f
                setTextColor(Color.parseColor("#9E9E9E"))
                setPadding(0, 48, 0, 48)
            }
            transactionsContainer.addView(emptyView)
            return
        }
        
        // Group by date
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEEE, d/M", Locale("id", "ID"))
        
        val grouped = records.sortedByDescending { it.createdAt }
            .groupBy { dateFormat.format(it.createdAt) }
        
        // Initially expand all dates
        if (expandedDates.isEmpty()) {
            expandedDates.addAll(grouped.keys)
        }
        
        grouped.forEach { (dateKey, dateRecords) ->
            val displayDate = displayFormat.format(dateRecords.first().createdAt)
            
            // Date Header
            val headerView = LayoutInflater.from(this)
                .inflate(R.layout.item_finance_date_header, transactionsContainer, false)
            
            val tvDateHeader = headerView.findViewById<TextView>(R.id.tvDateHeader)
            val ivExpand = headerView.findViewById<ImageView>(R.id.ivExpand)
            
            tvDateHeader.text = displayDate
            
            val isExpanded = expandedDates.contains(dateKey)
            ivExpand.rotation = if (isExpanded) 0f else -90f
            
            transactionsContainer.addView(headerView)
            
            // Card with transaction items
            val card = CardView(this).apply {
                radius = 24f
                cardElevation = 2f
                setCardBackgroundColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                visibility = if (isExpanded) View.VISIBLE else View.GONE
                tag = dateKey
            }
            
            val innerContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            
            dateRecords.forEachIndexed { index, record ->
                val itemView = createTransactionItem(record)
                innerContainer.addView(itemView)
                
                // Divider
                if (index < dateRecords.size - 1) {
                    val divider = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                        ).apply {
                            marginStart = 76
                            marginEnd = 16
                        }
                        setBackgroundColor(Color.parseColor("#EEEEEE"))
                    }
                    innerContainer.addView(divider)
                }
            }
            
            card.addView(innerContainer)
            
            // Click to toggle
            headerView.setOnClickListener {
                if (expandedDates.contains(dateKey)) {
                    expandedDates.remove(dateKey)
                    ivExpand.animate().rotation(-90f).setDuration(200)
                    card.visibility = View.GONE
                } else {
                    expandedDates.add(dateKey)
                    ivExpand.animate().rotation(0f).setDuration(200)
                    card.visibility = View.VISIBLE
                }
            }
            
            transactionsContainer.addView(card)
        }
    }
    
    private fun createTransactionItem(record: FinanceRecord): View {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.item_finance_transaction, null)
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvAmount = view.findViewById<TextView>(R.id.tvAmount)
        val tvPaymentMethod = view.findViewById<TextView>(R.id.tvPaymentMethod)
        val ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
        val cardIcon = view.findViewById<CardView>(R.id.cardIcon)
        
        tvTitle.text = record.title
        tvDescription.text = record.description.ifEmpty { record.category }
        tvPaymentMethod.text = record.paymentMethod
        
        // Format amount
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        val amountStr = if (record.type == TransactionType.INCOME) {
            "+Rp${formatter.format(record.amount.toLong())}"
        } else {
            "-Rp${formatter.format(record.amount.toLong())}"
        }
        tvAmount.text = amountStr
        
        // Set colors based on type
        if (record.type == TransactionType.INCOME) {
            tvAmount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            tvAmount.setTextColor(Color.parseColor("#F44336"))
        }
        
        // Set icon based on category
        setIconForCategory(ivIcon, cardIcon, record.category)
        
        // Click listeners
        view.setOnClickListener {
            editFinanceRecord(record)
        }
        
        view.setOnLongClickListener {
            deleteFinanceRecord(record)
            true
        }
        
        return view
    }
    
    private fun setIconForCategory(ivIcon: ImageView, cardIcon: CardView, category: String) {
        when (category.lowercase()) {
            "makanan", "makan" -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_compass)
                cardIcon.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            }
            "transportasi", "bensin" -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_directions)
                cardIcon.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
            }
            "belanja" -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_upload)
                cardIcon.setCardBackgroundColor(Color.parseColor("#FCE4EC"))
            }
            "hiburan" -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                cardIcon.setCardBackgroundColor(Color.parseColor("#F3E5F5"))
            }
            "tagihan", "pulsa" -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_call)
                cardIcon.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
            }
            "gaji", "bonus" -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_save)
                cardIcon.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
            }
            else -> {
                ivIcon.setImageResource(android.R.drawable.ic_menu_save)
                cardIcon.setCardBackgroundColor(Color.parseColor("#FFF8E1"))
            }
        }
    }

    private fun editFinanceRecord(record: FinanceRecord) {
        val bottomSheet = AddFinanceBottomSheet.newInstance(record.type, record)
        bottomSheet.setOnSaveListener { }
        bottomSheet.show(supportFragmentManager, "EditFinanceBottomSheet")
    }

    private fun deleteFinanceRecord(record: FinanceRecord) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus \"${record.title}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.financeDao().deleteFinanceRecord(record)
                        runOnUiThread {
                            Toast.makeText(this@FinanceActivity, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@FinanceActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleFab() {
        if (isFabOpen) {
            cardFabIncome.animate().alpha(0f).translationY(50f).setDuration(200).withEndAction {
                cardFabIncome.visibility = View.GONE
            }
            cardFabExpense.animate().alpha(0f).translationY(50f).setDuration(150).withEndAction {
                cardFabExpense.visibility = View.GONE
            }
            fabAddFinance.animate().rotation(0f).setDuration(200)
            isFabOpen = false
        } else {
            cardFabIncome.visibility = View.VISIBLE
            cardFabIncome.alpha = 0f
            cardFabIncome.translationY = 50f
            cardFabIncome.animate().alpha(1f).translationY(0f).setDuration(200)
            
            cardFabExpense.visibility = View.VISIBLE
            cardFabExpense.alpha = 0f
            cardFabExpense.translationY = 50f
            cardFabExpense.animate().alpha(1f).translationY(0f).setDuration(250)
            
            fabAddFinance.animate().rotation(45f).setDuration(200)
            isFabOpen = true
        }
    }
}
