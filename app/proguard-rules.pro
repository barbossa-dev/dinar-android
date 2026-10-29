# ---------------------------------------------------------------------------------------------
# Dinar R8 rules. Libraries (Hilt, Room, DataStore, Compose, Navigation, kotlinx.serialization)
# ship their own consumer rules; the rules below only cover what is specific to this app.
# ---------------------------------------------------------------------------------------------

# Keep line numbers for readable crash reports, but hide the original file names.
# Upload app/build/outputs/mapping/release/mapping.txt to the store to deobfuscate.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization: type-safe navigation routes -------------------------------------
# Routes are looked up by KClass at runtime (`hasRoute(Route::class)`, `toRoute<T>()`),
# which resolves their generated serializer reflectively.
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
-dontnote kotlinx.serialization.**

-keep,includedescriptorclasses class app.dinar.**$$serializer { *; }
-keepclassmembers class app.dinar.** {
    *** Companion;
}
-keepclasseswithmembers class app.dinar.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# `data object` routes expose their serializer through INSTANCE.
-keepclassmembers @kotlinx.serialization.Serializable class app.dinar.** {
    public static ** INSTANCE;
}
-keep @kotlinx.serialization.Serializable class app.dinar.**.navigation.** { *; }

# --- Persisted enums --------------------------------------------------------------------------
# Enum names are stored in Room and DataStore. Keep the constants intact so saved data
# keeps matching after every release.
-keepclassmembers enum app.dinar.core.model.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Room entities ----------------------------------------------------------------------------
-keep class app.dinar.core.database.model.** { *; }

# --- Strip debug logging from release builds ------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# --- Annotations referenced by libraries but not needed at runtime ----------------------------
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.coroutines.debug.**
