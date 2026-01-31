import json
from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse

from app.config import Config
from app.schemas import DownloadRequest, DownloadError
from app.services.downloader import downloader, DownloadError as DownloaderError


router = APIRouter(tags=["download"])


@router.post("/download")
async def download_audio(request: DownloadRequest):
    """
    Download YouTube audio and stream back with metadata.
    
    Response format:
    - First line: JSON metadata
    - Rest: Raw M4A binary data
    
    Headers:
    - X-Metadata-Size: Size of metadata JSON (including newline)
    """
    try:
        result = await downloader.download_audio(request.url, request.quality)
    except DownloaderError as e:
        raise HTTPException(
            status_code=422,
            detail={"error": "download_failed", "message": str(e)}
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail={"error": "server_error", "message": str(e)}
        )
    
    # Build metadata JSON
    metadata = {
        "title": result["title"],
        "artist": result["artist"],
        "duration": result["duration"],
        "youtube_id": result["youtube_id"],
        "file_size": result["file_size"],
        "thumbnail_base64": result["thumbnail_base64"],
    }
    metadata_json = json.dumps(metadata, ensure_ascii=False)
    metadata_bytes = metadata_json.encode("utf-8") + b"\n"
    
    async def generate():
        """Stream metadata + M4A file"""
        # Send metadata as JSON line
        yield metadata_bytes
        
        # Stream M4A file in chunks
        try:
            with open(result["audio_path"], "rb") as f:
                while chunk := f.read(Config.CHUNK_SIZE):
                    yield chunk
        finally:
            # Cleanup temp files after streaming completes
            downloader.cleanup(result["request_dir"])
    
    return StreamingResponse(
        generate(),
        media_type="audio/mp4",
        headers={
            "X-Metadata-Size": str(len(metadata_bytes)),
            "Content-Disposition": f'attachment; filename="{result["youtube_id"]}.m4a"',
        }
    )
