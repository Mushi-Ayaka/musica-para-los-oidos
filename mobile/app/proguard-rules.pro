# Música para los Oídos — reglas R8 para release.

# Metadatos que usan serialización, reflexión de Compose y Media3.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# kotlinx.serialization
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.musica.paralosoidos.**$$serializer { *; }
-keepclassmembers class com.musica.paralosoidos.** { *** Companion; }
-keepclasseswithmembers class com.musica.paralosoidos.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }

# Media3 / ExoPlayer
-dontwarn org.checkerframework.**
-dontwarn org.slf4j.**

# Coil
-dontwarn coil.**
