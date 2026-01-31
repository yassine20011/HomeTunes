from pydantic import BaseModel, field_validator
from typing import Optional
import re
from app.config import Config

class DownloadRequest(BaseModel):
    """Request schema for /download endpoint"""
    url: str
    quality: str = Config.DEFAULT_QUALITY
    
    @field_validator("url")
    @classmethod
    def validate_youtube_url(cls, v: str) -> str:
        """Validate that URL is a YouTube URL"""
        youtube_patterns = [
            r"(https?://)?(www\.)?youtube\.com/watch\?v=[\w-]+",
            r"(https?://)?(www\.)?youtu\.be/[\w-]+",
            r"(https?://)?(www\.)?youtube\.com/shorts/[\w-]+",
            r"(https?://)?music\.youtube\.com/watch\?v=[\w-]+",
        ]
        
        for pattern in youtube_patterns:
            if re.match(pattern, v):
                return v
        
        raise ValueError("Invalid YouTube URL")
    
    @field_validator("quality")
    @classmethod
    def validate_quality(cls, v: str) -> str:
        """Validate audio quality setting"""
        if v not in Config.ALLOWED_QUALITIES:
            raise ValueError(f"Quality must be one of: {Config.ALLOWED_QUALITIES}")
        return v


class TrackMetadata(BaseModel):
    """Metadata for a downloaded track"""
    title: str
    artist: str
    duration: int  # seconds
    youtube_id: str
    thumbnail_base64: Optional[str] = None


class DownloadError(BaseModel):
    """Error response schema"""
    error: str
    message: str


class HealthResponse(BaseModel):
    """Health check response"""
    status: str = "ok"
    version: str = "1.0.0"
