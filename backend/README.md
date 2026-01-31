# HomeTunes Backend

Personal music server that downloads YouTube videos as high-quality M4A (AAC) audio.

## Requirements

- Python 3.10+
- FFmpeg (must be installed on system)
- Docker (optional, recommended)

## Setup

```bash
# Create virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or: venv\Scripts\activate  # Windows

# Install dependencies
pip install -r requirements.txt

# Install FFmpeg (if not installed)
# Ubuntu/Debian:
sudo apt install ffmpeg

# Arch:
sudo pacman -S ffmpeg
```

## Run Server

### Option 1: Docker (Recommended)

```bash
# Build the image
docker build -t hometunes-backend .

# Run the container
docker run -p 8000:8000 hometunes-backend

# Run in background
docker run -d -p 8000:8000 --name hometunes hometunes-backend
```

### Option 2: Direct Python

```bash
python run.py
```

Server starts at `http://0.0.0.0:8000`

## API Endpoints

### GET /health
Health check for mobile app connection.

```bash
curl http://localhost:8000/health
```

### POST /download
Download YouTube audio.

```bash
curl -X POST http://localhost:8000/download \
  -H "Content-Type: application/json" \
  -d '{"url": "https://youtube.com/watch?v=VIDEO_ID", "quality": "192"}' \
  -o output.m4a
```

Response format:
- First line: JSON metadata
- Rest: M4A binary data

## Environment Variables

- `HOMETUNES_HOST`: Server host (default: 0.0.0.0)
- `HOMETUNES_PORT`: Server port (default: 8000)
