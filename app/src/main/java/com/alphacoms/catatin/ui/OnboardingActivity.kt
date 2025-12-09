package com.alphacoms.catatin.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.alphacoms.catatin.MainActivity
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.PreferenceHelper
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var layoutIndicators: LinearLayout
    private lateinit var btnSkip: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var preferenceHelper: PreferenceHelper

    private val onboardingItems = listOf(
        OnboardingItem(
            icon = android.R.drawable.ic_menu_edit,
            title = "Catatan",
            description = "Buat dan simpan catatan penting Anda dengan mudah. Tulis ide, pengingat, atau apapun yang perlu Anda ingat."
        ),
        OnboardingItem(
            icon = android.R.drawable.ic_btn_speak_now,
            title = "Voice Note",
            description = "Rekam catatan Anda dengan suara! Ubah suara menjadi teks secara langsung, cepat dan praktis tanpa perlu mengetik."
        ),
        OnboardingItem(
            icon = android.R.drawable.ic_menu_my_calendar,
            title = "To-Do List",
            description = "Atur tugas dan kegiatan harian Anda. Tandai prioritas, set deadline, dan centang saat selesai untuk produktivitas maksimal."
        ),
        OnboardingItem(
            icon = android.R.drawable.ic_menu_report_image,
            title = "Keuangan",
            description = "Catat pemasukan dan pengeluaran Anda. Pantau kondisi keuangan dengan mudah dan kelola budget dengan lebih baik."
        ),
        OnboardingItem(
            icon = android.R.drawable.ic_menu_search,
            title = "Siap Memulai!",
            description = "Semua fitur CatatIn siap membantu Anda mengatur hidup lebih terorganisir. Mari mulai mencatat!"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Hide action bar
        supportActionBar?.hide()

        preferenceHelper = PreferenceHelper(this)

        initViews()
        setupViewPager()
        setupIndicators()
        setupButtons()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        layoutIndicators = findViewById(R.id.layoutIndicators)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
                updateButtons(position)
            }
        })
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onboardingItems.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }

        // Set first indicator as active
        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_active)
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
                )
            }
        }
    }

    private fun updateButtons(position: Int) {
        if (position == onboardingItems.size - 1) {
            // Last page
            btnNext.text = "Mulai"
            btnSkip.visibility = View.GONE
        } else {
            btnNext.text = "Selanjutnya"
            btnSkip.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        btnSkip.setOnClickListener {
            finishOnboarding()
        }

        btnNext.setOnClickListener {
            val current = viewPager.currentItem
            if (current < onboardingItems.size - 1) {
                viewPager.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        // Mark first launch as complete
        preferenceHelper.setFirstLaunchComplete()

        // Navigate to main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
