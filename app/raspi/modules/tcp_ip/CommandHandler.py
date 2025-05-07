#!/usr/bin/env python3
"""
Listens for incoming commands from the client and processes them.

This class handles socket communication with the client, listening for 
incoming commands and placing them in a queue for further processing. 
It reads the length of incoming messages, receives the messages exactly 
as specified, and decodes them for further use.

Features:
- Reads incoming commands over a socket connection.
- Handles socket communication with error handling for connection issues.
- Puts the processed commands in a queue for further processing.
- Comprehensive logging for debugging and monitoring.

Dependencies:
- socket: For communication with the client.
- struct: For unpacking message length from the header.
- threading: For running the command handler as a separate thread.
- queue: For placing commands into a queue for further handling.
- logging: For structured logging of events and errors.

Attributes:
- HEADER_SIZE: Fixed size of the header used to specify message length (4 bytes).
- conn: The socket connection to the client.
- command_queue: A queue where the processed commands are placed for handling.
"""
import socket
import struct
import threading
import logging
from queue import Queue
from typing import Optional

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s'
)
logger = logging.getLogger(__name__)

class CommandHandler(threading.Thread):
    """Handles incoming commands from the client."""
    HEADER_SIZE = 4
    
    def __init__(self, conn: socket.socket, command_queue: Queue):
        super().__init__(daemon=True)
        self.conn = conn
        self.command_queue = command_queue

    def _recv_exactly(self, length: int) -> Optional[bytes]:
        """Receives exactly 'length' bytes from the socket."""
        data = b""
        while len(data) < length:
            try:
                chunk = self.conn.recv(length - len(data))
                if not chunk:
                    return None
                data += chunk
            except (ConnectionResetError, BrokenPipeError, socket.timeout, OSError):
                return None 
        return data

    def run(self) -> None:
        """Listens for incoming commands and places them in a queue."""
        try:
            while True:
                # 1. Read the header (4 bytes for length)
                header = self._recv_exactly(self.HEADER_SIZE)
                if not header:
                    logger.warning("Failed to receive header")
                    continue

                # 2. Unpack the header to get the message length
                message_length = struct.unpack(">I", header)[0]
                logger.debug(f"Received header, message length: {message_length} bytes")

                # 3. Read the exact message length
                data = self._recv_exactly(message_length)
                if not data:
                    logger.warning("Failed to receive complete message")
                    continue

                # 4. Decode and process the message
                try:
                    command = data.decode('utf-8').strip()
                    logger.info(f"Received command: {command}")
                except UnicodeDecodeError as e:
                    logger.error(f"Failed to decode message: {e}")
                    continue

                # 5. Put the command in the queue
                self.command_queue.put(command)
        except (ConnectionResetError, BrokenPipeError) as e:
            logger.warning(f"Connection closed by client: {e}")
        finally:
            self.conn.close()