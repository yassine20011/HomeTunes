# HomeTunes Android App

Native Android application for HomeTunes, built with modern Android development practices.

## Features

- **YouTube Processing**: Paste a link to download audio to your server.
- **Offline Library**: Automatically syncs and saves music for offline listening.
- **Smart Player**: Background playback, media notification controls, and lock screen integration.
- **Modern UI**: Clean, responsive interface built with Jetpack Compose.
- **Purely Local**: Connects directly to your home server—no cloud accounts required.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Asynchronous**: Coroutines & Flow
- **Network**: Retrofit & OkHttp
- **Database**: Room
- **Image Loading**: Coil
- **Audio**: Media3 (ExoPlayer)

## Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- Android Device or Emulator (API Level 26+)

## Setup & Build

1. **Clone the repository** (if you haven't already)
   ```bash
   git clone <repo-url>
   cd HomeTunes/mobile-android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open"
   - Navigate to `HomeTunes/mobile-android` and select it

3. **Build the project**
   - Wait for Gradle sync to complete
   - Run the app on your device/emulator (Shift+F10)
   - Or build via command line:
     ```bash
     ./gradlew assembleDebug
     ```

## Configuration

The app requires the backend server address.
1. Launch the app.
2. Go to the **Settings** screen.
3. Enter your server's IP address and port (e.g., `192.168.1.100:8000`).
4. Save the configuration.

## Troubleshooting

- **Build Fails**: Ensure you are using JDK 17 (Settings > Build, Execution, Deployment > Build Tools > Gradle).
- **Network Error**: Make sure your phone/emulator is on the same network as the server, or use `10.0.2.2:8000` if using the Android Emulator on the same machine as the server.
