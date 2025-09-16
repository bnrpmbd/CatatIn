package com.alphacoms.catatin.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "finance_records")
data class FinanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val description: String = "",
    val createdAt: Date
)

enum class TransactionType {
    INCOME, EXPENSE
}