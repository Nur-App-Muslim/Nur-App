# ProGuard / R8 Rules for NurApp

-dontusemixedcaseclassnames
-verbose

# Preserve line numbers for stack traces
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Preserve Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Room
-keep class androidx.room.** { *; }
-keepclasseswithmembernames class androidx.room.** { *; }
-dontwarn androidx.room.paging.**

# Keep App Data & Domain models
-keep class com.sajda.app.data.local.entity.** { *; }
-keep class com.sajda.app.data.local.** { *; }
-keep class com.sajda.app.domain.model.** { *; }
-keep class com.sajda.app.data.remote.** { *; }

# Keep Google Gson & TypeToken
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }

# Keep App Utilities & Data Loaders
-keep class com.sajda.app.util.** { *; }
-keepclassmembers class com.sajda.app.util.** { *; }


# Keep Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-keepclasseswithmembernames class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Dagger / Hilt
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class androidx.hilt.work.** { *; }
-dontwarn dagger.hilt.**

# Keep WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Keep Kotlinx Serialization & Navigation Routes
-keep class com.sajda.app.ui.navigation.** { *; }
-keepclassmembers class com.sajda.app.ui.navigation.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class * implements kotlinx.serialization.KSerializer { *; }
-keepclassmembers class **$$serializer {
    *;
}
-keepclassmembers class * {
    *** Companion;
}
-dontnote kotlinx.serialization.SerializationKt

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

