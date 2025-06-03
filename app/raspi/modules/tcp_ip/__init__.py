#!/usr/bin/env python3
"""
TCP/IP module for Die-Macher application.

This module provides classes for TCP/IP communication, webcam streaming,
and command handling with robust error handling and proper separation of concerns.
"""

from .camera_streamer import CameraStreamer
from .command_handler import CommandHandler
from .connection import (
    Connection,
    ConnectionClosedError,
    ConnectionReadError,
    ConnectionWriteError,
    TCPConnectionError,
)
from .webcam_server import WebcamServer, WebcamServerError

__all__ = [
    "Connection",
    "TCPConnectionError",
    "ConnectionClosedError",
    "ConnectionReadError",
    "ConnectionWriteError",
    "CommandHandler",
    "WebcamServer",
    "WebcamServerError",
    "CameraStreamer",
]
