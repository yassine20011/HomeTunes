# HomeTunes 🎵

A self-hosted personal music system with a local home server and mobile app.


## Overview

HomeTunes lets you download music from YouTube and build your personal offline music library. The system consists of:

- **Backend Server**: Python/FastAPI server that downloads and processes YouTube audio (M4A/AAC) running on your local home server.
- **Mobile App**: Native Android app (Kotlin/Jetpack Compose) that manages your library and plays music offline

## Architecture

```
┌─────────────────────┐         ┌─────────────────────┐
│   📱 Mobile App     │◄───────►│   🖥️ Home Server    │
│   (Native Android)  │  WiFi   │   (Python/FastAPI)  │
├─────────────────────┤         ├─────────────────────┤
│ • URL Input         │         │ • YouTube Download  │
│ • Local Library     │         │ • M4A/AAC Processing│
│ • Offline Playback  │         │ • Metadata Extract  │
│ • Background Audio  │         │ • Light Weight      │
└─────────────────────┘         └─────────────────────┘
```

## Quick Start

### 1. Start the Server

**Option A: Using Docker (Recommended)**

```bash
cd backend
docker build -t hometunes-backend .
docker run -p 8000:8000 hometunes-backend
```

**Option B: Manual Setup**

```bash
cd backend

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Start server
python run.py
```

Server will start at `http://0.0.0.0:8000`

### 2. Build the Mobile App

```bash
cd mobile-android

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### 3. Configure the App

1. Open HomeTunes on your phone
2. Go to **Settings** tab
3. Enter your server IP (e.g., `192.168.1.100:8000`)
4. Tap **Save**

### 4. Download Music

1. Copy a YouTube URL
2. Go to **Home** tab
3. Paste the URL
4. Tap **Download**
5. Track appears in your **Library**!

## Requirements

### Server
- Python 3.10+
- FFmpeg (must be installed on system)
- Linux/macOS (any system with FFmpeg)

### Mobile App
- Android Studio Hedgehog+
- JDK 17
- Android phone or emulator (API 26+)

## Project Structure

```
HomeTunes/
├── backend/                 # Python FastAPI server
│   ├── app/
│   │   ├── main.py         # FastAPI app
│   │   ├── routers/        # API endpoints
│   │   └── services/       # yt-dlp downloader
│   ├── requirements.txt
│   └── run.py              # Server startup
│
└── mobile-android/          # Native Android app
    ├── app/src/main/
    │   ├── java/.../       # Kotlin source
    │   │   ├── ui/         # Jetpack Compose screens
    │   │   ├── data/       # Repository & database
    │   │   └── player/     # Media3 audio player
    │   └── res/            # Resources & icons
    └── build.gradle.kts
```

## Features

- ✅ Download audio from YouTube
- ✅ High-quality M4A (AAC) audio (no re-encoding)
- ✅ Metadata extraction (title, artist, thumbnail)
- ✅ Local offline library
- ✅ Background audio playback
- ✅ Media notification controls
- ✅ Custom app icon & splash screen
- ✅ No cloud, no accounts, 100% local

## Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Python 3.11, FastAPI |
| Downloader | yt-dlp, FFmpeg |
| Mobile | Kotlin, Jetpack Compose |
| Audio | Media3 ExoPlayer |
| Storage | Room Database |
| DI | Hilt |

## License

Personal use only. Not for distribution.
