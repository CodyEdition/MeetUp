# Meet Up

A Java Android app for discovering and managing meetups.

## Environment Setup

1. **Install Android Studio**  
   Use a recent stable release that supports **Android Gradle Plugin 9.x** and **API level 36** (the project’s compile and target SDK).

2. **Install SDK Components**  
   In **Settings / Preferences → Languages & Frameworks → Android SDK**:
   - **SDK Platforms**: install **Android API 36**.
   - **SDK Tools**: keep **Android SDK Build-Tools** updated to the version the sync prompts for (AGP will request a minimum).

3. **JDK for Gradle**  
   - Prefer **Android Studio’s bundled JDK (JBR)** for builds: **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**.  
   - App source is compiled with **Java 11**.

4. **Open the Project**
   **File → Open** and select the repository root `MeetUp`. Let Gradle sync finish.

5. **Run on a Emulator**  
   - **minSdk**: 28  
   - **targetSdk** / **compileSdk**: 36

## Project Dependencies

Dependencies are declared in `app/build.gradle.kts` and versioned in `gradle/libs.versions.toml`.

| Used For | Libraries |
|------|----------------------------------|
| **Local Database** | Room (`room-runtime`, `room-compiler`) |
| **Authentication** | Firebase Authentication (`firebase-auth`) |

Repositories are centralized in `settings.gradle.kts` (`google()`, `mavenCentral()`).

## Configuration

**Application and SDK** (`app/build.gradle.kts`)

- `applicationId`: `com.meetup`
- `namespace`: `com.meetup`
- `minSdk`: 28, `targetSdk`: 36, `compileSdk`: 36 (The API 36 extension level used in the build file)

**Gradle properties** (`gradle.properties`)

- `android.useAndroidX=true` — AndroidX enabled.
- `org.gradle.jvmargs` — JVM memory and encoding for the Gradle's daemon.
- `org.gradle.configuration-cache` — currently disabled for predictable sync

**Firebase**

- The app applies the Google Services plugin and uses Firebase Auth. Make sure `app/google-services.json` matches the Firebase Android app (package name `com.meetup`). Replace it with the file from the [Firebase Console](https://console.firebase.google.com/) for the project.