# PHEONIX — LRMA CAMERA

Native Android camera app in Kotlin, Jetpack Compose, and CameraX. This repository currently contains the first runnable milestone: a dark camera preview, photo capture to the system gallery, front/rear switching, pinch zoom, and runtime camera capability reporting.

## Build and run

1. Open this repository in the latest Android Studio (JDK 17 required).
2. Let Android Studio sync Gradle.
3. Connect an Android 8.0+ device or start an emulator with a camera.
4. Run the `app` configuration. Grant camera access when prompted.

Or, with Android SDK and Gradle installed, run `gradle :app:assembleDebug` from the repository root. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Current scope

This is the initial capture/preview milestone, not the complete professional camera feature set. Unsupported camera options are not shown. Pro controls, analysis overlays, video, effects, gallery/editor, and settings are planned follow-on milestones. No ads, subscriptions, or paywalls are included.

Minimum SDK: 26. Target SDK: 35. The app requests camera access only for this milestone; audio, location, and media permissions will be requested only when the corresponding feature is implemented and enabled.

## Project layout

- `app/src/main/java/com/pheonix/camera/` — app entry, Compose UI, and runtime camera capability discovery
- `app/src/main/AndroidManifest.xml` — app and camera declarations
- `app/build.gradle.kts` — Android, Compose, and CameraX dependencies
