package com.alphacoms.catatin.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceHelper(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "catatin_preferences"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_TODO_CATEGORIES = "todo_categories"
        private const val KEY_PROFILE_IMAGE_PATH = "profile_image_path"
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
        sharedPreferences.edit { putBoolean(KEY_FIRST_LAUNCH, false) }
    }
    
    /**
     * Reset first launch flag (for testing purposes)
     */
    fun resetFirstLaunch() {
        sharedPreferences.edit { putBoolean(KEY_FIRST_LAUNCH, true) }
    }
    
    /**
     * Get user name
     */
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "User") ?: "User"
    }
    
    /**
     * Set user name
     */
    fun setUserName(name: String) {
        sharedPreferences.edit { putString(KEY_USER_NAME, name) }
    }
    
    /**
     * Get user role
     */
    fun getUserRole(): String {
        return sharedPreferences.getString(KEY_USER_ROLE, "Pengguna CatatIn") ?: "Pengguna CatatIn"
    }
    
    /**
     * Set user role
     */
    fun setUserRole(role: String) {
        sharedPreferences.edit { putString(KEY_USER_ROLE, role) }
    }
    
    /**
     * Get profile image path
     */
    fun getProfileImagePath(): String {
        return sharedPreferences.getString(KEY_PROFILE_IMAGE_PATH, "") ?: ""
    }
    
    /**
     * Set profile image path
     */
    fun setProfileImagePath(path: String) {
        sharedPreferences.edit { putString(KEY_PROFILE_IMAGE_PATH, path) }
    }
    
    /**
     * Get saved todo categories
     */
    fun getCategories(): Set<String> {
        return sharedPreferences.getStringSet(KEY_TODO_CATEGORIES, setOf("Tugas", "Agenda")) 
            ?: setOf("Tugas", "Agenda")
    }
    
    /**
     * Save todo categories
     */
    fun saveCategories(categories: Set<String>) {
        sharedPreferences.edit { putStringSet(KEY_TODO_CATEGORIES, categories) }
    }
}
