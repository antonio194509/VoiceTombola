# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep all project classes and JavaScript bridges
-keep class com.anton.voicetombola.** { *; }
-keepclassmembers class com.anton.voicetombola.** { *; }

# AdMob / Google Play Services
-keep public class com.google.android.gms.ads.** {
   public *;
}

# For WebView with JS
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Optimize Coroutines and Compose
-assumenosideeffects class kotlinx.coroutines.DebugKt {
    boolean getASSERTIONS_ENABLED();
}

