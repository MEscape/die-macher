#!/usr/bin/env python3
"""
Main entry point for running both TCP/IP camera streaming and OPC UA servers.

This script uses asyncio to run both servers concurrently. The TCP/IP server
streams webcam images while the OPC UA server handles temperature and humidity
monitoring.
"""
import asyncio
import sys

from modules.tcp_ip.server import serve_webcam_stream


async def run_tcp_server() -> None:
    """Run the TCP/IP camera streaming server."""
    try:
        # Run the TCP server
        await asyncio.to_thread(serve_webcam_stream, host="localhost", port=8000)
    except Exception as e:
        print(f"Error in TCP/IP server: {e}", file=sys.stderr)


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
        print(f"Error: {e}", file=sys.stderr)


if __name__ == "__main__":
    asyncio.run(main())
