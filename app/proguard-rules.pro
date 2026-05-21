# WeatherSnap Proguard Rules

# ----------------------------------------------------------------------------
# Kotlinx Serialization
# ----------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,allowoptimization class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# ----------------------------------------------------------------------------
# Room Database
# ----------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * extends androidx.room.RoomDatabase$Callback
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
}
-dontwarn androidx.room.paging.**

# ----------------------------------------------------------------------------
# Retrofit
# ----------------------------------------------------------------------------
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# ----------------------------------------------------------------------------
# Hilt / Dagger
# ----------------------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager

# ----------------------------------------------------------------------------
# Compose
# ----------------------------------------------------------------------------
-keep class androidx.compose.** { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
}

# ----------------------------------------------------------------------------
# Coil
# ----------------------------------------------------------------------------
-keep class coil.** { *; }

# ----------------------------------------------------------------------------
# WorkManager
# ----------------------------------------------------------------------------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
