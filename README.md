# 📱 CatatIn - Aplikasi Catatan Pintar

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)

**CatatIn** adalah aplikasi Android untuk mengelola catatan, to-do list, dan keuangan dalam satu aplikasi.

## ✨ Fitur Utama

### 📝 Catatan Biasa
- Membuat, mengedit, dan menghapus catatan teks
- Interface yang sederhana dan mudah digunakan

### 🎤 Voice to Text Notes
- Speech Recognition untuk convert suara ke teks
- Import file audio (.mp3, .wav, .m4a)
- Edit dan simpan hasil transcription

### ✅ To-Do List
- CRUD operations untuk task management
- Priority system (Low, Normal, High, Urgent)
- Due date dengan date picker
- Mark complete/incomplete

### 💰 Finance Tracker
- Track income dan expense
- Category management
- Dashboard dengan summary balance
- Format Rupiah

## 🛠️ Teknologi

- **Platform**: Android (API 24+)
- **Language**: Kotlin
- **Database**: Room (SQLite)
- **Architecture**: MVVM with LiveData
- **UI**: Material Design Components

## 🏗️ Struktur Project

```
app/
├── src/main/java/com/alphacoms/catatin/
│   ├── data/          # Room database, entities, DAOs
│   ├── ui/            # Activities dan Adapters
│   ├── utils/         # Utility classes
│   └── MainActivity   # Entry point
├── res/
│   ├── layout/        # XML layouts
│   ├── values/        # Colors, strings, themes
│   └── drawable/      # Icons dan images
```

## 📱 Screenshots

*Screenshots akan ditambahkan setelah development selesai*

## 🚀 Instalasi

1. Clone repository
2. Buka di Android Studio
3. Sync Gradle
4. Run di device/emulator

## 📄 License

Project ini dibuat untuk tugas akhir mata kuliah PAPB.