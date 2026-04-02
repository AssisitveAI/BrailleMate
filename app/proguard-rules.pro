# Proguard rules for BrailleMate

# Keep Room entities
-keep class kr.ac.kaist.aailab.braillemate.android.data.local.entity.** { *; }

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Gemini SDK
-keep class com.google.ai.client.generativeai.** { *; }

# General
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
