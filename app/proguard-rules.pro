# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose/Coroutines reflection
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keep class kotlinx.coroutines.** { *; }

# Obfuscate everything else heavily
-renamesourcefileattribute SourceFile
-keepattributes Signature,Annotation,*Annotation*,InnerClasses,EnclosingMethod

# Do NOT keep the BotCore inner paths, let them be obfuscated to 'a.b.c' 
# to prevent easy reverse engineering!

# Strip logs in release build to prevent leaking class/method names via strings
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Google Play Services Ads
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.**

# Google ML Kit (Text Recognition) - CRITICAL for Bot logic
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-dontwarn com.google.mlkit.**

# Allow our logic classes to be fully obfuscated and minified
-keep,allowobfuscation,allowshrinking class com.botgta.autobot.logic.** { *; }
-keep,allowobfuscation,allowshrinking class com.botgta.autobot.core.** { *; }
