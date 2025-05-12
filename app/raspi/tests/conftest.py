#!/usr/bin/env python3
"""
Pytest-Konfiguration und gemeinsame Fixtures für Tests.

Diese Datei enthält gemeinsame Fixtures und Konfigurationen,
die von mehreren Testmodulen verwendet werden können.
"""

from unittest.mock import MagicMock, patch

import pytest


@pytest.fixture
def mock_camera():
    """Fixture für ein Mock-Kameraobjekt."""
    with patch("cv2.VideoCapture") as mock_video_capture:
        # Konfiguriere das Mock-Objekt für erfolgreiche Initialisierung
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        mock_instance.read.return_value = (True, MagicMock())
        yield mock_video_capture


@pytest.fixture
def mock_socket():
    """Fixture für ein Mock-Socket-Objekt."""
    with patch("socket.socket") as mock_socket:
        mock_instance = mock_socket.return_value
        yield mock_socket


@pytest.fixture
def mock_connection():
    """Fixture für ein Mock-Connection-Objekt."""
    mock_conn = MagicMock()
    mock_conn.recv_message.return_value = b"test_command"
    mock_conn.send_message.return_value = None
    yield mock_conn


@pytest.fixture
def mock_command_queue():
    """Fixture für eine Mock-Befehlswarteschlange."""
    from queue import Queue

    return Queue()
