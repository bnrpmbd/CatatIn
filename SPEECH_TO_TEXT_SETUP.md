# 🎤 Setup Google Cloud Speech-to-Text API

Untuk menggunakan fitur **import audio file** dengan transcription yang sebenarnya, Anda perlu mengatur Google Cloud Speech-to-Text API.

## 📋 Langkah-langkah Setup

### 1. Buat Google Cloud Project
1. Kunjungi [Google Cloud Console](https://console.cloud.google.com/)
2. Buat project baru atau pilih project yang sudah ada
3. Aktifkan **Speech-to-Text API**

### 2. Dapatkan API Key
1. Di Google Cloud Console, pergi ke **APIs & Services** > **Credentials**
2. Klik **Create Credentials** > **API Key**
3. Copy API key yang dihasilkan

### 3. Konfigurasi di Aplikasi
1. Buka file `app/src/main/res/values/config.xml`
2. Ganti `YOUR_GOOGLE_CLOUD_API_KEY_HERE` dengan API key Anda:
```xml
<string name="google_cloud_api_key" translatable="false">AIzaSyC-abcd1234567890...</string>
```
3. Ubah demo mode menjadi false:
```xml
<bool name="demo_mode">false</bool>
```

### 4. Setup Billing (Optional untuk Production)
- Google Cloud Speech-to-Text memiliki free tier 60 menit per bulan
- Untuk usage lebih besar, setup billing account

## 🎯 Fitur yang Tersedia

### Mode Demo (Default)
- **Tanpa API Key**: Aplikasi akan berjalan dalam mode demo
- **Simulasi Transcription**: Menampilkan hasil simulasi untuk testing
- **File Validation**: Tetap memvalidasi format dan ukuran file
- **UI Testing**: Semua fitur UI dapat ditest

### Mode Production (Dengan API Key)
- **Real Transcription**: Menggunakan Google Cloud Speech-to-Text
- **Multi-language Support**: Indonesia & English
- **High Accuracy**: Akurasi tinggi untuk berbagai jenis audio
- **Advanced Features**: Punctuation, word timing, confidence score

## 📁 Format Audio yang Didukung

| Format | Extension | Quality | Recommended |
|--------|-----------|---------|-------------|
| **MP3** | .mp3 | Good | ✅ Terbaik untuk YouTube downloads |
| **WAV** | .wav | Excellent | ✅ Kualitas terbaik |
| **M4A** | .m4a | Good | ✅ iOS recordings |
| **AAC** | .aac | Good | ✅ Android recordings |
| **OGG** | .ogg | Good | ✅ Open source format |

## 🎬 Cara Menggunakan dengan File YouTube

### 1. Download Audio dari YouTube
```bash
# Menggunakan yt-dlp (recommended)
yt-dlp -x --audio-format mp3 "https://youtube.com/watch?v=VIDEO_ID"

# Atau menggunakan youtube-dl
youtube-dl -x --audio-format mp3 "https://youtube.com/watch?v=VIDEO_ID"
```

### 2. Transfer ke Android
- Copy file .mp3 ke folder Downloads di Android
- Atau gunakan cloud storage (Google Drive, Dropbox)

### 3. Import di Aplikasi
1. Buka **CatatIn** > **Voice to Text**
2. Tap tombol **"Import Audio"**
3. Pilih file .mp3 dari file manager
4. Tunggu proses transcription selesai
5. Edit hasil jika perlu dan save

## ⚙️ Troubleshooting

### File Tidak Bisa Diimport
- **Check Format**: Pastikan file format didukung (.mp3, .wav, dll)
- **Check Size**: Maksimal 50MB per file
- **Check Duration**: Maksimal 10 menit per file
- **Check Permissions**: Berikan izin storage access

### Transcription Tidak Akurat
- **Background Noise**: Gunakan audio dengan noise minimal
- **Speech Clarity**: Pastikan speech jelas dan tidak terlalu cepat
- **Language**: Aplikasi optimized untuk Bahasa Indonesia & English
- **Audio Quality**: Gunakan bitrate minimal 128kbps

### Error "API Key Invalid"
- Pastikan API key benar dan aktif
- Check Google Cloud billing status
- Pastikan Speech-to-Text API sudah diaktifkan

## 💡 Tips untuk Hasil Terbaik

### Audio Quality
- **Bitrate**: Minimal 128kbps untuk MP3
- **Sample Rate**: 16kHz atau 44.1kHz
- **Channels**: Mono/Stereo keduanya didukung
- **Background**: Minimal background noise

### Content Type
- **Speech**: Optimized untuk human speech
- **Language**: Indonesia & English paling akurat
- **Pace**: Speech normal speed (tidak terlalu cepat/lambat)
- **Volume**: Volume consistent sepanjang audio

## 🔒 Privacy & Security

- **Local Processing**: File audio diproses lokal sebelum dikirim ke API
- **Temporary Storage**: Audio tidak disimpan permanent di cloud
- **API Communication**: Encrypted HTTPS communication
- **No Data Retention**: Google tidak menyimpan audio untuk training

## 📊 Pricing (Google Cloud)

- **Free Tier**: 60 menit/bulan gratis
- **Standard Pricing**: $0.006 per 15-second increment
- **Data Logging**: Disable untuk privacy (no additional cost)

## 📞 Support

Jika mengalami masalah dengan setup API:
1. Check [Google Cloud Documentation](https://cloud.google.com/speech-to-text/docs)
2. Buka issue di GitHub repository
3. Contact developer di README.md