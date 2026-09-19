# Wichtige Klassen behalten
-keep class com.example.timewallet.** { *; }
-keep class androidx.room.** { *; }

# Alles andere verschleiern
-dontwarn **
