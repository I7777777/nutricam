# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep DataStore
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions

# Keep Model classes
-keep class com.nutricam.model.** { *; }