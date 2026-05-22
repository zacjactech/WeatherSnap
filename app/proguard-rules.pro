# WeatherSnap Proguard Rules

# ----------------------------------------------------------------------------
# Kotlin Reflection
# ----------------------------------------------------------------------------
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.reflect.** { *; }

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
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

# ----------------------------------------------------------------------------
# Hilt / Dagger
# ----------------------------------------------------------------------------
-keep class dagger.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager
-keep class * extends dagger.hilt.android.internal.managers.FragmentComponentManager
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager
-keep class dagger.hilt.internal.aggregatedroot.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keepclassmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <init>(...);
    @dagger.hilt.AndroidEntryPoint *;
    @javax.inject.Inject *;
}
-keep class javax.inject.** { *; }

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

# ----------------------------------------------------------------------------
# Gson
# ----------------------------------------------------------------------------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.weather.core.network.model.** { *; }
-keep class com.weather.core.model.** { *; }
