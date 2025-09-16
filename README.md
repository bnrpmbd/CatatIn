# 📱 CatatIn - Aplikasi Catatan Pintar

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)

**CatatIn** adalah aplikasi Android modern yang menggabungkan tiga fitur utama untuk membantu produktivitas harian Anda: catatan voice-to-text, manajemen to-do list, dan tracking keuangan.

## ✨ Fitur Utama

### 🎤 Voice to Text Notes
- **Speech Recognition**: Convert suara langsung ke teks menggunakan microphone
- **Audio File Upload**: Import file audio (.mp3, .wav, .m4a) dan convert ke teks
- **Smart Transcription**: Deteksi bahasa otomatis (Indonesia & Inggris)
- **Edit & Save**: Edit hasil transcription dan simpan ke database lokal

### ✅ To-Do List Management
- **CRUD Operations**: Create, Read, Update, Delete tasks
- **Priority System**: Low, Normal, High, Urgent dengan color coding
- **Due Date**: Set deadline dengan date picker
- **Completion Tracking**: Mark tasks sebagai complete/incomplete
- **Smart Sorting**: Otomatis sort berdasarkan prioritas dan tanggal

### 💰 Finance Tracker
- **Income & Expense**: Track pemasukan dan pengeluaran
- **Category Management**: Kategori otomatis berdasarkan jenis transaksi
- **Dashboard**: Real-time summary total income, expense, dan balance
- **Currency Format**: Format Rupiah yang user-friendly
- **Transaction History**: Riwayat lengkap dengan filter

## 📱 Screenshots

| Main Dashboard | Voice Notes | To-Do List | Finance Tracker |
|----------------|-------------|------------|-----------------|
| ![Main](screenshots/main.png) | ![Voice](screenshots/voice.png) | ![Todo](screenshots/todo.png) | ![Finance](screenshots/finance.png) |

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room SQLite
- **UI Framework**: Material Design 3
- **Speech Recognition**: Android Speech API + Google Cloud Speech-to-Text
- **Audio Processing**: MediaRecorder & MediaPlayer
- **Async Operations**: Coroutines + LiveData
- **Dependency Injection**: Manual DI

## 📋 Requirements

### Minimum Requirements
- **Android**: 7.0 (API Level 24+)
- **RAM**: 2GB
- **Storage**: 50MB free space
- **Permissions**: Microphone, Storage access

### Recommended
- **Android**: 10.0+ untuk performa optimal
- **RAM**: 4GB+
- **Storage**: 100MB+ untuk audio files

### Tested Devices
- ✅ **Redmi Note 10s** (Android 13, MIUI 14.0.5, 8GB RAM)
- ✅ Samsung Galaxy A-Series
- ✅ Google Pixel devices
- ✅ OnePlus devices

## 🚀 Installation

### Via APK (Recommended)
1. Download APK terbaru dari [Releases](https://github.com/username/catatin/releases)
2. Enable "Install from Unknown Sources" di Settings
3. Install APK file
4. Grant required permissions

### Build from Source
```bash
# Clone repository
git clone https://github.com/username/catatin.git
cd catatin

# Open dengan Android Studio
# Sync Gradle dependencies
# Connect device dan Run
```

## 🎯 How to Use

### 🎤 Voice to Text
1. **Live Recording**:
   - Tap "Voice Input" button
   - Speak clearly ke microphone
   - Hasil akan muncul di text field

2. **Audio File Import**:
   - Tap "Import Audio" button
   - Pilih file .mp3/.wav/.m4a
   - Wait for transcription process
   - Edit hasil jika diperlukan

### ✅ To-Do Management
1. Tap FAB (+) untuk add new task
2. Fill title, description, priority, due date
3. Tap task untuk edit
4. Checkbox untuk mark complete
5. Swipe atau long press untuk delete

### 💰 Finance Tracking
1. Tap FAB (+) untuk add transaction
2. Pilih Income/Expense
3. Select category (auto-update based on type)
4. Enter amount dan description
5. View summary di dashboard

## 🔧 Configuration

### API Keys (Optional untuk advanced features)
```kotlin
// app/src/main/res/values/config.xml
<resources>
    <string name="google_cloud_api_key">YOUR_API_KEY</string>
    <string name="speech_service_url">YOUR_SERVICE_URL</string>
</resources>
```

### Supported Audio Formats
- **.mp3** (Recommended)
- **.wav** (High quality)
- **.m4a** (iOS compatible)
- **.aac** (Android native)

## 📂 Project Structure

```
app/src/main/
├── java/com/alphacoms/catatin/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── entities/
│   │   │   ├── Note.kt
│   │   │   ├── ToDo.kt
│   │   │   └── FinanceRecord.kt
│   │   ├── dao/
│   │   │   ├── NoteDao.kt
│   │   │   ├── ToDoDao.kt
│   │   │   └── FinanceDao.kt
│   │   ├── database/
│   │   │   └── AppDatabase.kt
│   │   └── repository/
│   │       └── AppRepository.kt
│   ├── ui/
│   │   ├── voice/
│   │   │   ├── VoiceNoteActivity.kt
│   │   │   └── VoiceNoteAdapter.kt
│   │   ├── todo/
│   │   │   ├── ToDoListActivity.kt
│   │   │   └── ToDoAdapter.kt
│   │   └── finance/
│   │       ├── FinanceActivity.kt
│   │       └── FinanceAdapter.kt
│   └── utils/
│       ├── AudioProcessor.kt
│       ├── SpeechRecognitionHelper.kt
│       └── FileUtils.kt
└── res/
    ├── layout/
    ├── values/
    └── drawable/
```

## 🔒 Permissions

```xml
<!-- Required -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- Optional -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🛡️ Privacy & Security

- **Local Storage**: Semua data disimpan lokal di device
- **No Cloud Sync**: Data tidak dikirim ke server external
- **Encrypted Database**: Room database dengan encryption
- **Permission Based**: Hanya request permission yang diperlukan
- **Open Source**: Code dapat di-audit untuk security review

## 🤝 Contributing

Kontribusi sangat welcome! Silakan:

1. Fork repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests untuk new features
- Update documentation
- Test pada minimal 2 device types

## 📝 Changelog

### v1.0.0 (2025-09-16)
- ✨ Initial release
- 🎤 Voice to text dengan live recording
- 📁 Audio file import support
- ✅ Complete to-do list management
- 💰 Finance tracking dengan dashboard
- 🎨 Material Design 3 UI

### Planned Features (v1.1.0)
- 🌐 Multi-language transcription
- 📊 Export data to CSV/PDF
- 🔄 Data backup & restore
- 🌙 Dark mode support
- 📱 Widget untuk quick access

## 🐛 Known Issues

- Audio file size limit: 50MB
- Speech recognition accuracy depends on background noise
- MIUI devices mungkin perlu whitelist app untuk background processing

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/username/catatin/issues)
- **Email**: developer@alphacoms.com
- **Documentation**: [Wiki](https://github.com/username/catatin/wiki)

## 📄 License

```
MIT License

Copyright (c) 2025 Alpha Coms

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

- [Material Design](https://material.io/design) untuk design system
- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture) untuk best practices
- [Room Database](https://developer.android.com/training/data-storage/room) untuk local storage
- [Google Speech-to-Text](https://cloud.google.com/speech-to-text) untuk transcription service

---

⭐ **Jangan lupa kasih star jika project ini membantu!** ⭐