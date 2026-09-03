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

# Keep Google Gson & Retrofit
-keep class com.google.gson.** { *; }
-keepclasseswithmembernames class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

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

# Preserve enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
