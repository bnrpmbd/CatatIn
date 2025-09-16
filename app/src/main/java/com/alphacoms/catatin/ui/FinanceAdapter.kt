package com.alphacoms.catatin.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
        private val tvTitle: TextView = itemView.findViewById(R.id.tvFinanceTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvFinanceAmount)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvFinanceCategory)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvFinanceDescription)
        private val tvDate: TextView = itemView.findViewById(R.id.tvFinanceDate)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteFinance)

        fun bind(record: FinanceRecord) {
            tvTitle.text = record.title
            
            // Format amount with currency
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val amountText = formatter.format(record.amount)
            tvAmount.text = amountText
            
            // Set color based on transaction type
            tvAmount.setTextColor(
                if (record.type == TransactionType.INCOME) 
                    android.graphics.Color.parseColor("#4CAF50") 
                else 
                    android.graphics.Color.parseColor("#F44336")
            )
            
            tvCategory.text = record.category
            tvDescription.text = record.description
            tvDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(record.createdAt)
            
            // Set listeners
            itemView.setOnClickListener {
                onItemClick(record)
            }
            
            btnDelete.setOnClickListener {
                onDeleteClick(record)
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