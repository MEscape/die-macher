#!/usr/bin/env python3
"""
Main Entry Point for Running Webcam Streaming and OPC UA Servers.

This script uses asyncio to run both TCP/IP webcam streaming and OPC UA servers concurrently.
- The TCP/IP server streams webcam images upon request using the `WebcamServer` class.
- The OPC UA server (currently commented out) is intended to handle temperature and humidity monitoring.

Features:
- Asynchronous execution of both servers using asyncio
- Real-time image capture and streaming over TCP/IP
- Scalable structure for future integration of OPC UA server

Dependencies:
- Python 3.x
- OpenCV (cv2)
- asyncio
- logging

Modules:
- WebcamServer (from modules.tcp_ip.server)

To Do:
- Integrate OPC UA server logic
- Improve error handling for async operations
"""
import asyncio
import sys
import logging

from modules.tcp_ip.WebcamServer import WebcamServer

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s'
)
logger = logging.getLogger(__name__)

async def run_tcp_server() -> None:
    """Run the TCP/IP camera streaming server."""
    try:
        # Run the TCP server
        server = WebcamServer(host="localhost", port=8000)
        await asyncio.to_thread(server.run)
    except Exception as e:
        logger.error(f"Error in TCP/IP server: {e}")


async def main() -> None:
    """Run all server modules concurrently."""
    try:
        # Create tasks for both servers
        tcp_task = asyncio.create_task(run_tcp_server())
        # opc_task = asyncio.create_task(run_opc_server())

        # Wait for both tasks to complete
        # await asyncio.gather(tcp_task, opc_task)
        await asyncio.gather(tcp_task)
    except Exception as e:
        logger.error(f"Error: {e}", file=sys.stderr)


if __name__ == "__main__":
    asyncio.run(main())
