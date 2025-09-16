package com.alphacoms.catatin.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.FinanceRecord
import com.alphacoms.catatin.data.TransactionType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class FinanceActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var financeAdapter: FinanceAdapter
    private lateinit var fabAddFinance: FloatingActionButton
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var tvBalance: TextView

    private val incomeCategories = listOf(
        "Gaji", "Freelance", "Investasi", "Hadiah", "Bonus", "Penjualan", "Lainnya"
    )

    private val expenseCategories = listOf(
        "Makanan", "Transportasi", "Belanja", "Hiburan", "Tagihan", "Kesehatan", 
        "Pendidikan", "Investasi", "Lainnya"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finance)

        initViews()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadFinanceData()
        observeFinanceTotals()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewFinance)
        fabAddFinance = findViewById(R.id.fabAddFinance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)
        tvBalance = findViewById(R.id.tvBalance)
    }

    private fun setupDatabase() {
        database = AppDatabase.getDatabase(this)
    }

    private fun setupRecyclerView() {
        financeAdapter = FinanceAdapter(
            onItemClick = { record -> editFinanceRecord(record) },
            onDeleteClick = { record -> deleteFinanceRecord(record) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = financeAdapter
    }

    private fun setupClickListeners() {
        fabAddFinance.setOnClickListener {
            showAddFinanceDialog()
        }
    }

    private fun showAddFinanceDialog(record: FinanceRecord? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_finance, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etFinanceTitle)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etFinanceAmount)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etFinanceDescription)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rbIncome)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rbExpense)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerFinanceCategory)

        // Pre-fill if editing
        record?.let {
            etTitle.setText(it.title)
            etAmount.setText(it.amount.toString())
            etDescription.setText(it.description)
            when (it.type) {
                TransactionType.INCOME -> rbIncome.isChecked = true
                TransactionType.EXPENSE -> rbExpense.isChecked = true
            }
        }

        // Setup category spinner based on transaction type
        fun updateCategorySpinner(isIncome: Boolean) {
            val categories = if (isIncome) incomeCategories else expenseCategories
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            // Pre-select if editing
            record?.let {
                val index = categories.indexOf(it.category)
                if (index >= 0) spinnerCategory.setSelection(index)
            }
        }

        rbIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateCategorySpinner(true)
        }

        rbExpense.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) updateCategorySpinner(false)
        }

        // Initial setup
        updateCategorySpinner(rbIncome.isChecked)

        AlertDialog.Builder(this)
            .setTitle(if (record == null) "Tambah Transaksi" else "Edit Transaksi")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val category = spinnerCategory.selectedItem.toString()
                val type = if (rbIncome.isChecked) TransactionType.INCOME else TransactionType.EXPENSE

                if (title.isEmpty()) {
                    Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = try {
                    amountStr.toDouble()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (amount <= 0) {
                    Toast.makeText(this, "Jumlah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newRecord = if (record == null) {
                    FinanceRecord(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        description = description,
                        createdAt = Date()
                    )
                } else {
                    record.copy(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        description = description
                    )
                }

                saveFinanceRecord(newRecord, record == null)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveFinanceRecord(record: FinanceRecord, isNew: Boolean) {
        lifecycleScope.launch {
            try {
                if (isNew) {
                    database.financeDao().insertFinanceRecord(record)
                } else {
                    database.financeDao().updateFinanceRecord(record)
                }
                runOnUiThread {
                    Toast.makeText(
                        this@FinanceActivity,
                        if (isNew) "Transaksi berhasil ditambahkan!" else "Transaksi berhasil diupdate!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@FinanceActivity,
                        "Error menyimpan transaksi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun editFinanceRecord(record: FinanceRecord) {
        showAddFinanceDialog(record)
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
                            Toast.makeText(this@FinanceActivity, "Error menghapus transaksi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun loadFinanceData() {
        database.financeDao().getAllFinanceRecords().observe(this) { records ->
            financeAdapter.submitList(records)
        }
    }

    private fun observeFinanceTotals() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

        database.financeDao().getTotalIncome().observe(this) { totalIncome ->
            val income = totalIncome ?: 0.0
            tvTotalIncome.text = formatter.format(income)
            updateBalance()
        }

        database.financeDao().getTotalExpense().observe(this) { totalExpense ->
            val expense = totalExpense ?: 0.0
            tvTotalExpense.text = formatter.format(expense)
            updateBalance()
        }
    }

    private fun updateBalance() {
        lifecycleScope.launch {
            try {
                val totalIncome = database.financeDao().getTotalIncome().value ?: 0.0
                val totalExpense = database.financeDao().getTotalExpense().value ?: 0.0
                val balance = totalIncome - totalExpense

                runOnUiThread {
                    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    tvBalance.text = formatter.format(balance)
                    tvBalance.setTextColor(
                        if (balance >= 0) 
                            android.graphics.Color.parseColor("#4CAF50") 
                        else 
                            android.graphics.Color.parseColor("#F44336")
                    )
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}