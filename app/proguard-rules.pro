# ---- Anthropic Java SDK (Jackson + Kotlin reflection) ----
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*,RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keep class kotlin.Metadata { *; }

# Request/response models used for the Messages API are (de)serialised reflectively.
-keep class com.anthropic.models.messages.** { *; }
-keep class com.anthropic.core.** { *; }
-keep class com.anthropic.errors.** { *; }
-keep class com.anthropic.client.** { *; }
# For any other SDK class that survives shrinking, keep the Jackson-annotated members.
-keepclassmembers class com.anthropic.** {
    @com.fasterxml.jackson.annotation.* <fields>;
    @com.fasterxml.jackson.annotation.* <methods>;
    <init>(...);
}

# Jackson
-keep class com.fasterxml.jackson.databind.ObjectMapper { *; }
-keep class com.fasterxml.jackson.module.kotlin.** { *; }
-keep class com.fasterxml.jackson.datatype.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }

# Optional/compile-only dependencies referenced by the SDK and its libraries.
-dontwarn java.beans.**
-dontwarn javax.**
-dontwarn org.slf4j.**
-dontwarn com.github.victools.**
-dontwarn io.swagger.**
-dontwarn io.opentelemetry.**
-dontwarn com.standardwebhooks.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.reflect.jvm.internal.**
