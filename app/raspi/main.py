#!/usr/bin/env python3
"""
Main entry point for running both TCP/IP camera streaming and OPC UA servers.

This script uses asyncio to run both servers concurrently. The TCP/IP server
streams webcam images while the OPC UA server handles temperature and humidity
monitoring.
"""
import asyncio
import sys
from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path


def import_module_from_file(module_name: str, file_path: str):
    """Import a module from file path."""
    spec = spec_from_file_location(module_name, file_path)
    if spec is None or spec.loader is None:
        raise ImportError(
            f"Could not load spec for module {module_name} from {file_path}"
        )

    module = module_from_spec(spec)
    sys.modules[module_name] = module
    spec.loader.exec_module(module)
    return module


async def run_tcp_server():
    """Run the TCP/IP camera streaming server."""
    # Import the TCP/IP server module
    tcp_module_path = Path(__file__).parent / "modules" / "tcp_ip" / "main.py"
    tcp_module = import_module_from_file("tcp_server", str(tcp_module_path))

    # Create a new event loop for the TCP server
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)

    # Run the TCP server
    try:
        tcp_module.main()
    except Exception as e:
        print(f"Error in TCP/IP server: {e}", file=sys.stderr)


async def run_opc_server():
    """Run the OPC UA server."""
    # Import the OPC UA server module
    opc_module_path = Path(__file__).parent / "modules" / "opc_ua" / "main.py"
    try:
        opc_module = import_module_from_file("opc_server", str(opc_module_path))
        # Run the OPC UA server
        await opc_module.main()
    except FileNotFoundError:
        print("Warning: OPC UA server module not found. Skipping.", file=sys.stderr)
    except Exception as e:
        print(f"Error in OPC UA server: {e}", file=sys.stderr)


async def main():
    """Run both servers concurrently."""
    try:
        # Create tasks for both servers
        tcp_task = asyncio.create_task(run_tcp_server())
        opc_task = asyncio.create_task(run_opc_server())

        # Wait for both tasks to complete
        await asyncio.gather(tcp_task, opc_task)
    except KeyboardInterrupt:
        print("\nShutting down servers...")
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        print("\nServers shutdown complete.")
