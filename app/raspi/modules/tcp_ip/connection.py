#!/usr/bin/env python3
"""
Connection management module for TCP/IP communications.

This module provides classes for managing TCP/IP connections with proper exception
handling and separation of concerns. It follows best practices for error propagation
and provides a clean interface for socket communication.

Features:
- Clear separation between connection management and command processing
- Robust exception handling with proper error propagation
- Comprehensive logging for debugging and monitoring
- Type hints for better code maintainability

Dependencies:
- socket: For TCP/IP communication
- struct: For binary data packing/unpacking
- logging: For structured logging of events and errors
"""

import logging
import socket
import struct
from typing import Any, Tuple

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s",
)
logger = logging.getLogger(__name__)


class TCPConnectionError(Exception):
    """Base exception for all connection-related errors."""


class ConnectionClosedError(TCPConnectionError):
    """Exception raised when the connection is closed by the peer."""


class ConnectionReadError(TCPConnectionError):
    """Exception raised when there's an error reading from the connection."""


class ConnectionWriteError(TCPConnectionError):
    """Exception raised when there's an error writing to the connection."""


class Connection:
    """Manages a TCP/IP socket connection with robust error handling."""

    HEADER_SIZE = 4  # 4 bytes for message length header

    def __init__(self, sock: socket.socket):
        """Initialize with an existing socket connection."""
        self.sock = sock

    @classmethod
    def create_server(cls, host: str, port: int) -> socket.socket:
        """Creates and configures a server socket."""
        try:
            server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            server_socket.bind((host, port))
            server_socket.listen(1)
            logger.info("Server socket listening on %s:%s", host, port)
            return server_socket
        except OSError as e:
            logger.error("Failed to create server socket: %s: %s", type(e).__name__, e)
            raise TCPConnectionError(f"Failed to create server socket: {e}") from e

    @classmethod
    def accept_connection(
        cls, server_socket: socket.socket
    ) -> Tuple["Connection", Any]:
        """Accepts a client connection from a server socket."""
        try:
            client_socket, addr = server_socket.accept()
            logger.info("Client connected from %s", addr)
            return cls(client_socket), addr
        except OSError as e:
            logger.error("Failed to accept connection: %s: %s", type(e).__name__, e)
            raise TCPConnectionError(f"Failed to accept connection: {e}") from e

    def close(self) -> None:
        """Closes the connection safely."""
        try:
            self.sock.close()
            logger.info("Connection closed")
        except Exception as e:
            logger.warning(
                "Error while closing connection: %s: %s", type(e).__name__, e
            )

    def recv_exactly(self, length: int) -> bytes:
        """Receives exactly 'length' bytes from the socket with proper error handling."""
        data = b""
        while len(data) < length:
            try:
                chunk = self.sock.recv(length - len(data))
                if not chunk:  # Connection closed by peer
                    logger.warning("Connection closed by peer during receive")
                    raise ConnectionClosedError("Connection closed by peer")
                data += chunk
            except (ConnectionResetError, BrokenPipeError) as e:
                logger.error("Connection reset during receive: %s", e)
                raise ConnectionClosedError(f"Connection reset: {e}") from e
            except socket.timeout as e:
                logger.error("Socket timeout during receive: %s", e)
                raise ConnectionReadError(f"Socket timeout: {e}") from e
            except OSError as e:
                logger.error("Socket error during receive: %s: %s", type(e).__name__, e)
                raise ConnectionReadError(f"Socket error: {e}") from e
        return data

    def send_all(self, data: bytes) -> None:
        """Sends all data to the socket with proper error handling."""
        try:
            self.sock.sendall(data)
        except (ConnectionResetError, BrokenPipeError) as e:
            logger.error("Connection reset during send: %s", e)
            raise ConnectionClosedError(f"Connection reset: {e}") from e
        except socket.timeout as e:
            logger.error("Socket timeout during send: %s", e)
            raise ConnectionWriteError(f"Socket timeout: {e}") from e
        except OSError as e:
            logger.error("Socket error during send: %s: %s", type(e).__name__, e)
            raise ConnectionWriteError(f"Socket error: {e}") from e

    def recv_message(self) -> bytes:
        """Receives a complete message with length header."""
        # Read the header (4 bytes for length)
        header = self.recv_exactly(self.HEADER_SIZE)

        # Unpack the header to get the message length
        message_length = struct.unpack(">I", header)[0]
        logger.debug("Received header, message length: %s bytes", message_length)

        # Read the exact message length
        return self.recv_exactly(message_length)

    def send_message(self, data: bytes) -> None:
        """Sends a complete message with length header."""
        size = len(data)
        header = struct.pack(">I", size)
        try:
            self.send_all(header + data)
            logger.debug("Sent message of %s bytes", size)
        except Exception as e:
            logger.error("Failed to send message: %s: %s", type(e).__name__, e)
            raise
