# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep accessibility service
-keep class com.shortslock.app.service.ShortsDetectionService { *; }
-keep class com.shortslock.app.service.MonitoringForegroundService { *; }

# Keep managers
-keep class com.shortslock.app.manager.** { *; }
-keep class com.shortslock.app.data.** { *; }

# Keep UI classes
-keep class com.shortslock.app.ui.** { *; }
