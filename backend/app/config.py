import os
from pathlib import Path


class Config:
    """Application configuration"""
    
    # Server settings
    HOMETUNES_HOST: str = os.getenv("HOMETUNES_HOST", "0.0.0.0")
    HOMETUNES_PORT: int = int(os.getenv("HOMETUNES_PORT", "8000"))
    
    # Paths
    BASE_DIR: Path = Path(__file__).parent.parent
    TEMP_DIR: Path = BASE_DIR / "temp"
    
    # Download settings
    DEFAULT_QUALITY: str = "192"  # kbps
    ALLOWED_QUALITIES: list = ["128", "192", "320"]
    
    # yt-dlp settings
    MAX_DURATION: int = 3600  # 1 hour max video length in seconds

    # Chunk size for streaming
    CHUNK_SIZE: int = 8192
    
    @classmethod
    def init(cls):
        """Initialize directories"""
        cls.TEMP_DIR.mkdir(exist_ok=True)


# Initialize on import
Config.init()
