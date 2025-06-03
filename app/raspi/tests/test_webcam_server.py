#!/usr/bin/env python3
"""
Tests für das WebcamServer-Modul.

Diese Tests überprüfen die Funktionalität des WebcamServer-Moduls,
einschließlich Serverinitialisierung, Verbindungsannahme, Befehlsverarbeitung und Fehlerbehandlung.
"""

import unittest
from unittest.mock import AsyncMock, MagicMock, patch

from modules.tcp_ip.connection import ConnectionWriteError, TCPConnectionError
from modules.tcp_ip.webcam_server import WebcamServer, WebcamServerError


class TestWebcamServer(unittest.TestCase):
    """Testklasse für das WebcamServer-Modul."""

    def setUp(self):
        """Testumgebung einrichten."""
        # Patches für externe Abhängigkeiten
        self.connection_patcher = patch("modules.tcp_ip.webcam_server.Connection")
        self.camera_streamer_patcher = patch(
            "modules.tcp_ip.webcam_server.CameraStreamer"
        )
        self.command_handler_patcher = patch(
            "modules.tcp_ip.webcam_server.CommandHandler"
        )

        # Mock-Objekte
        self.mock_connection = self.connection_patcher.start()
        self.mock_camera_streamer = self.camera_streamer_patcher.start()
        self.mock_command_handler = self.command_handler_patcher.start()

        # Server-Instanz
        self.server = WebcamServer(host="localhost", port=8000)

    def tearDown(self):
        """Testumgebung aufräumen."""
        self.connection_patcher.stop()
        self.camera_streamer_patcher.stop()
        self.command_handler_patcher.stop()

    def test_init(self):
        """Test Initialisierung des WebcamServers."""
        self.assertEqual(self.server.host, "localhost")
        self.assertEqual(self.server.port, 8000)
        self.assertIsNotNone(self.server.command_queue)
        self.assertIsNotNone(self.server.camera_streamer)
        self.assertIsNone(self.server.server_socket)
        self.assertIsNone(self.server.client_connection)
        self.assertIsNone(self.server.command_handler)
        self.assertFalse(self.server.is_running)

    def test_start_success(self):
        """Test erfolgreicher Start des WebcamServers."""
        # Mock-Konfiguration
        mock_server_socket = MagicMock()
        self.mock_connection.create_server.return_value = mock_server_socket

        # Patch für _main_loop, um die Ausführung zu simulieren
        with patch.object(self.server, "_main_loop") as mock_main_loop:
            # Test
            self.server.start()

            # Überprüfungen
            self.mock_connection.create_server.assert_called_once_with(
                "localhost", 8000
            )
            self.assertTrue(self.server.is_running)
            mock_main_loop.assert_called_once()

    def test_start_failure(self):
        """Test fehlgeschlagener Start des WebcamServers."""
        # Mock-Konfiguration
        self.mock_connection.create_server.side_effect = TCPConnectionError(
            "Verbindungsfehler"
        )

        # Überprüfungen
        with self.assertRaises(WebcamServerError):
            self.server.start()

    def test_stop(self):
        """Test Stoppen des WebcamServers."""
        # Mock-Konfiguration
        self.server.is_running = True
        self.server.command_handler = MagicMock()

        # Patch für _cleanup, um die Ausführung zu simulieren
        with patch.object(self.server, "_cleanup") as mock_cleanup:
            # Test
            self.server.stop()

            # Überprüfungen
            self.assertFalse(self.server.is_running)
            self.server.command_handler.stop.assert_called_once()
            mock_cleanup.assert_called_once()

    def test_send_image_no_camera(self):
        """Test Bildversand ohne Kamera."""
        self.server.client_connection = MagicMock()
        self.server.camera_streamer = None

        self.server._send_image()

        self.server.client_connection.send_message.assert_not_called()

    def test_cleanup_camera_error(self):
        """Test Aufräumen mit Fehler bei der Kamera-Freigabe."""
        mock_camera = MagicMock()
        mock_camera.release.side_effect = Exception("Camera release error")
        self.server.camera_streamer = mock_camera

        with patch("modules.tcp_ip.webcam_server.logger"):
            self.server._cleanup()

        self.assertIsNone(self.server.camera_streamer)

    def test_main_loop(self):
        """Test Hauptschleife des WebcamServers."""
        # Mock-Konfiguration
        self.server.server_socket = MagicMock()
        mock_client_connection = MagicMock()
        mock_addr = ("127.0.0.1", 12345)

        self.mock_connection.accept_connection.return_value = (
            mock_client_connection,
            mock_addr,
        )
        self.mock_command_handler.return_value = MagicMock()

        # Setze is_running auf False nach einem Durchlauf
        def side_effect(*args, **kwargs):
            self.server.is_running = False
            return (mock_client_connection, mock_addr)

        self.mock_connection.accept_connection.side_effect = side_effect

        # Test
        self.server.is_running = True
        self.server._main_loop()

        # Überprüfungen
        self.mock_connection.accept_connection.assert_called_once_with(
            self.server.server_socket
        )
        self.assertEqual(self.server.client_connection, mock_client_connection)
        self.mock_command_handler.assert_called_once()
        self.server.command_handler.start.assert_called_once()

    def test_main_loop_connection_error(self):
        """Test Hauptschleife des WebcamServers mit Verbindungsfehler."""
        # Mock-Konfiguration
        self.server.server_socket = MagicMock()
        self.mock_connection.accept_connection.side_effect = TCPConnectionError(
            "Verbindungsfehler"
        )

        # Setze is_running auf False nach einem Durchlauf
        def side_effect(*args, **kwargs):
            self.server.is_running = False
            raise TCPConnectionError("Verbindungsfehler")

        self.mock_connection.accept_connection.side_effect = side_effect

        # Test - sollte keine Ausnahme auslösen
        self.server.is_running = True
        self.server._main_loop()

        # Überprüfungen
        self.mock_connection.accept_connection.assert_called_once_with(
            self.server.server_socket
        )

    def test_process_commands(self):
        """Test Befehlsverarbeitung des WebcamServers."""
        # Mock-Konfiguration
        self.server.client_connection = MagicMock()
        self.server.camera_streamer = MagicMock()
        self.server.camera_streamer.capture_frame.return_value = b"test_jpeg_data"

        # Test mit 'SEND_IMAGE' Befehl
        self.server._send_image()

        # Überprüfungen
        self.server.camera_streamer.capture_frame.assert_called_once()
        self.server.client_connection.send_message.assert_called_once_with(
            b"test_jpeg_data"
        )

    def test_process_commands_capture_error(self):
        """Test Befehlsverarbeitung des WebcamServers mit Fehler bei der Bilderfassung."""
        # Mock-Konfiguration
        self.server.client_connection = MagicMock()
        self.server.camera_streamer = MagicMock()
        self.server.camera_streamer.capture_frame.side_effect = RuntimeError(
            "Kamera-Fehler"
        )

        # Test
        self.server._send_image()

        # Überprüfungen
        self.server.camera_streamer.capture_frame.assert_called_once()
        self.server.client_connection.send_message.assert_not_called()

    def test_send_image_no_client(self):
        """Test Bildversand ohne Client-Verbindung."""
        self.server.client_connection = None
        self.server.camera_streamer = MagicMock()

        self.server._send_image()

        self.server.camera_streamer.capture_frame.assert_not_called()

    def test_process_commands_send_error(self):
        """Test Befehlsverarbeitung des WebcamServers mit Fehler beim Senden."""
        # Mock-Konfiguration
        mock_client = MagicMock()
        mock_client.send_message.side_effect = ConnectionWriteError("Sende-Fehler")
        self.server.client_connection = mock_client
        self.server.camera_streamer = MagicMock()
        self.server.camera_streamer.capture_frame.return_value = b"test_jpeg_data"

        # Patch _handle_client_disconnection, um zu verhindern, dass client_connection auf None gesetzt wird
        with patch.object(self.server, "_handle_client_disconnection"):
            # Test mit 'SEND_IMAGE' Befehl - sollte keine Ausnahme auslösen
            self.server._send_image()

            # Überprüfungen
            self.server.camera_streamer.capture_frame.assert_called_once()
            mock_client.send_message.assert_called_once_with(b"test_jpeg_data")
            self.server._handle_client_disconnection.assert_called_once()


if __name__ == "__main__":
    unittest.main()
