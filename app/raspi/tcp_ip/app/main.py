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
import time
import cv2
import argparse
import sys

def parse_args():
    """Parse command-line arguments for server configuration."""
    parser = argparse.ArgumentParser(description="TCP/IP webcam stream server")
    parser.add_argument("--host", type=str, default="localhost",
                        help="Host/IP to bind the server (default all interfaces)")
    parser.add_argument("--port", type=int, default=8000,
                        help="TCP port to listen on (default 8000)")
    parser.add_argument("--fps", type=float, default=0,
                        help="Target frames per second (0 for max speed)")
    return parser.parse_args()

def setup_server_socket(host: str, port: int) -> socket.socket:
    """
    Create, configure, bind, and listen on a TCP socket.
    Uses SO_REUSEADDR to allow quick restarts&#8203;:contentReference[oaicite:7]{index=7}.
    """
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Allow immediate reuse of the address after the program exits
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind((host, port))
    server_socket.listen(1)
    print(f"Listening on {host}:{port} (waiting for a connection)...")
    return server_socket

def stream_camera(conn: socket.socket, frame_interval: float):
    """
    Capture frames from the default camera and send them over the socket.
    Each frame is JPEG-encoded and sent with a 4-byte length prefix.
    """
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Error: Could not open video capture device.", file=sys.stderr)
        return

    # OPTIONAL: Set camera resolution (smaller -> faster streaming)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 320)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 240)

    try:
        while True:
            ret, frame = cap.read()
            if not ret:
                print("Warning: Failed to read frame from camera.", file=sys.stderr)
                break

            # Encode frame as JPEG
            result, jpeg = cv2.imencode('.jpg', frame)
            if not result:
                print("Warning: JPEG encoding failed.", file=sys.stderr)
                break
            data = jpeg.tobytes()
            size = len(data)
            # Send the length of the JPEG data first (4 bytes, big-endian)
            header = struct.pack(">L", size)
            try:
                conn.sendall(header + data)  # ensures all bytes are sent&#8203;:contentReference[oaicite:9]{index=9}&#8203;:contentReference[oaicite:10]{index=10}
            except (BrokenPipeError, ConnectionResetError):
                print("Client disconnected.")
                break

            # Throttle frame rate if requested
            if frame_interval > 0:
                time.sleep(frame_interval)
    finally:
        cap.release()

def main():
    args = parse_args()
    # Calculate delay between frames from FPS
    frame_interval = 0.0
    if args.fps and args.fps > 0:
        frame_interval = 1.0 / args.fps

    # Setup server socket (context manager will auto-close)&#8203;:contentReference[oaicite:11]{index=11}.
    try:
        with setup_server_socket(args.host, args.port) as server_socket:
            # Accept a single client connection
            conn, addr = server_socket.accept()
            print(f"Connected by {addr}")
            with conn:
                # Stream video to the connected client
                stream_camera(conn, frame_interval)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)

if __name__ == "__main__":
    main()
