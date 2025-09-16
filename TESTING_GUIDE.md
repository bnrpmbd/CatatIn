# 🧪 Testing Guide - Audio Import Feature

## 📁 Cara Testing Fitur Import Audio

### 1. Mode Demo (Tanpa API Key)
Aplikasi sudah bisa ditest langsung tanpa setup apapun:

```kotlin
// Di config.xml, pastikan demo_mode = true
<bool name="demo_mode">true</bool>
```

**Yang akan terjadi:**
- File audio akan divalidasi (format, size, duration)
- Hasil transcription adalah simulasi berdasarkan info file
- Semua fitur UI berfungsi normal
- Cocok untuk development dan demo

### 2. Sample Audio Files untuk Testing

Anda bisa menggunakan file audio berikut untuk testing:

#### A. Record Voice sendiri
1. Buka **Voice Recorder** di Android
2. Record ucapan: *"Halo, ini adalah test transcription untuk aplikasi CatatIn"*
3. Save as MP3
4. Import ke aplikasi

#### B. Download Sample Audio
```bash
# Contoh download audio speech dari internet
# (Pastikan legal dan tidak melanggar copyright)

# Atau buat sendiri menggunakan Text-to-Speech online
# Website: https://ttsmaker.com/
# Input text: "Selamat datang di aplikasi CatatIn. Aplikasi ini dapat mengubah suara menjadi teks."
# Download sebagai MP3
```

#### C. Convert dari YouTube (Educational Content)
```bash
# Download audio dari video educational/speech
yt-dlp -x --audio-format mp3 --audio-quality 0 "https://youtube.com/watch?v=EDUCATIONAL_VIDEO_ID"

# Atau gunakan online converter:
# 1. Copy YouTube URL video speech/educational
# 2. Paste di website converter online (contoh: y2mate.com)
# 3. Download as MP3
# 4. Transfer ke Android
```

### 3. Testing Steps

#### Step 1: Persiapan File
```
1. Siapkan file audio (.mp3, .wav, .m4a)
2. Pastikan size < 50MB
3. Pastikan duration < 10 menit
4. Copy ke folder Downloads Android
```

#### Step 2: Import Process
```
1. Buka CatatIn > Voice to Text
2. Tap "Import Audio"
3. Pilih file dari file manager
4. Observe:
   - File validation message
   - Processing progress indicator
   - Final transcription result
```

#### Step 3: Verify Results
```
✅ File info ditampilkan (name, size, duration)
✅ Progress indicator muncul saat processing
✅ Result muncul di text field
✅ Bisa edit dan save ke database
✅ Muncul di list notes
```

## 🔧 Testing Different Scenarios

### ✅ Valid Files
- **MP3 (5MB, 2 minutes)**: Should work perfectly
- **WAV (10MB, 1 minute)**: Should work with high quality
- **M4A from iPhone**: Should work for mobile recordings

### ❌ Invalid Files
- **File > 50MB**: Should show "File terlalu besar" error
- **Duration > 10 minutes**: Should show duration error
- **Unsupported format (.txt, .jpg)**: Should show format error
- **Corrupted file**: Should show validation error

### 🎯 Edge Cases
- **Very short audio (< 5 seconds)**: Should work but may have low confidence
- **Background music**: May affect transcription accuracy
- **Multiple speakers**: May reduce accuracy
- **Non-speech audio**: Should return "No speech detected"

## 📊 Expected Results by Audio Type

| Audio Type | Expected Accuracy | Notes |
|------------|------------------|-------|
| **Clear Speech** | 90-95% | Indonesian/English |
| **Phone Recording** | 80-90% | Depends on quality |
| **YouTube Educational** | 85-95% | Usually good quality |
| **Music with Lyrics** | 60-80% | Background music interferes |
| **Multiple Speakers** | 70-85% | May mix speakers |
| **Background Noise** | 50-80% | Depends on noise level |

## 🐛 Common Issues & Solutions

### Issue 1: "Permission Denied"
```
Solution:
1. Grant storage permission
2. Restart app
3. Try import again
```

### Issue 2: "File Not Found"
```
Solution:
1. Check file still exists
2. Try copying to Downloads folder
3. Use different file manager
```

### Issue 3: "Network Error" (Production Mode)
```
Solution:
1. Check internet connection
2. Verify API key in config.xml
3. Check Google Cloud billing
```

### Issue 4: Low Transcription Accuracy
```
Solution:
1. Use clearer audio
2. Reduce background noise
3. Try mono channel audio
4. Check audio bitrate (min 128kbps)
```

## 📱 Device-Specific Testing

### Redmi Note 10s (Your Device)
- **MIUI 14**: May need to whitelist app for file access
- **Storage Access**: Use "Files" app or Mi File Manager
- **Performance**: 8GB RAM should handle large files smoothly
- **Audio Codecs**: All formats supported natively

### Testing Commands
```bash
# Check audio file properties
ffprobe -v quiet -show_format -show_streams "audio_file.mp3"

# Convert audio if needed
ffmpeg -i input.wav -ar 16000 -ac 1 output.mp3
```

## 🎯 Success Criteria

Feature dianggap berhasil jika:

✅ **File Validation**: Semua validasi berjalan dengan benar
✅ **UI Responsiveness**: UI tidak freeze saat processing
✅ **Error Handling**: Error messages yang jelas dan helpful
✅ **Memory Management**: Tidak memory leak dengan file besar
✅ **Demo Mode**: Berfungsi tanpa API key
✅ **Production Mode**: Berfungsi dengan API key valid
✅ **Multi-format Support**: Semua format audio didukung
✅ **Progress Indication**: User tahu status processing

## 📞 Get Help

Jika mengalami masalah saat testing:
1. Check logs di Android Studio Logcat
2. Verify file permissions
3. Test dengan file audio yang berbeda
4. Check device storage space
5. Restart aplikasi jika perlu