#!/usr/bin/env python3
"""
Webcam server for TCP/IP communications.

This module provides a server that streams webcam images to clients with proper
exception handling and separation of concerns. It follows best practices for error
propagation and provides a clean interface for webcam streaming.

Features:
- Clear separation between connection management, command processing, and image streaming
- Robust exception handling with proper error propagation
- Comprehensive logging for debugging and monitoring
- Type hints for better code maintainability

Dependencies:
- socket: For TCP/IP communication
- queue: For managing commands received from the client
- logging: For structured logging of events and errors
- connection: For TCP/IP connection management
- command_handler: For processing client commands
- camera_streamer: For capturing and encoding webcam frames
"""

import logging
import socket
from queue import Empty, Queue
from typing import Optional

from .camera_streamer import CameraStreamer
from .command_handler import CommandHandler
from .connection import Connection, ConnectionWriteError, TCPConnectionError

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s",
)
logger = logging.getLogger(__name__)


class WebcamServerError(Exception):
    """Base exception for all webcam server related errors."""


class WebcamServer:
    """Server that streams webcam images on demand."""

    def __init__(self, host: str, port: int):
        """
        Initialize the webcam server.

        Args:
            host: The host address to bind the server to
            port: The port to bind the server to
        """
        self.host = host
        self.port = port
        self.command_queue: Queue[str] = Queue()
        self.camera_streamer: Optional[CameraStreamer] = CameraStreamer(0)
        self.server_socket: Optional[socket.socket] = None
        self.client_connection: Optional[Connection] = None
        self.command_handler: Optional[CommandHandler] = None
        self.is_running = False

    def start(self) -> None:
        """Start the webcam server."""
        try:
            self.server_socket = Connection.create_server(self.host, self.port)
            self.is_running = True
            self._main_loop()
        except Exception as e:
            logger.error("Failed to start webcam server: %s: %s", type(e).__name__, e)
            raise WebcamServerError(f"Failed to start webcam server: {e}") from e
        finally:
            self._cleanup()

    def stop(self) -> None:
        """Stop the webcam server."""
        self.is_running = False
        if self.command_handler:
            self.command_handler.stop()
        self._cleanup()

    def _cleanup(self) -> None:
        """Clean up resources."""
        # Clean up client connection
        if self.client_connection:
            self.client_connection.close()
            self.client_connection = None

        # Clean up server socket
        if self.server_socket:
            try:
                self.server_socket.close()
            except Exception as e:
                logger.warning(
                    "Error closing server socket: %s: %s", type(e).__name__, e
                )
            self.server_socket = None

        # Release camera resources
        if self.camera_streamer:
            try:
                self.camera_streamer.release()
                logger.info("Camera resources released")
            except Exception as e:
                logger.warning(
                    "Error releasing camera resources: %s: %s", type(e).__name__, e
                )
            self.camera_streamer = None

    def _handle_client_disconnection(self) -> None:
        """Handle client disconnection."""
        if self.client_connection:
            logger.info("Client disconnected, cleaning up resources")
            self.client_connection.close()
            self.client_connection = None
        if self.command_handler:
            self.command_handler = None

    def _send_image(self) -> None:
        """Capture and send an image to the client."""
        if not self.client_connection:
            logger.warning("Cannot send image: No client connection")
            return

        try:
            if not self.camera_streamer:
                logger.warning("Cannot send image: Camera streamer not initialized")
                return

            # Capture frame from camera
            data = self.camera_streamer.capture_frame()

            # Send the image data with length header
            self.client_connection.send_message(data)
            logger.info("Image of %s bytes sent to client", len(data))
        except ConnectionWriteError as e:
            logger.error("Failed to send image: %s", e)
            self._handle_client_disconnection()
        except Exception as e:
            logger.error(
                "Error capturing or sending image: %s: %s", type(e).__name__, e
            )

    def _main_loop(self) -> None:
        """Main server loop with proper error handling."""
        while self.is_running:
            try:
                # Wait for a client connection if we don't have one
                if not self.client_connection and self.server_socket:
                    logger.info("Waiting for client connection...")
                    self.client_connection, _ = Connection.accept_connection(
                        self.server_socket
                    )

                    # Start command handler for this connection
                    self.command_handler = CommandHandler(
                        self.client_connection,
                        self.command_queue,
                        on_connection_closed=self._handle_client_disconnection,
                    )
                    self.command_handler.start()

                # Process commands from the queue
                try:
                    command = self.command_queue.get(block=True, timeout=0.5)
                    if command == "SEND_IMAGE":
                        self._send_image()
                    else:
                        logger.warning("Unknown command received: %s", command)
                except Empty:
                    # No commands in the queue, continue waiting
                    continue
            except TCPConnectionError as e:
                logger.error("Connection error in main loop: %s", e)
                self._handle_client_disconnection()
            except Exception as e:
                logger.error(
                    "Unexpected error in main loop: %s: %s", type(e).__name__, e
                )
                # Continue running the server despite errors with a specific client
                self._handle_client_disconnection()
