# ALTEROK Mobile v0.1
Native Kotlin + Jetpack Compose Android engineering dashboard, Paper Circuit Industrial style.

## Build
Open this folder in Android Studio (JDK 17), allow Gradle sync, and run on the Lenovo Idea Tab.
Android Gradle Plugin 8.7.3 / Kotlin 2.0.21; Android SDK 35 required.
This ZIP contains source code, not a compiled APK or Gradle wrapper. Android Studio can use its installed Gradle distribution.

## Current scope
- Landscape Paper Circuit dashboard with HOME / DESIGN / CONTROL / SYSTEM tabs.
- Krita package launcher with Play Store fallback.
- Blender experimental-build website shortcut (package name not stable).
- Materializr launcher with website fallback (verify package ID on device).
- Android document picker and Settings.
- Control deck is intentionally a disconnected placeholder.

## Not yet implemented
Real project library, pen-pressure capture, robotics connections, full installed-app launcher,
default-home integration, tested APK, and automatic STEP/3MF transfer.

## Security
No root, no network permissions, no credentials stored. Keep repo private if it will later hold device configuration.
