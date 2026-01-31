#!/usr/bin/env python3
import uvicorn
from app.config import Config


def main():
    """Run the HomeTunes server"""
    print(f"""
    ╔═══════════════════════════════════════╗
    ║           HomeTunes Server            ║
    ╠═══════════════════════════════════════╣
    ║  Starting on http://{Config.HOMETUNES_HOST}:{Config.HOMETUNES_PORT}      ║
    ║                                       ║
    ║  Endpoints:                           ║
    ║    GET  /health    - Health check     ║
    ║    POST /download  - Download audio   ║
    ╚═══════════════════════════════════════╝
    """)
    
    uvicorn.run(
        "app.main:app",
        host=Config.HOMETUNES_HOST,
        port=Config.HOMETUNES_PORT,
        reload=False,
        log_level="info",
    )


if __name__ == "__main__":
    main()
