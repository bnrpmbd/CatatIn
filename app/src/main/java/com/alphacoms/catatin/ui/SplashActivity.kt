package com.alphacoms.catatin.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.alphacoms.catatin.MainActivity
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.PreferenceHelper

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var preferenceHelper: PreferenceHelper
    private val splashDelay = 2500L // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        preferenceHelper = PreferenceHelper(this)

        // Delay and navigate to appropriate screen
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, splashDelay)
    }

    private fun navigateToNextScreen() {
        val intent = if (preferenceHelper.isFirstLaunch()) {
            // First time launching app - show onboarding
            Intent(this, OnboardingActivity::class.java)
        } else {
            // Not first time - go directly to main activity
            Intent(this, MainActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
