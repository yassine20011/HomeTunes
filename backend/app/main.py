from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import Config
from app.routers import download
from app.schemas import HealthResponse


# Create FastAPI app
app = FastAPI(
    title="HomeTunes",
    description="Personal music server - YouTube to M4A audio downloader",
    version="1.0.0",
)

# CORS middleware for mobile app access
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for local network
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(download.router)


@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint for mobile app connection test"""
    return HealthResponse()


@app.get("/")
async def root():
    """Root endpoint with API info"""
    return {
        "name": "HomeTunes",
        "version": "1.0.0",
        "endpoints": {
            "health": "GET /health",
            "download": "POST /download",
        }
    }
