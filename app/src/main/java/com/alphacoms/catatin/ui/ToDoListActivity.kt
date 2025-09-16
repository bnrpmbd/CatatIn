package com.alphacoms.catatin.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.Priority
import com.alphacoms.catatin.data.ToDo
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ToDoListActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var todoAdapter: ToDoAdapter
    private lateinit var fabAddTodo: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo_list)

        initViews()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadTodos()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewTodos)
        fabAddTodo = findViewById(R.id.fabAddTodo)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupRecyclerView() {
        todoAdapter = ToDoAdapter(
            onItemClick = { todo -> editTodo(todo) },
            onStatusChange = { todo, isCompleted -> updateTodoStatus(todo, isCompleted) },
            onDeleteClick = { todo -> deleteTodo(todo) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = todoAdapter
    }

    private fun setupClickListeners() {
        fabAddTodo.setOnClickListener {
            showAddTodoDialog()
        }
    }

    private fun showAddTodoDialog(todo: ToDo? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_todo, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etTodoTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etTodoDescription)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val btnSelectDate = dialogView.findViewById<MaterialButton>(R.id.btnSelectDueDate)

        // Setup priority spinner
        val priorities = Priority.values().map { it.name }
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter

        var selectedDueDate: Date? = null

        // Pre-fill if editing
        todo?.let {
            etTitle.setText(it.title)
            etDescription.setText(it.description)
            spinnerPriority.setSelection(it.priority.ordinal)
            selectedDueDate = it.dueDate
        }

        btnSelectDate.setOnClickListener {
            showDatePicker { date ->
                selectedDueDate = date
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                btnSelectDate.text = "Due: ${dateFormat.format(date)}"
            }
        }

        AlertDialog.Builder(this)
            .setTitle(if (todo == null) "Tambah Todo" else "Edit Todo")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val priority = Priority.values()[spinnerPriority.selectedItemPosition]

                if (title.isEmpty()) {
                    Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newTodo = if (todo == null) {
                    ToDo(
                        title = title,
                        description = description,
                        priority = priority,
                        dueDate = selectedDueDate,
                        createdAt = Date()
                    )
                } else {
                    todo.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        dueDate = selectedDueDate
                    )
                }

                saveTodo(newTodo, todo == null)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveTodo(todo: ToDo, isNew: Boolean) {
        lifecycleScope.launch {
            try {
                if (isNew) {
                    database.todoDao().insertToDo(todo)
                } else {
                    database.todoDao().updateToDo(todo)
                }
                runOnUiThread {
                    Toast.makeText(
                        this@ToDoListActivity,
                        if (isNew) "Todo berhasil ditambahkan!" else "Todo berhasil diupdate!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@ToDoListActivity,
                        "Error menyimpan todo: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun editTodo(todo: ToDo) {
        showAddTodoDialog(todo)
    }

    private fun updateTodoStatus(todo: ToDo, isCompleted: Boolean) {
        lifecycleScope.launch {
            try {
                database.todoDao().updateToDoStatus(todo.id, isCompleted)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ToDoListActivity, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTodo(todo: ToDo) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Todo")
            .setMessage("Apakah Anda yakin ingin menghapus \"${todo.title}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.todoDao().deleteToDo(todo)
                        runOnUiThread {
                            Toast.makeText(this@ToDoListActivity, "Todo dihapus", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@ToDoListActivity, "Error menghapus todo: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadTodos() {
        database.todoDao().getAllTodos().observe(this) { todos ->
            todoAdapter.submitList(todos)
        }
    }
}