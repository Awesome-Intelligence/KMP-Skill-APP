# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Application class
-keep class com.androidkit.app.AndroidKitApp { *; }

# Keep Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep DataStore（只保留 dontwarn：Preferences API 无反射，不需要 keep）
-dontwarn androidx.datastore.**

# Keep annotations
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep model classes（字段名不能被改）
-keep class com.androidkit.app.data.model.** { *; }

# Gson DTO：字段靠 @SerializedName 注解值，成员不能被删
# Retrofit 的泛型/注解反射规则由 retrofit2.pro 自带，无需重复声明
-keepclassmembers,allowobfuscation class com.androidkit.app.data.ai.providers.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 不要加 -keep class androidx.compose.** { *; }
# Compose 各 AAR 自带 consumer 规则（proguard.txt），AGP 自动应用。
# 全量 keep 会让 R8 完全无法收缩：曾保留 5.3 万个类、154 万个成员，
# R8 单任务耗时 9m36s，APK 10.4MB。去掉后由 R8 只保留实际用到的 95 个图标。
