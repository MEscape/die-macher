#!/usr/bin/env python3
"""
A server that streams webcam images to a client on demand.

The server listens for client connections and sends real-time webcam frames 
encoded as JPEG images upon request. It uses a separate thread for handling 
client commands and provides a main loop to accept connections, process 
commands, and stream images.

Features:
- Listens for incoming client connections.
- Captures webcam frames and encodes them as JPEG.
- Streams the JPEG images to the connected client upon request.
- Handles multiple commands sent by the client using a command handler.
- Comprehensive logging for debugging and monitoring.

Dependencies:
- socket: For handling TCP/IP communication with the client.
- struct: For packing and unpacking message headers and image sizes.
- queue: For managing commands received from the client.
- CameraStreamer: For capturing and encoding webcam frames.
- CommandHandler: For processing client commands.
- logging: For structured logging of events and errors.

Attributes:
- host: The host address where the server will listen for connections.
- port: The port on which the server will listen for connections.
- command_queue: A queue that stores incoming commands from the client.
- camera_streamer: An instance of the CameraStreamer class for capturing webcam images.
- server_socket: The socket server that listens for incoming client connections.
"""
import socket
import struct
from queue import Queue, Empty
import logging
from .CameraStreamer import CameraStreamer
from .CommandHandler import CommandHandler

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s'
)
logger = logging.getLogger(__name__)

class WebcamServer:
    """Main server that streams webcam images on demand."""
    
    def __init__(self, host: str, port: int):
        self.host = host
        self.port = port
        self.command_queue = Queue()
        self.camera_streamer = CameraStreamer()
        self.server_socket = self._setup_server_socket()

    def _setup_server_socket(self) -> socket.socket:
        """Sets up the server socket."""
        try:
            server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            server_socket.bind((self.host, self.port))
            server_socket.listen(1)
            logger.info(f"Server socket listening on {self.host}:{self.port}")
            return server_socket
        except OSError as e:
            logger.error(f"Failed to set up server socket: {e}")
            raise
    
    def _send_image(self, conn: socket.socket) -> None:
        """Captures an image from the camera and sends it to the client."""
        try:
            data = self.camera_streamer.capture_frame()
            size = len(data)
            header = struct.pack(">L", size)
            conn.sendall(header + data)
            logger.info(f"Image of {size} bytes sent to client")
        except Exception as e:
            logger.error(f"Failed to send image: {type(e).__name__}: {e}")
    
    def run(self) -> None:
        """Main loop to accept connections and handle requests."""
        while True:
            conn, addr = self.server_socket.accept()
            logger.info(f"Client connected from {addr}")
            
            command_handler = CommandHandler(conn, self.command_queue)
            command_handler.start()
            
            while True:
                try:
                    command = self.command_queue.get()
                    if command == "SEND_IMAGE":
                        self._send_image(conn)
                except Empty:
                    continue