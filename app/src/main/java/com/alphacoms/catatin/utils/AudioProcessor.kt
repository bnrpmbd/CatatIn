package com.alphacoms.catatin.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AudioProcessor(private val context: Context) {

    companion object {
        private const val SPEECH_TO_TEXT_URL = "https://speech.googleapis.com/v1/speech:recognize"
        private const val MAX_AUDIO_SIZE = 50 * 1024 * 1024 // 50MB
        private val SUPPORTED_FORMATS = listOf("mp3", "wav", "m4a", "aac", "ogg")
    }

    data class TranscriptionResult(
        val success: Boolean,
        val text: String = "",
        val confidence: Float = 0f,
        val error: String = ""
    )

    /**
     * Check if audio file is supported
     */
    fun isAudioFileSupported(uri: Uri): Boolean {
        return try {
            val fileName = getFileName(uri)
            val extension = fileName.substringAfterLast(".", "").lowercase()
            SUPPORTED_FORMATS.contains(extension)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get audio file duration
     */
    fun getAudioDuration(uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            duration?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get audio file size
     */
    fun getAudioFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Validate audio file before processing
     */
    fun validateAudioFile(uri: Uri): Pair<Boolean, String> {
        if (!isAudioFileSupported(uri)) {
            return false to "Format file tidak didukung. Gunakan: ${SUPPORTED_FORMATS.joinToString(", ")}"
        }

        val fileSize = getAudioFileSize(uri)
        if (fileSize > MAX_AUDIO_SIZE) {
            return false to "File terlalu besar. Maksimal 50MB"
        }

        if (fileSize == 0L) {
            return false to "File tidak valid atau kosong"
        }

        val duration = getAudioDuration(uri)
        if (duration > 600000) { // 10 minutes
            return false to "Durasi audio terlalu panjang. Maksimal 10 menit"
        }

        return true to "File valid"
    }

    /**
     * Transcribe audio file using Google Speech-to-Text API
     * Note: Requires API key untuk production use
     */
    suspend fun transcribeAudioFile(
        uri: Uri,
        apiKey: String? = null
    ): TranscriptionResult = withContext(Dispatchers.IO) {
        try {
            // Validate file first
            val (isValid, message) = validateAudioFile(uri)
            if (!isValid) {
                return@withContext TranscriptionResult(false, error = message)
            }

            // For demo purposes, we'll simulate transcription
            // In production, you would use actual API
            if (apiKey.isNullOrEmpty()) {
                return@withContext simulateTranscription(uri)
            }

            // Real Google Speech-to-Text implementation
            return@withContext transcribeWithGoogleAPI(uri, apiKey)

        } catch (e: Exception) {
            TranscriptionResult(false, error = "Error: ${e.message}")
        }
    }

    /**
     * Simulate transcription for demo (without API key)
     */
    private suspend fun simulateTranscription(uri: Uri): TranscriptionResult {
        // Simulate processing time
        kotlinx.coroutines.delay(2000)

        val fileName = getFileName(uri)
        val duration = getAudioDuration(uri)
        val durationSeconds = duration / 1000

        // Generate simulated text based on file info
        val simulatedText = """
            [DEMO MODE - Simulated Transcription]
            
            File: $fileName
            Duration: ${durationSeconds}s
            
            Ini adalah hasil simulasi transcription untuk file audio Anda. 
            Untuk mendapatkan hasil transcription yang sebenarnya, silakan:
            
            1. Daftar Google Cloud Speech-to-Text API
            2. Dapatkan API key
            3. Masukkan API key di pengaturan aplikasi
            
            File audio Anda telah berhasil diproses dan siap untuk di-transcribe dengan API yang sebenarnya.
        """.trimIndent()

        return TranscriptionResult(
            success = true,
            text = simulatedText,
            confidence = 0.95f
        )
    }

    /**
     * Real Google Speech-to-Text transcription (Simplified HTTP implementation)
     */
    private suspend fun transcribeWithGoogleAPI(
        uri: Uri,
        apiKey: String
    ): TranscriptionResult {
        return try {
            // Convert audio to base64
            val audioBytes = readAudioFileBytes(uri)
            val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)

            // Prepare request using Gson
            val config = JsonObject().apply {
                addProperty("encoding", getAudioEncoding(uri))
                addProperty("sampleRateHertz", 16000)
                addProperty("languageCode", "id-ID")
                addProperty("enableAutomaticPunctuation", true)
            }
            
            val audio = JsonObject().apply {
                addProperty("content", base64Audio)
            }
            
            val requestBody = JsonObject().apply {
                add("config", config)
                add("audio", audio)
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()

            val mediaType = "application/json".toMediaType()
            val body = requestBody.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$SPEECH_TO_TEXT_URL?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                parseTranscriptionResponse(responseBody)
            } else {
                TranscriptionResult(false, error = "API Error: ${response.code}")
            }

        } catch (e: Exception) {
            TranscriptionResult(false, error = "Network Error: ${e.message}")
        }
    }

    private fun readAudioFileBytes(uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: byteArrayOf()
    }

    private fun getAudioEncoding(uri: Uri): String {
        val fileName = getFileName(uri)
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "wav" -> "LINEAR16"
            "mp3" -> "MP3"
            "m4a" -> "MP3"
            "aac" -> "MP3"
            "ogg" -> "OGG_OPUS"
            else -> "MP3"
        }
    }

    private fun parseTranscriptionResponse(responseBody: String): TranscriptionResult {
        return try {
            val gson = Gson()
            val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
            val results = jsonResponse.getAsJsonArray("results")

            if (results != null && results.size() > 0) {
                val firstResult = results[0].asJsonObject
                val alternatives = firstResult.getAsJsonArray("alternatives")
                
                if (alternatives.size() > 0) {
                    val bestAlternative = alternatives[0].asJsonObject
                    val transcript = bestAlternative.get("transcript").asString
                    val confidence = bestAlternative.get("confidence")?.asFloat ?: 0.0f
                    
                    TranscriptionResult(
                        success = true,
                        text = transcript,
                        confidence = confidence
                    )
                } else {
                    TranscriptionResult(false, error = "No transcription alternatives found")
                }
            } else {
                TranscriptionResult(false, error = "No speech detected in audio")
            }
        } catch (e: Exception) {
            TranscriptionResult(false, error = "Failed to parse response: ${e.message}")
        }
    }

    private fun getFileName(uri: Uri): String {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return it.getString(nameIndex)
                    }
                }
            }
            "unknown_audio_file"
        } catch (e: Exception) {
            "unknown_audio_file"
        }
    }
}