package com.alphacoms.catatin.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "catatin_preferences"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
    }
    
    /**
     * Check if this is the first time the app is launched
     */
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    /**
     * Mark that the app has been launched before
     */
    fun setFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
    
    /**
     * Reset first launch flag (for testing purposes)
     */
    fun resetFirstLaunch() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply()
    }
}
