package com.alphacoms.catatin.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.alphacoms.catatin.R
import com.alphacoms.catatin.data.PreferenceHelper
import java.io.File
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var tvUserName: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var imgProfileSettings: ImageView

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveProfileImage(uri)
            }
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk memilih foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferenceHelper = PreferenceHelper(this)
        initViews()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserRole = findViewById(R.id.tvUserRole)
        imgProfileSettings = findViewById(R.id.imgProfileSettings)
    }

    private fun setupClickListeners() {
        // Back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Profile image click
        findViewById<View>(R.id.layoutProfileImage).setOnClickListener {
            checkPermissionAndPickImage()
        }

        // Edit profile
        findViewById<View>(R.id.layoutEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        // About
        findViewById<View>(R.id.layoutAbout).setOnClickListener {
            showAboutDialog()
        }

        // Clear data
        findViewById<View>(R.id.layoutClearData).setOnClickListener {
            showClearDataDialog()
        }
    }

    private fun loadUserProfile() {
        tvUserName.text = preferenceHelper.getUserName()
        tvUserRole.text = preferenceHelper.getUserRole()
        loadProfileImage()
    }

    private fun loadProfileImage() {
        val profileImagePath = preferenceHelper.getProfileImagePath()
        if (profileImagePath.isNotEmpty()) {
            val file = File(profileImagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(profileImagePath)
                imgProfileSettings.setImageBitmap(bitmap)
                return
            }
        }
        // Set default image
        imgProfileSettings.setImageResource(android.R.drawable.sym_def_app_icon)
    }

    private fun checkPermissionAndPickImage() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
            else -> openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfileImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Save to internal storage
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            // Save path to preferences
            preferenceHelper.setProfileImagePath(file.absolutePath)

            // Update UI
            imgProfileSettings.setImageBitmap(bitmap)
            Toast.makeText(this, "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal menyimpan foto", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etProfileName)
        val etRole = dialogView.findViewById<EditText>(R.id.etProfileRole)

        etName.setText(preferenceHelper.getUserName())
        etRole.setText(preferenceHelper.getUserRole())

        AlertDialog.Builder(this)
            .setTitle("Edit Profil")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString().trim()
                val role = etRole.text.toString().trim()

                if (name.isNotEmpty()) {
                    preferenceHelper.setUserName(name)
                    preferenceHelper.setUserRole(role.ifEmpty { "Pengguna CatatIn" })
                    loadUserProfile()
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tentang CatatIn")
            .setMessage("CatatIn v1.0.0\n\nAplikasi pencatatan all-in-one untuk:\n• Catatan\n• To-Do List\n• Keuangan\n\n© 2025 Alphacoms")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Semua Data")
            .setMessage("Apakah Anda yakin ingin menghapus semua data? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                // Reset preferences
                preferenceHelper.setUserName("User")
                preferenceHelper.setUserRole("Pengguna CatatIn")
                preferenceHelper.setProfileImagePath("")
                loadUserProfile()
                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
