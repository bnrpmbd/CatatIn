# Add project specific ProGuard rules here.

# Keep data classes for Room
-keep class com.alphacoms.catatin.data.** { *; }

# Keep Room classes
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep speech recognition classes
-keep class android.speech.** { *; }

# Keep OkHttp classes
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*