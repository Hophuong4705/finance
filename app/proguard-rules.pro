# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Giữ lại class của Retrofit & Gson
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Giữ lại class Data / Entity của bạn để Room và Gson không bị lỗi
-keep class com.example.cuoikyltdd.data.** { *; }
-keep class com.example.cuoikyltdd.domain.model.** { *; }

# Giữ lại iText và POI
-keep class com.itextpdf.** { *; }
-keep class org.apache.poi.** { *; }