#!/usr/bin/env python3
"""
Command handler for TCP/IP communications.

This module provides a thread-based command handler that processes incoming
commands from a TCP/IP connection with proper exception handling and separation
of concerns. It follows best practices for error propagation and provides a clean
interface for command processing.

Features:
- Clear separation between connection management and command processing
- Robust exception handling with proper error propagation
- Comprehensive logging for debugging and monitoring
- Type hints for better code maintainability

Dependencies:
- threading: For running the command handler as a separate thread
- queue: For placing commands into a queue for further handling
- logging: For structured logging of events and errors
- connection: For TCP/IP connection management
"""

import logging
import threading
from queue import Queue
from typing import Any, Callable, Optional

from .connection import Connection, ConnectionClosedError, TCPConnectionError

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s",
)
logger = logging.getLogger(__name__)


class CommandHandlerError(Exception):
    """Base exception for all command handler related errors."""


class CommandHandler(threading.Thread):
    """Handles incoming commands from the client with improved error handling."""

    def __init__(
        self,
        connection: Connection,
        command_queue: Queue[str],
        on_connection_closed: Optional[Callable[[], Any]] = None,
    ):
        """
        Initialize the command handler.

        Args:
            connection: The Connection object for communication
            command_queue: Queue where received commands will be placed
            on_connection_closed: Optional callback when connection closes
        """
        super().__init__(daemon=True)
        self.connection = connection
        self.command_queue = command_queue
        self.on_connection_closed = on_connection_closed
        self._running = True

    def stop(self) -> None:
        """Signal the command handler to stop."""
        self._running = False

    def run(self) -> None:
        """Listens for incoming commands and places them in a queue."""
        try:
            self._process_commands()
        except ConnectionClosedError as e:
            logger.warning("Connection closed: %s", e)
        except TCPConnectionError as e:
            logger.error("Connection error: %s", e)
        except Exception as e:
            logger.error(
                "Unexpected error in command handler: %s: %s", type(e).__name__, e
            )
        finally:
            # Clean up resources
            self.connection.close()
            # Notify about connection closure if callback is provided
            if self.on_connection_closed:
                try:
                    self.on_connection_closed()
                except Exception as e:
                    logger.error(
                        "Error in connection closed callback: %s: %s",
                        type(e).__name__,
                        e,
                    )

    def _process_commands(self) -> None:
        """Main command processing loop with proper error handling."""
        while self._running:
            try:
                # Receive the complete message
                data = self.connection.recv_message()

                # Decode and process the message
                try:
                    command = data.decode("utf-8").strip()
                    logger.info("Received command: %s", command)
                except UnicodeDecodeError as e:
                    logger.error("Failed to decode message: %s", e)
                    continue

                # Put the command in the queue
                self.command_queue.put(command)
            except ConnectionClosedError:
                logger.info("Connection closed by peer, stopping command handler")
                break
            except TCPConnectionError as e:
                logger.error("Connection error while processing commands: %s", e)
                break
            except Exception as e:
                logger.error(
                    "Unexpected error while processing commands: %s: %s",
                    type(e).__name__,
                    e,
                )
                break
