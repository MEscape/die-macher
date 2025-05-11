#!/usr/bin/env python3
"""
TCP/IP Socket Server for streaming webcam images (JPEG) to one client.

This server captures frames from a Logitech C270 (or similar) webcam using OpenCV,
encodes each frame as JPEG, and sends it over a TCP connection. It follows
best practices for socket programming (context managers, SO_REUSEADDR, graceful
shutdown) and for camera handling (release on exit). The frame rate can be
adjusted via a command-line parameter.

Requires: Python 3, OpenCV (cv2).
"""
import socket
import struct
import sys
import time
from typing import Any, Tuple

import cv2
import numpy as np
from numpy.typing import NDArray


def setup_server_socket(host: str, port: int) -> socket.socket:
    """
    Create, configure, bind, and listen on a TCP socket.
    Uses SO_REUSEADDR to allow quick restarts.
    """
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind((host, port))
    server_socket.listen(1)
    print(f"Listening on {host}:{port} (waiting for a connection)...")
    return server_socket


def stream_camera(conn: socket.socket) -> None:
    """
    Capture frames from the default camera and send them over the socket.
    Each frame is JPEG-encoded and sent with a 4-byte length prefix.
    """
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Error: Could not open video capture device.", file=sys.stderr)
        return

    try:
        while True:
            ret: bool
            frame: NDArray[Any]
            ret, frame = cap.read()
            if not ret:
                print("Warning: Failed to read frame from camera.", file=sys.stderr)
                break

            result: bool
            jpeg: NDArray[np.uint8]
            result, jpeg = cv2.imencode(".jpg", frame)
            if not result:
                print("Warning: JPEG encoding failed.", file=sys.stderr)
                break

            data: bytes = jpeg.tobytes()
            size: int = len(data)
            header: bytes = struct.pack(">L", size)
            try:
                conn.sendall(header + data)
            except (BrokenPipeError, ConnectionResetError):
                print("Client disconnected.")
                break

            # Wait 10 seconds before capturing the next frame
            time.sleep(10)
    finally:
        cap.release()


def serve_webcam_stream(host: str = "localhost", port: int = 8000) -> None:
    """Run the TCP/IP webcam image server with specified configuration.

    Sets up the server socket, and starts sending camera frames
    to the connected client at 10-second intervals. Handles connection
    setup and cleanup.
    """

    try:
        with setup_server_socket(host, port) as server_socket:
            conn: socket.socket
            addr: Tuple[str, int]
            conn, addr = server_socket.accept()
            print(f"Connected by {addr}")
            with conn:
                stream_camera(conn)
    except (socket.error, IOError) as e:
        print(f"Network or I/O error: {e}", file=sys.stderr)
    except Exception as e:
        print(f"Unexpected error: {e}", file=sys.stderr)
