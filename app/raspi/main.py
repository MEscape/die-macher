#!/usr/bin/env python3
"""
Main Entry Point for Running Webcam Streaming and OPC UA Servers.

This script uses asyncio to run both TCP/IP webcam streaming and OPC UA servers concurrently.
- The TCP/IP server streams webcam images upon request using the `WebcamServer` class.
- The OPC UA server (currently commented out) is intended to handle temperature and humidity monitoring.

Features:
- Asynchronous execution of both servers using asyncio
- Real-time image capture and streaming over TCP/IP
- Improved exception handling and modularity
- Scalable structure for future integration of OPC UA server

Dependencies:
- Python 3.x
- OpenCV (cv2)
- asyncio
- logging

Modules:
- WebcamServer (from modules.tcp_ip)

To Do:
- Integrate OPC UA server logic
- Improve error handling for async operations
"""
import asyncio
import logging
import sys

from modules.tcp_ip import WebcamServer

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s",
)
logger = logging.getLogger(__name__)


async def run_tcp_server() -> None:
    """Run the TCP/IP camera streaming server.

    This function initializes and starts the WebcamServer in a separate thread
    using asyncio.to_thread to prevent blocking the main event loop.
    """
    server = None
    try:
        # Initialize and run the TCP server
        logger.info("Starting TCP/IP camera streaming server...")
        server = WebcamServer(host="localhost", port=8000)
        await asyncio.to_thread(server.start)
    except ConnectionError as e:
        logger.error(f"Connection error in TCP/IP server: {e}")
    except OSError as e:
        logger.error(f"OS error in TCP/IP server (possibly port already in use): {e}")
    except Exception as e:
        logger.error(f"Unexpected error in TCP/IP server: {e}", exc_info=True)
    finally:
        # Ensure resources are properly released if server was initialized
        if server is not None:
            logger.info("Shutting down TCP/IP server...")
            server.stop()


async def main() -> None:
    """Run all server modules concurrently.

    This function creates and manages tasks for all server modules, handling
    proper shutdown and cleanup when interrupted.
    """
    try:
        # Create tasks for both servers
        tcp_task = asyncio.create_task(run_tcp_server())
        #opc_task = asyncio.create_task(run_opc_server())

        # Wait for both tasks to complete
        # await asyncio.gather(tcp_task, opc_task)
        await asyncio.gather(tcp_task)
    except asyncio.CancelledError:
        logger.info("Main task cancelled, shutting down gracefully...")
    except KeyboardInterrupt:
        logger.info("Keyboard interrupt received, shutting down gracefully...")
    except Exception as e:
        logger.error(f"Unexpected error in main function: {e}", exc_info=True)
    finally:
        logger.info("Application shutdown complete")


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Application terminated by keyboard interrupt")
    except Exception as e:
        logger.error(f"Fatal error: {e}", exc_info=True)
        sys.exit(1)
