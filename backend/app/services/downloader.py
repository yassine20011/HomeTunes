import tempfile
import shutil
import base64
import asyncio
from pathlib import Path
from typing import Optional
from concurrent.futures import ThreadPoolExecutor

import yt_dlp

from app.config import Config


class DownloadError(Exception):
    """Custom exception for download failures"""
    pass


class Downloader:
    """Service for downloading and converting YouTube audio"""
    
    def __init__(self):
        self.temp_dir = Config.TEMP_DIR
        self._executor = ThreadPoolExecutor(max_workers=2)
    
    async def download_audio(self, url: str, quality: str = Config.DEFAULT_QUALITY) -> dict:
        """
        Download YouTube audio as M4A, extract metadata.
        
        Args:
            url: YouTube video URL
            quality: Audio quality in kbps (128, 192, 320)
            
        Returns:
            dict with metadata, audio_path, thumbnail_base64, and request_dir
            
        Raises:
            DownloadError: If download or conversion fails
        """
        # Run blocking yt-dlp in thread pool
        loop = asyncio.get_event_loop()
        return await loop.run_in_executor(
            self._executor,
            self._download_sync,
            url,
            quality
        )
    
    def _download_sync(self, url: str, quality: str) -> dict:
        """Synchronous download implementation"""
        # Create unique temp directory for this request
        request_dir = tempfile.mkdtemp(dir=self.temp_dir)
        
        try:
            # Step 1: Download audio with thumbnail
            # Using M4A (AAC) instead of MP3 - this is YouTube's native format
            # so we avoid re-encoding and preserve original quality
            ydl_opts_download = {
                # Prefer AAC audio (m4a) since it's YouTube's native format
                'format': 'bestaudio[ext=m4a]/bestaudio[acodec=aac]/bestaudio/best',
                'postprocessors': [
                    {
                        'key': 'FFmpegExtractAudio',
                        'preferredcodec': 'm4a',  # M4A = AAC audio, YouTube's native format
                        'preferredquality': '0',   # 0 = best quality (copy if possible)
                    },
                    {
                        'key': 'FFmpegMetadata',  # Embed metadata tags
                        'add_metadata': True,
                    },
                ],
                'outtmpl': f'{request_dir}/%(id)s.%(ext)s',
                'writethumbnail': True,
                'quiet': True,
                'no_warnings': True,
                'extract_flat': False,
            }
            
            with yt_dlp.YoutubeDL(ydl_opts_download) as ydl:
                # Extract info and download
                info = ydl.extract_info(url, download=True)
                
                if info is None:
                    raise DownloadError("Failed to extract video information")
                
                # Check duration limit
                duration = info.get("duration", 0)
                if duration > Config.MAX_DURATION:
                    raise DownloadError(
                        f"Video too long ({duration}s). Max: {Config.MAX_DURATION}s"
                    )
            
            # Find the M4A file
            audio_files = list(Path(request_dir).glob("*.m4a"))
            if not audio_files:
                raise DownloadError("M4A file not created")
            audio_path = audio_files[0]
            
            # Step 2: Get thumbnail as base64 BEFORE embedding (for our app)
            thumbnail_base64 = self._get_thumbnail_base64(request_dir)
            
            # Step 3: Embed thumbnail into M4A (for other apps like YouTube Music)
            self._embed_thumbnail_in_audio(request_dir, audio_path)
            
            # Extract metadata
            return {
                "title": self._clean_title(info.get("title", "Unknown")),
                "artist": info.get("artist") or info.get("uploader", "Unknown"),
                "duration": info.get("duration", 0),
                "youtube_id": info.get("id", ""),
                "audio_path": str(audio_path),
                "thumbnail_base64": thumbnail_base64,
                "request_dir": request_dir,
                "file_size": audio_path.stat().st_size,
            }
            
        except yt_dlp.utils.DownloadError as e:
            self.cleanup(request_dir)
            raise DownloadError(f"Download failed: {str(e)}")
        except Exception as e:
            self.cleanup(request_dir)
            raise DownloadError(f"Unexpected error: {str(e)}")
    
    def _get_thumbnail_base64(self, request_dir: str) -> Optional[str]:
        """Read thumbnail file and convert to base64"""
        request_path = Path(request_dir)
        
        # Try common thumbnail extensions
        for ext in ["jpg", "jpeg", "webp", "png"]:
            thumbnails = list(request_path.glob(f"*.{ext}"))
            if thumbnails:
                with open(thumbnails[0], "rb") as f:
                    return base64.b64encode(f.read()).decode("utf-8")
        
        return None
    
    def _embed_thumbnail_in_audio(self, request_dir: str, audio_path: Path) -> None:
        """Embed thumbnail image into M4A file for other music apps"""
        import subprocess
        
        request_path = Path(request_dir)
        
        # Find thumbnail file
        thumbnail_path = None
        for ext in ["jpg", "jpeg", "webp", "png"]:
            thumbnails = list(request_path.glob(f"*.{ext}"))
            if thumbnails:
                thumbnail_path = thumbnails[0]
                break
        
        if not thumbnail_path:
            return  # No thumbnail to embed
        
        # Use ffmpeg to embed the thumbnail
        output_path = audio_path.with_suffix('.tmp.m4a')
        try:
            subprocess.run([
                'ffmpeg', '-y',
                '-i', str(audio_path),
                '-i', str(thumbnail_path),
                '-map', '0:a',
                '-map', '1:0',
                '-c:a', 'copy',
                '-c:v', 'mjpeg',
                '-disposition:v', 'attached_pic',
                str(output_path)
            ], check=True, capture_output=True)
            
            # Replace original with embedded version
            output_path.replace(audio_path)
        except subprocess.CalledProcessError:
            # If embedding fails, just continue without embedded thumbnail
            if output_path.exists():
                output_path.unlink()
        except FileNotFoundError:
            # ffmpeg not available, skip embedding
            pass
    
    def _clean_title(self, title: str) -> str:
        """Clean up video title for use as track title"""
        # Remove common YouTube title patterns
        patterns_to_remove = [
            "(Official Video)",
            "(Official Music Video)",
            "(Official Audio)",
            "(Lyric Video)",
            "(Lyrics)",
            "[Official Video]",
            "[Official Music Video]",
            "[Official Audio]",
            "| Official Video",
            "| Official Music Video",
        ]
        
        cleaned = title
        for pattern in patterns_to_remove:
            cleaned = cleaned.replace(pattern, "")
        
        return cleaned.strip()
    
    def cleanup(self, request_dir: str):
        """Remove temporary files after response sent"""
        try:
            shutil.rmtree(request_dir, ignore_errors=True)
        except Exception:
            pass  # Best effort cleanup


# Singleton instance
downloader = Downloader()
