// This file is created to force Room annotation processing
// to generate the required AppDatabase_Impl and related classes
package com.alphacoms.catatin.data

import androidx.room.Room
import android.content.Context

class DatabaseHelper {
    companion object {
        fun createDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "catatin_database"
            ).build()
        }
    }
}