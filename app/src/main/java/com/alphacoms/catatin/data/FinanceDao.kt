package com.alphacoms.catatin.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_records ORDER BY createdAt DESC")
    fun getAllFinanceRecords(): LiveData<List<FinanceRecord>>

    @Query("SELECT * FROM finance_records WHERE type = 'INCOME' ORDER BY createdAt DESC")
    fun getIncomeRecords(): LiveData<List<FinanceRecord>>

    @Query("SELECT * FROM finance_records WHERE type = 'EXPENSE' ORDER BY createdAt DESC")
    fun getExpenseRecords(): LiveData<List<FinanceRecord>>

    @Query("SELECT SUM(amount) FROM finance_records WHERE type = 'INCOME'")
    fun getTotalIncome(): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM finance_records WHERE type = 'EXPENSE'")
    fun getTotalExpense(): LiveData<Double?>

    @Query("SELECT * FROM finance_records WHERE id = :id")
    suspend fun getFinanceRecordById(id: Long): FinanceRecord?

    @Insert
    suspend fun insertFinanceRecord(record: FinanceRecord): Long

    @Update
    suspend fun updateFinanceRecord(record: FinanceRecord)

    @Delete
    suspend fun deleteFinanceRecord(record: FinanceRecord)

    @Query("SELECT category, SUM(amount) as total FROM finance_records WHERE type = :type GROUP BY category ORDER BY total DESC")
    suspend fun getCategoryTotals(type: TransactionType): List<CategoryTotal>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)