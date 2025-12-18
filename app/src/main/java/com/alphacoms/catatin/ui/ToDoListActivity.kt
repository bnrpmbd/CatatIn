package com.alphacoms.catatin.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
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
import com.alphacoms.catatin.data.PreferenceHelper
import com.alphacoms.catatin.data.ToDo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ToDoListActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var fabAddTodo: FloatingActionButton
    private lateinit var tvName: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvTodoCount: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var chipContainer: LinearLayout
    
    private var allTodos: List<ToDo> = emptyList()
    private var customCategories: MutableSet<String> = mutableSetOf("Tugas", "Agenda")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        preferenceHelper = PreferenceHelper(this)
        
        // Load saved categories
        val savedCategories = preferenceHelper.getCategories()
        if (savedCategories.isNotEmpty()) {
            customCategories = savedCategories.toMutableSet()
        }
        
        initViews()
        setupDatabase()
        setupClickListeners()
        loadTodos()
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
        fabAddTodo = findViewById(R.id.fabAdd)
        tvName = findViewById(R.id.tvName)
        tvRole = findViewById(R.id.tvRole)
        tvTodoCount = findViewById(R.id.tvTodoCount)
        imgProfile = findViewById(R.id.imgProfile)
        categoriesContainer = findViewById(R.id.categoriesContainer)
        chipContainer = findViewById(R.id.chipContainer)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupClickListeners() {
        fabAddTodo.setOnClickListener {
            showAddTodoDialog()
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
        
        // Chip All
        findViewById<TextView>(R.id.chipAll).setOnClickListener {
            loadTodos()
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
            // Already in To-Do, do nothing
        }
        
        findViewById<View>(R.id.navFinance).setOnClickListener {
            startActivity(Intent(this, FinanceActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    
    private fun showAddTodoDialog(todo: ToDo? = null) {
        val bottomSheet = AddTodoBottomSheet(
            todoToEdit = todo,
            categories = customCategories.toList()
        ) { resultTodo ->
            if (todo == null) {
                addTodo(resultTodo)
            } else {
                updateTodo(resultTodo)
            }
            
            // Save new category if it's custom
            if (!customCategories.contains(resultTodo.category)) {
                customCategories.add(resultTodo.category)
                preferenceHelper.saveCategories(customCategories)
            }
        }
        bottomSheet.show(supportFragmentManager, "AddTodoBottomSheet")
    }

    private fun addTodo(todo: ToDo) {
        lifecycleScope.launch {
            database.todoDao().insert(todo)
            loadTodos()
            Toast.makeText(this@ToDoListActivity, "Tugas ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTodo(todo: ToDo) {
        lifecycleScope.launch {
            database.todoDao().updateToDo(todo)
            loadTodos()
            Toast.makeText(this@ToDoListActivity, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTodo(todo: ToDo) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tugas")
            .setMessage("Yakin ingin menghapus \"${todo.title}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    database.todoDao().deleteToDo(todo)
                    loadTodos()
                    Toast.makeText(this@ToDoListActivity, "Tugas dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun toggleTodoComplete(todo: ToDo) {
        lifecycleScope.launch {
            val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
            database.todoDao().updateToDo(updatedTodo)
            loadTodos()
        }
    }

    private fun loadTodos() {
        lifecycleScope.launch {
            allTodos = database.todoDao().getAllTodosSync()
            
            // Update count
            tvTodoCount.text = "${allTodos.size} Tugas"
            
            // Get unique categories from todos
            val categoriesInUse = allTodos.map { it.category }.toSet()
            customCategories.addAll(categoriesInUse)
            
            // Update chips
            updateChips()
            
            // Render categories
            renderCategories()
        }
    }
    
    private fun updateChips() {
        // Clear existing dynamic chips (keep chipAll)
        val chipAll = findViewById<TextView>(R.id.chipAll)
        chipContainer.removeAllViews()
        chipContainer.addView(chipAll)
        
        // Add category chips
        customCategories.forEach { category ->
            val chip = TextView(this).apply {
                text = category
                setBackgroundResource(R.drawable.bg_chip_transparent)
                setPadding(32, 16, 32, 16)
                textSize = 12f
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 16
                }
                setOnClickListener {
                    filterByCategory(category)
                }
            }
            chipContainer.addView(chip)
        }
    }
    
    private fun filterByCategory(category: String) {
        lifecycleScope.launch {
            val filteredTodos = allTodos.filter { it.category == category }
            
            categoriesContainer.removeAllViews()
            
            if (filteredTodos.isNotEmpty()) {
                addCategorySection(category, filteredTodos)
            }
            
            tvTodoCount.text = "${filteredTodos.size} Tugas"
        }
    }
    
    private fun renderCategories() {
        categoriesContainer.removeAllViews()
        
        // Group todos by category
        val groupedTodos = allTodos.groupBy { it.category }
        
        groupedTodos.forEach { (category, todos) ->
            addCategorySection(category, todos)
        }
    }
    
    private fun addCategorySection(category: String, todos: List<ToDo>) {
        // Section Title
        val titleView = TextView(this).apply {
            text = category
            textSize = 20f
            setTextColor(resources.getColor(android.R.color.black, null))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(48, 32, 48, 16)
        }
        categoriesContainer.addView(titleView)
        
        // Card with items
        val cardView = CardView(this).apply {
            radius = 32f
            cardElevation = 4f
            setCardBackgroundColor(resources.getColor(android.R.color.white, null))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = 48
                marginEnd = 48
            }
        }
        
        val itemsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        
        todos.forEachIndexed { index, todo ->
            val itemView = createTodoItemView(todo, category)
            itemsContainer.addView(itemView)
            
            // Add divider except for last item
            if (index < todos.size - 1) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    ).apply {
                        marginStart = 32
                        marginEnd = 32
                    }
                    setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
                    alpha = 0.2f
                }
                itemsContainer.addView(divider)
            }
        }
        
        cardView.addView(itemsContainer)
        categoriesContainer.addView(cardView)
    }
    
    private fun createTodoItemView(todo: ToDo, category: String): View {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        // Use different layout for different categories
        return if (category == "Agenda") {
            // Agenda style (no checkbox)
            val view = LayoutInflater.from(this).inflate(R.layout.item_agenda_simple, null)
            
            view.findViewById<TextView>(R.id.tvAgendaTitle).text = todo.title
            
            val dateStr = todo.dueDate?.let { dateFormat.format(it) } ?: ""
            val timeStr = todo.dueDate?.let { timeFormat.format(it) } ?: ""
            val dayName = todo.dueDate?.let {
                SimpleDateFormat("EEEE", Locale("id")).format(it)
            } ?: ""
            
            view.findViewById<TextView>(R.id.tvAgendaInfo).text = "$dateStr, $timeStr, $dayName"
            
            view.setOnClickListener {
                showAddTodoDialog(todo)
            }
            
            view.setOnLongClickListener {
                deleteTodo(todo)
                true
            }
            
            view
        } else {
            // Task style (with checkbox)
            val view = LayoutInflater.from(this).inflate(R.layout.item_todo_simple, null)
            
            val cbTodo = view.findViewById<CheckBox>(R.id.cbTodo)
            val tvTitle = view.findViewById<TextView>(R.id.tvTodoTitle)
            val tvInfo = view.findViewById<TextView>(R.id.tvTodoInfo)
            val ivAlarm = view.findViewById<View>(R.id.ivAlarm)
            
            tvTitle.text = todo.title
            cbTodo.isChecked = todo.isCompleted
            
            // Strike through if completed
            if (todo.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            
            val dateStr = todo.dueDate?.let { dateFormat.format(it) } ?: ""
            val timeStr = todo.dueDate?.let { timeFormat.format(it) } ?: ""
            val repeatStr = if (todo.repeatOption != "Tidak pernah") todo.repeatOption else ""
            
            tvInfo.text = listOf(dateStr, timeStr, repeatStr).filter { it.isNotEmpty() }.joinToString(", ")
            
            ivAlarm.visibility = if (todo.hasAlarm) View.VISIBLE else View.GONE
            
            cbTodo.setOnClickListener {
                toggleTodoComplete(todo)
            }
            
            view.setOnClickListener {
                showAddTodoDialog(todo)
            }
            
            view.setOnLongClickListener {
                deleteTodo(todo)
                true
            }
            
            view
        }
    }
}
