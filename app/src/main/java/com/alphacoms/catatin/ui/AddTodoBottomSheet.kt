package com.alphacoms.catatin.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.Priority
import com.alphacoms.catatin.data.ToDo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTodoBottomSheet(
    private val todoToEdit: ToDo? = null,
    private val categories: List<String> = listOf("Tugas", "Agenda"),
    private val onSaveClick: (ToDo) -> Unit
) : BottomSheetDialogFragment() {

    // Menggunakan Calendar untuk menyimpan gabungan Tanggal & Waktu
    private val currentCalendar = Calendar.getInstance()
    private var selectedPriority = Priority.LOW // Default sesuai gambar
    private var selectedCategory = "Tugas"
    private var selectedRepeat = "Tidak pernah"
    private var alarmEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_todo, container, false)
    }

    override fun getTheme(): Int {
        // Pastikan style ini sudah ada di themes.xml seperti instruksi sebelumnya
        return R.style.CustomBottomSheetDialog
    }
    
    override fun onStart() {
        super.onStart()
        // Make bottom sheet full screen
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            behavior.isDraggable = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi View
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnSave = view.findViewById<TextView>(R.id.btnSave)
        val etTaskName = view.findViewById<EditText>(R.id.etTaskName)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val tvPriorityBadge = view.findViewById<TextView>(R.id.tvPriorityBadge)

        val tvRepeatOption = view.findViewById<TextView>(R.id.tvRepeatOption)
        val layoutRepeat = view.findViewById<View>(R.id.layoutRepeat)
        val switchAlarm = view.findViewById<SwitchCompat>(R.id.switchAlarm)

        val tvHourValue = view.findViewById<TextView>(R.id.tvHourValue)
        val tvMinuteValue = view.findViewById<TextView>(R.id.tvMinuteValue)
        
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val layoutCategory = view.findViewById<View>(R.id.layoutCategory)

        // 2. Isi Data Jika Mode Edit
        todoToEdit?.let { todo ->
            etTaskName.setText(todo.title)
            selectedCategory = todo.category
            tvCategory.text = selectedCategory
            selectedRepeat = todo.repeatOption
            tvRepeatOption.text = selectedRepeat
            alarmEnabled = todo.hasAlarm
            switchAlarm.isChecked = alarmEnabled

            selectedPriority = todo.priority
            updatePriorityView(tvPriorityBadge, selectedPriority)

            todo.dueDate?.let { date ->
                currentCalendar.time = date
                calendarView.date = date.time // Set tampilan kalender
            }
            // Update tampilan jam/menit berdasarkan data lama
            updateTimeView(tvHourValue, tvMinuteValue)
        }

        // 3. Listener Kalender (CalendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            currentCalendar.set(Calendar.YEAR, year)
            currentCalendar.set(Calendar.MONTH, month)
            currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        // 4. Listener Jam & Menit (Simulasi Wheel Picker dengan TimePickerDialog)
        // Karena membuat Wheel Picker asli kompleks, kita pakai Dialog native saat angka diklik
        val timeClickListener = View.OnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    currentCalendar.set(Calendar.MINUTE, minute)
                    updateTimeView(tvHourValue, tvMinuteValue)
                },
                currentCalendar.get(Calendar.HOUR_OF_DAY),
                currentCalendar.get(Calendar.MINUTE),
                true // 24 jam format
            ).show()
        }
        tvHourValue.setOnClickListener(timeClickListener)
        tvMinuteValue.setOnClickListener(timeClickListener)

        // 5. Listener Prioritas (Popup Menu)
        tvPriorityBadge.setOnClickListener {
            showPriorityMenu(it, tvPriorityBadge)
        }
        
        // 5b. Listener Ulangi (Repeat Option)
        layoutRepeat.setOnClickListener {
            showRepeatMenu(it, tvRepeatOption)
        }
        tvRepeatOption.setOnClickListener {
            showRepeatMenu(it, tvRepeatOption)
        }
        
        // 5c. Listener Category
        layoutCategory.setOnClickListener {
            showCategoryMenu(it, tvCategory)
        }
        tvCategory.setOnClickListener {
            showCategoryMenu(it, tvCategory)
        }
        
        // 5d. Listener Alarm Switch
        switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            alarmEnabled = isChecked
        }

        // 6. Tombol Cancel
        btnCancel.setOnClickListener {
            dismiss()
        }

        // 7. Tombol Simpan
        btnSave.setOnClickListener {
            val title = etTaskName.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(context, "Nama tugas harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gabungkan tanggal (dari CalendarView) dan jam (dari TimePicker logic)
            val finalDueDate = currentCalendar.time

            // Buat Object Todo
            val resultTodo = if (todoToEdit == null) {
                ToDo(
                    title = title,
                    description = "",
                    priority = selectedPriority,
                    dueDate = finalDueDate,
                    createdAt = Date(),
                    category = selectedCategory,
                    hasAlarm = alarmEnabled,
                    repeatOption = selectedRepeat
                )
            } else {
                todoToEdit.copy(
                    title = title,
                    priority = selectedPriority,
                    dueDate = finalDueDate,
                    category = selectedCategory,
                    hasAlarm = alarmEnabled,
                    repeatOption = selectedRepeat
                )
            }

            onSaveClick(resultTodo)
            dismiss()
        }
    }

    private fun updateTimeView(tvHour: TextView, tvMinute: TextView) {
        val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = currentCalendar.get(Calendar.MINUTE)
        tvHour.text = String.format(Locale.getDefault(), "%02d", hour)
        tvMinute.text = String.format(Locale.getDefault(), "%02d", minute)
    }

    private fun updatePriorityView(tvBadge: TextView, priority: Priority) {
        tvBadge.text = "${priority.name} ▼"

        // Ubah warna background badge sesuai prioritas (Opsional)
        val cardParent = tvBadge.parent as? CardView
        when(priority) {
            Priority.HIGH -> cardParent?.setCardBackgroundColor(0xFFFF5252.toInt()) // Merah
            Priority.URGENT -> cardParent?.setCardBackgroundColor(0xFFFF1744.toInt()) // Merah Tua
            Priority.MEDIUM -> cardParent?.setCardBackgroundColor(0xFFFFAB91.toInt()) // Oranye
            Priority.NORMAL -> cardParent?.setCardBackgroundColor(0xFF40C4FF.toInt()) // Biru Muda
            Priority.LOW -> cardParent?.setCardBackgroundColor(0xFF00E676.toInt()) // Hijau
        }
    }

    private fun showPriorityMenu(view: View, tvBadge: TextView) {
        val popup = PopupMenu(requireContext(), view)
        // Manual populate menu karena Priority berasal dari Enum
        Priority.values().forEach {
            popup.menu.add(it.name)
        }

        popup.setOnMenuItemClickListener { item ->
            selectedPriority = Priority.valueOf(item.title.toString())
            updatePriorityView(tvBadge, selectedPriority)
            true
        }
        popup.show()
    }
    
    private fun showRepeatMenu(view: View, tvRepeat: TextView) {
        val popup = PopupMenu(requireContext(), view)
        val repeatOptions = listOf(
            "Tidak pernah",
            "Sekali",
            "Harian",
            "Mingguan",
            "Bulanan",
            "Tahunan"
        )
        
        repeatOptions.forEach { option ->
            popup.menu.add(option)
        }
        
        popup.setOnMenuItemClickListener { item ->
            selectedRepeat = item.title.toString()
            tvRepeat.text = selectedRepeat
            true
        }
        popup.show()
    }
    
    private fun showCategoryMenu(view: View, tvCategory: TextView) {
        val popup = PopupMenu(requireContext(), view)
        
        // Add existing categories
        categories.forEach { category ->
            popup.menu.add(category)
        }
        
        // Add option to create custom category
        popup.menu.add("+ Tambah Kategori Baru")
        
        popup.setOnMenuItemClickListener { item ->
            if (item.title == "+ Tambah Kategori Baru") {
                showAddCategoryDialog(tvCategory)
            } else {
                selectedCategory = item.title.toString()
                tvCategory.text = selectedCategory
            }
            true
        }
        popup.show()
    }
    
    private fun showAddCategoryDialog(tvCategory: TextView) {
        val editText = EditText(requireContext()).apply {
            hint = "Nama kategori baru"
            setPadding(50, 30, 50, 30)
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Tambah Kategori")
            .setView(editText)
            .setPositiveButton("Tambah") { _, _ ->
                val newCategory = editText.text.toString().trim()
                if (newCategory.isNotEmpty()) {
                    selectedCategory = newCategory
                    tvCategory.text = selectedCategory
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}