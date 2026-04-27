# Keep Ktor Client
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Keep Room
-keep class androidx.room.** { *; }

# Keep ZXing
-keep class com.google.zxing.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
