# Keep application entry points and Room-generated implementations used by reflection.
-keep class com.example.timewallet.** { *; }

# Keep Room database interfaces/entities and generated implementations.
-keep class com.example.timewallet.data.** { *; }

# Do not globally suppress warnings in release builds.
