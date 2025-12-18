package com.alphacoms.catatin

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.PreferenceHelper
import com.alphacoms.catatin.data.TransactionType
import com.alphacoms.catatin.ui.FinanceActivity
import com.alphacoms.catatin.ui.NotesActivity
import com.alphacoms.catatin.ui.NotesHomeAdapter
import com.alphacoms.catatin.ui.SettingsActivity
import com.alphacoms.catatin.ui.ToDoListActivity
import com.alphacoms.catatin.ui.TodosHomeAdapter
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    
    private lateinit var pieChart: PieChart
    
    private lateinit var imgProfile: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvSelectedMonth: TextView
    private lateinit var btnToggleVisibility: ImageView
    
    private lateinit var recyclerViewNotesHome: RecyclerView
    private lateinit var recyclerViewTodosHome: RecyclerView
    private lateinit var tvEmptyNotes: TextView
    private lateinit var tvEmptyTodos: TextView
    
    // Notes scroll controls
    private lateinit var layoutNotesScrollbar: View
    private lateinit var scrollViewNotes: androidx.core.widget.NestedScrollView
    private lateinit var btnNotesScrollUp: ImageView
    private lateinit var btnNotesScrollDown: ImageView
    
    // To-Do List controls
    private lateinit var layoutWeekToggle: View
    private lateinit var tvWeekLabel: TextView
    private lateinit var layoutMonthYearSelector: View
    private lateinit var tvTodoMonthYear: TextView
    private lateinit var btnPrevDay: ImageView
    private lateinit var tvSelectedDay: TextView
    private lateinit var btnNextDay: ImageView
    
    private lateinit var notesHomeAdapter: NotesHomeAdapter
    private lateinit var todosHomeAdapter: TodosHomeAdapter
    
    private var isAmountVisible = true
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    
    // To-Do calendar state
    private var todoSelectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var todoSelectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var todoSelectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private var todoViewMode = "Hari" // "Hari", "Week", "Month"
    
    private val months = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveProfileImage(uri)
            }
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk memilih foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)
        preferenceHelper = PreferenceHelper(this)
        
        initViews()
        setupClickListeners()
        setupDropdown()
        setupRecyclerViews()
        setupSearch()
        setupMonthSelector()
        setupVisibilityToggle()
        setupTodoControls()
    }
    
    override fun onResume() {
        super.onResume()
        loadUserProfile()
        loadFinanceData()
        loadNotesData()
        loadTodosData()
    }
    
    private fun initViews() {
        imgProfile = findViewById(R.id.imgProfile)
        tvName = findViewById(R.id.tvName)
        tvRole = findViewById(R.id.tvRole)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvBalance = findViewById(R.id.tvBalance)
        etSearch = findViewById(R.id.etSearch)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        btnToggleVisibility = findViewById(R.id.btnToggleVisibility)
        recyclerViewNotesHome = findViewById(R.id.recyclerViewNotesHome)
        recyclerViewTodosHome = findViewById(R.id.recyclerViewTodosHome)
        tvEmptyNotes = findViewById(R.id.tvEmptyNotes)
        tvEmptyTodos = findViewById(R.id.tvEmptyTodos)
        
        // Pie Chart
        pieChart = findViewById(R.id.pieChart)
        setupPieChart()
        
        // Notes scroll controls
        layoutNotesScrollbar = findViewById(R.id.layoutNotesScrollbar)
        scrollViewNotes = findViewById(R.id.scrollViewNotes)
        btnNotesScrollUp = findViewById(R.id.btnNotesScrollUp)
        btnNotesScrollDown = findViewById(R.id.btnNotesScrollDown)
        
        // To-Do List controls
        layoutWeekToggle = findViewById(R.id.layoutWeekToggle)
        tvWeekLabel = findViewById(R.id.tvWeekLabel)
        layoutMonthYearSelector = findViewById(R.id.layoutMonthYearSelector)
        tvTodoMonthYear = findViewById(R.id.tvTodoMonthYear)
        btnPrevDay = findViewById(R.id.btnPrevDay)
        tvSelectedDay = findViewById(R.id.tvSelectedDay)
        btnNextDay = findViewById(R.id.btnNextDay)
        
        // Set current month
        tvSelectedMonth.text = months[selectedMonth]
        
        // Set current month/year for To-Do
        updateTodoMonthYear()
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
        // Set default image
        imgProfile.setImageResource(android.R.drawable.sym_def_app_icon)
    }

    private fun setupClickListeners() {
        // Profile click - pick image
        imgProfile.setOnClickListener {
            showProfileOptionsDialog()
        }
        
        // Settings button
        findViewById<View>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Notification button
        findViewById<View>(R.id.btnNotif).setOnClickListener {
            Toast.makeText(this, "Tidak ada notifikasi baru", Toast.LENGTH_SHORT).show()
        }
        
        // Notes scroll buttons
        btnNotesScrollUp.setOnClickListener {
            scrollViewNotes.smoothScrollBy(0, -200)
        }
        
        btnNotesScrollDown.setOnClickListener {
            scrollViewNotes.smoothScrollBy(0, 200)
        }
        
        // Bottom Navigation with transitions
        findViewById<View>(R.id.cardNotes).setOnClickListener {
            val intent = Intent(this, NotesActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<View>(R.id.cardTodoList).setOnClickListener {
            val intent = Intent(this, ToDoListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        findViewById<View>(R.id.cardFinance).setOnClickListener {
            val intent = Intent(this, FinanceActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    
    private fun showProfileOptionsDialog() {
        val options = arrayOf("Ganti Foto Profil", "Edit Profil", "Batal")
        AlertDialog.Builder(this)
            .setTitle("Opsi Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkPermissionAndPickImage()
                    1 -> startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            .show()
    }
    
    private fun checkPermissionAndPickImage() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> openImagePicker()
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun saveProfileImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Save to internal storage
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            // Save path to preferences
            preferenceHelper.setProfileImagePath(file.absolutePath)
            
            // Update UI
            imgProfile.setImageBitmap(bitmap)
            Toast.makeText(this, "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun setupMonthSelector() {
        val layoutMonthSelector = findViewById<LinearLayout>(R.id.layoutMonthSelector)
        layoutMonthSelector.setOnClickListener {
            showMonthPickerDialog()
        }
    }
    
    private fun showMonthPickerDialog() {
        AlertDialog.Builder(this)
            .setTitle("Pilih Bulan")
            .setItems(months) { _, which ->
                selectedMonth = which
                tvSelectedMonth.text = months[which]
                loadFinanceData()
            }
            .show()
    }
    
    private fun setupVisibilityToggle() {
        btnToggleVisibility.setOnClickListener {
            isAmountVisible = !isAmountVisible
            updateAmountVisibility()
        }
    }
    
    private fun updateAmountVisibility() {
        if (isAmountVisible) {
            btnToggleVisibility.setImageResource(R.drawable.ic_visibility)
            loadFinanceData()
        } else {
            btnToggleVisibility.setImageResource(R.drawable.ic_visibility_off)
            tvTotalIncome.text = "••••••"
            tvTotalExpense.text = "••••••"
            tvBalance.text = "••••••"
        }
    }
    
    private fun setupRecyclerViews() {
        // Notes RecyclerView with Grid
        notesHomeAdapter = NotesHomeAdapter { note ->
            startActivity(Intent(this, NotesActivity::class.java))
        }
        recyclerViewNotesHome.layoutManager = GridLayoutManager(this, 2)
        recyclerViewNotesHome.adapter = notesHomeAdapter
        
        // Todos RecyclerView
        todosHomeAdapter = TodosHomeAdapter { todo ->
            startActivity(Intent(this, ToDoListActivity::class.java))
        }
        recyclerViewTodosHome.layoutManager = LinearLayoutManager(this)
        recyclerViewTodosHome.adapter = todosHomeAdapter
    }
    
    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchData(query)
                } else {
                    loadNotesData()
                    loadTodosData()
                }
            }
        })
    }
    
    private fun searchData(query: String) {
        lifecycleScope.launch {
            // Search notes
            val allNotes = database.noteDao().getAllNotesSync()
            val filteredNotes = allNotes.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.content.contains(query, ignoreCase = true)
            }.take(4)
            notesHomeAdapter.submitList(filteredNotes)
            tvEmptyNotes.visibility = if (filteredNotes.isEmpty()) View.VISIBLE else View.GONE
            
            // Search todos
            val allTodos = database.todoDao().getAllTodosSync()
            val filteredTodos = allTodos.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }.take(5)
            todosHomeAdapter.submitList(filteredTodos)
            tvEmptyTodos.visibility = if (filteredTodos.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun loadNotesData() {
        lifecycleScope.launch {
            val allNotes = database.noteDao().getAllNotesSync()
            val notes = allNotes.take(8) // Allow more notes for scrolling
            notesHomeAdapter.submitList(notes)
            tvEmptyNotes.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
            
            // Show scrollbar only if more than 4 notes
            layoutNotesScrollbar.visibility = if (allNotes.size > 4) View.VISIBLE else View.GONE
        }
    }
    
    private fun loadTodosData() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val startTime: Date
            val endTime: Date
            
            when (todoViewMode) {
                "Hari" -> {
                    // Show todos for selected day only
                    calendar.set(todoSelectedYear, todoSelectedMonth, todoSelectedDay, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startTime = calendar.time
                    
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    endTime = calendar.time
                }
                "Week" -> {
                    // Show todos for selected week
                    calendar.set(todoSelectedYear, todoSelectedMonth, todoSelectedDay, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    startTime = calendar.time
                    
                    calendar.add(Calendar.DAY_OF_WEEK, 6)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    endTime = calendar.time
                }
                else -> { // "Month"
                    // Show todos for selected month
                    calendar.set(todoSelectedYear, todoSelectedMonth, 1, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    startTime = calendar.time
                    
                    calendar.add(Calendar.MONTH, 1)
                    calendar.add(Calendar.MILLISECOND, -1)
                    endTime = calendar.time
                }
            }
            
            val todos = database.todoDao().getAllTodosSync()
                .filter { todo ->
                    !todo.isCompleted && 
                    todo.dueDate != null &&
                    !todo.dueDate.before(startTime) && 
                    !todo.dueDate.after(endTime)
                }
                .sortedBy { it.dueDate }
                .take(5)
            todosHomeAdapter.submitList(todos)
            tvEmptyTodos.visibility = if (todos.isEmpty()) View.VISIBLE else View.GONE
        }
    }
    
    private fun loadFinanceData() {
        if (!isAmountVisible) return
        
        lifecycleScope.launch {
            // Get start and end of selected month
            val calendar = Calendar.getInstance()
            calendar.set(selectedYear, selectedMonth, 1, 0, 0, 0)
            val startOfMonth = calendar.time
            
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfMonth = calendar.time
            
            // Get records for selected month
            val records = database.financeDao().getRecordsBetweenDatesSync(startOfMonth, endOfMonth)
            
            var totalIncome = 0.0
            var totalExpense = 0.0
            
            for (record in records) {
                if (record.type == TransactionType.INCOME) {
                    totalIncome += record.amount
                } else {
                    totalExpense += record.amount
                }
            }
            
            val balance = totalIncome - totalExpense
            
            tvTotalIncome.text = formatCurrency(totalIncome)
            tvTotalExpense.text = formatCurrency(totalExpense)
            tvBalance.text = formatCurrency(balance)
            tvBalance.setTextColor(if (balance >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
            
            // Update Pie Chart
            updatePieChart(totalIncome.toFloat(), totalExpense.toFloat())
        }
    }
    
    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = "Keuangan"
        pieChart.setCenterTextSize(16f)
        pieChart.setDrawCenterText(true)
        pieChart.legend.isEnabled = true
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 50f
        pieChart.transparentCircleRadius = 55f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
    }
    
    private fun updatePieChart(income: Float, expense: Float) {
        val entries = ArrayList<PieEntry>()
        
        if (income > 0 || expense > 0) {
            if (income > 0) {
                entries.add(PieEntry(income, "Pemasukan"))
            }
            if (expense > 0) {
                entries.add(PieEntry(expense, "Pengeluaran"))
            }
            
            val colors = ArrayList<Int>()
            if (income > 0) colors.add(Color.parseColor("#4CAF50"))
            if (expense > 0) colors.add(Color.parseColor("#F44336"))
            
            val dataSet = PieDataSet(entries, "")
            dataSet.colors = colors
            dataSet.valueTextSize = 14f
            dataSet.valueTextColor = Color.WHITE
            
            val data = PieData(dataSet)
            data.setValueFormatter(com.github.mikephil.charting.formatter.PercentFormatter(pieChart))
            
            pieChart.data = data
            pieChart.invalidate()
        } else {
            pieChart.clear()
            pieChart.centerText = "Belum ada data"
            pieChart.invalidate()
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("IDR", "Rp").replace(",00", "")
    }

    private fun setupDropdown() {
        val items = listOf("Catatan", "To-Do List")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        val dropdown = findViewById<AutoCompleteTextView>(R.id.tvCatatanTitle)
        dropdown.setAdapter(adapter)

        dropdown.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            val layoutCatatan = findViewById<View>(R.id.layoutCatatan)
            val layoutToDoList = findViewById<View>(R.id.layoutToDoList)

            if (selectedItem == "Catatan") {
                layoutCatatan.visibility = View.VISIBLE
                layoutToDoList.visibility = View.GONE
            } else {
                layoutCatatan.visibility = View.GONE
                layoutToDoList.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupTodoControls() {
        // Week toggle click - switch between Hari/Week/Month view
        layoutWeekToggle.setOnClickListener {
            showViewModeDialog()
        }
        
        // Month/Year selector click - show month picker
        layoutMonthYearSelector.setOnClickListener {
            showTodoMonthYearPicker()
        }
        
        // Day navigation clicks
        btnPrevDay.setOnClickListener {
            navigateDay(-1)
        }
        
        btnNextDay.setOnClickListener {
            navigateDay(1)
        }
        
        // Day label click - show date picker
        tvSelectedDay.setOnClickListener {
            showTodoDatePicker()
        }
    }
    
    private fun updateTodoMonthYear() {
        tvTodoMonthYear.text = "${months[todoSelectedMonth]} $todoSelectedYear"
    }
    
    private fun updateTodoDay() {
        val calendar = Calendar.getInstance()
        calendar.set(todoSelectedYear, todoSelectedMonth, todoSelectedDay)
        
        val today = Calendar.getInstance()
        val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                      calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                      calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
        
        tvSelectedDay.text = if (isToday) "Hari ini" else "${todoSelectedDay} ${months[todoSelectedMonth].substring(0, 3)}"
        loadTodosData() // Reload todos for selected date
    }
    
    private fun navigateDay(direction: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(todoSelectedYear, todoSelectedMonth, todoSelectedDay)
        calendar.add(Calendar.DAY_OF_MONTH, direction)
        
        todoSelectedYear = calendar.get(Calendar.YEAR)
        todoSelectedMonth = calendar.get(Calendar.MONTH)
        todoSelectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        updateTodoMonthYear()
        updateTodoDay()
    }
    
    private fun showViewModeDialog() {
        val viewModes = arrayOf("Hari", "Week", "Month")
        
        AlertDialog.Builder(this)
            .setTitle("Pilih Tampilan")
            .setItems(viewModes) { _, which ->
                todoViewMode = viewModes[which]
                tvWeekLabel.text = todoViewMode
                
                // Show feedback
                val message = when (todoViewMode) {
                    "Hari" -> "Menampilkan tugas hari ini"
                    "Week" -> "Menampilkan tugas minggu ini"
                    else -> "Menampilkan tugas bulan ini"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                
                loadTodosData() // Reload with new view mode
            }
            .show()
    }
    
    private fun showTodoMonthYearPicker() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 5).toList()
        
        // First, show month picker
        AlertDialog.Builder(this)
            .setTitle("Pilih Bulan")
            .setItems(months) { _, monthIndex ->
                todoSelectedMonth = monthIndex
                
                // Then show year picker
                AlertDialog.Builder(this)
                    .setTitle("Pilih Tahun")
                    .setItems(years.map { it.toString() }.toTypedArray()) { _, yearIndex ->
                        todoSelectedYear = years[yearIndex]
                        updateTodoMonthYear()
                        updateTodoDay()
                    }
                    .show()
            }
            .show()
    }
    
    private fun showTodoDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                todoSelectedYear = year
                todoSelectedMonth = month
                todoSelectedDay = dayOfMonth
                updateTodoMonthYear()
                updateTodoDay()
            },
            todoSelectedYear,
            todoSelectedMonth,
            todoSelectedDay
        )
        datePickerDialog.show()
    }
}
