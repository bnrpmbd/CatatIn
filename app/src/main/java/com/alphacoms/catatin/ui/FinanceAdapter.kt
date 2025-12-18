package com.alphacoms.catatin.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.FinanceRecord
import com.alphacoms.catatin.data.TransactionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class FinanceAdapter(
    private val onItemClick: (FinanceRecord) -> Unit,
    private val onDeleteClick: (FinanceRecord) -> Unit
) : ListAdapter<FinanceRecord, FinanceAdapter.FinanceViewHolder>(FinanceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FinanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_finance, parent, false)
        return FinanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: FinanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FinanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)

        fun bind(record: FinanceRecord) {
            tvCategory.text = record.category
            tvDescription.text = record.title
            
            // Format amount with currency
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val amountText = formatter.format(record.amount)
            tvAmount.text = amountText
            
            // Set color and type text based on transaction type
            if (record.type == TransactionType.INCOME) {
                tvAmount.setTextColor(Color.parseColor("#4CAF50"))
                tvType.text = "Pemasukan"
                ivIcon.setImageResource(android.R.drawable.arrow_up_float)
                ivIcon.setColorFilter(Color.parseColor("#4CAF50"))
            } else {
                tvAmount.setTextColor(Color.parseColor("#F44336"))
                tvType.text = "Pengeluaran"
                ivIcon.setImageResource(android.R.drawable.arrow_down_float)
                ivIcon.setColorFilter(Color.parseColor("#F44336"))
            }
            
            tvDate.text = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(record.createdAt)
            
            // Set listeners
            itemView.setOnClickListener {
                onItemClick(record)
            }
            
            // Delete functionality is moved to edit dialog or long press
            itemView.setOnLongClickListener {
                onDeleteClick(record)
                true
            }
        }
    }

    class FinanceDiffCallback : DiffUtil.ItemCallback<FinanceRecord>() {
        override fun areItemsTheSame(oldItem: FinanceRecord, newItem: FinanceRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FinanceRecord, newItem: FinanceRecord): Boolean {
            return oldItem == newItem
        }
    }
}