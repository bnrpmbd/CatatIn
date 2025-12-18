package com.alphacoms.catatin.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.AppDatabase
import com.alphacoms.catatin.data.FinanceRecord
import com.alphacoms.catatin.data.TransactionType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddFinanceBottomSheet : BottomSheetDialogFragment() {

    private var transactionType: TransactionType = TransactionType.EXPENSE
    private var existingRecord: FinanceRecord? = null
    private var onSaveListener: ((FinanceRecord) -> Unit)? = null

    companion object {
        private const val ARG_TYPE = "transaction_type"
        private const val ARG_RECORD_ID = "record_id"

        fun newInstance(type: TransactionType, record: FinanceRecord? = null): AddFinanceBottomSheet {
            return AddFinanceBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type.name)
                    record?.let { putLong(ARG_RECORD_ID, it.id) }
                }
                existingRecord = record
            }
        }
    }

    fun setOnSaveListener(listener: (FinanceRecord) -> Unit) {
        onSaveListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionType = TransactionType.valueOf(it.getString(ARG_TYPE, TransactionType.EXPENSE.name))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_finance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Make bottom sheet expanded by default
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }

        setupViews(view)
    }

    private fun setupViews(view: View) {
        val headerLayout = view.findViewById<LinearLayout>(R.id.headerLayout)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)
        val iconType = view.findViewById<ImageView>(R.id.iconType)
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etCategory = view.findViewById<EditText>(R.id.etCategory)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etPaymentMethod = view.findViewById<EditText>(R.id.etPaymentMethod)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // Set appearance based on transaction type
        if (transactionType == TransactionType.INCOME) {
            headerLayout.setBackgroundColor(Color.parseColor("#4CAF50"))
            tvTitle.text = "Tambah Pemasukan"
            iconType.setImageResource(android.R.drawable.arrow_up_float)
            btnSave.setBackgroundColor(Color.parseColor("#4CAF50"))
            etCategory.hint = "Gaji, Freelance, Investasi, dll"
        } else {
            headerLayout.setBackgroundColor(Color.parseColor("#F44336"))
            tvTitle.text = "Tambah Pengeluaran"
            iconType.setImageResource(android.R.drawable.arrow_down_float)
            btnSave.setBackgroundColor(Color.parseColor("#F44336"))
            etCategory.hint = "Makanan, Transportasi, Belanja, dll"
        }

        // Set current date and time
        val currentDate = existingRecord?.createdAt ?: Date()
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        tvDate.text = dateFormat.format(currentDate)
        tvTime.text = timeFormat.format(currentDate)
        
        // Pre-fill if editing
        existingRecord?.let { record ->
            etTitle.setText(record.title)
            etAmount.setText(record.amount.toLong().toString())
            etCategory.setText(record.category)
            etDescription.setText(record.description)
            etPaymentMethod.setText(record.paymentMethod)
            tvTitle.text = if (transactionType == TransactionType.INCOME) "Edit Pemasukan" else "Edit Pengeluaran"
        }

        // Close button
        btnClose.setOnClickListener {
            dismiss()
        }

        // Save button
        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val paymentMethod = etPaymentMethod.text.toString().trim().ifEmpty { "Tunai" }

            // Validation
            if (title.isEmpty()) {
                etTitle.error = "Judul tidak boleh kosong"
                etTitle.requestFocus()
                return@setOnClickListener
            }

            if (amountStr.isEmpty()) {
                etAmount.error = "Jumlah tidak boleh kosong"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            val amount = try {
                amountStr.toDouble()
            } catch (e: NumberFormatException) {
                etAmount.error = "Jumlah tidak valid"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            if (amount <= 0) {
                etAmount.error = "Jumlah harus lebih dari 0"
                etAmount.requestFocus()
                return@setOnClickListener
            }

            if (category.isEmpty()) {
                etCategory.error = "Kategori tidak boleh kosong"
                etCategory.requestFocus()
                return@setOnClickListener
            }

            // Create finance record
            val record = if (existingRecord != null) {
                existingRecord!!.copy(
                    title = title,
                    amount = amount,
                    type = transactionType,
                    category = category,
                    description = description,
                    paymentMethod = paymentMethod
                )
            } else {
                FinanceRecord(
                    title = title,
                    amount = amount,
                    type = transactionType,
                    category = category,
                    description = description,
                    paymentMethod = paymentMethod,
                    createdAt = currentDate
                )
            }

            // Save to database
            saveRecord(record, existingRecord != null)
        }
    }

    private fun saveRecord(record: FinanceRecord, isUpdate: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(requireContext())
                if (isUpdate) {
                    database.financeDao().updateFinanceRecord(record)
                } else {
                    database.financeDao().insertFinanceRecord(record)
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        if (isUpdate) "Transaksi berhasil diperbarui" 
                        else if (transactionType == TransactionType.INCOME) "Pemasukan berhasil ditambahkan" 
                        else "Pengeluaran berhasil ditambahkan",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSaveListener?.invoke(record)
                    dismiss()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
