#!/usr/bin/env python3
"""
Tests für das Connection-Modul.

Diese Tests überprüfen die Funktionalität des Connection-Moduls,
einschließlich Serverinitialisierung, Verbindungsannahme, Nachrichtenübertragung und Fehlerbehandlung.
"""

import socket
import struct
import unittest
from unittest.mock import MagicMock, call, patch

from modules.tcp_ip.connection import (
    Connection,
    ConnectionClosedError,
    ConnectionReadError,
    ConnectionWriteError,
    TCPConnectionError,
)


class TestConnection(unittest.TestCase):
    """Testklasse für das Connection-Modul."""

    @patch("socket.socket")
    def test_create_server_success(self, mock_socket):
        """Test erfolgreiche Serverinitialisierung."""
        # Mock-Konfiguration
        mock_instance = mock_socket.return_value

        # Test
        server_socket = Connection.create_server("localhost", 8000)

        # Überprüfungen
        mock_socket.assert_called_once_with(socket.AF_INET, socket.SOCK_STREAM)
        mock_instance.setsockopt.assert_called_once_with(
            socket.SOL_SOCKET, socket.SO_REUSEADDR, 1
        )
        mock_instance.bind.assert_called_once_with(("localhost", 8000))
        mock_instance.listen.assert_called_once_with(1)
        self.assertEqual(server_socket, mock_instance)

    @patch("socket.socket")
    def test_create_server_failure(self, mock_socket):
        """Test Serverinitialisierung fehlgeschlagen."""
        # Mock-Konfiguration
        mock_instance = mock_socket.return_value
        mock_instance.bind.side_effect = OSError("Test-Fehler")

        # Überprüfungen
        with self.assertRaises(TCPConnectionError):
            Connection.create_server("localhost", 8000)

    @patch("socket.socket")
    def test_accept_connection_success(self, mock_socket):
        """Test erfolgreiche Verbindungsannahme."""
        # Mock-Konfiguration
        mock_server_socket = MagicMock()
        mock_client_socket = MagicMock()
        mock_server_socket.accept.return_value = (
            mock_client_socket,
            ("127.0.0.1", 12345),
        )

        # Test
        connection, addr = Connection.accept_connection(mock_server_socket)

        # Überprüfungen
        mock_server_socket.accept.assert_called_once()
        self.assertIsInstance(connection, Connection)
        self.assertEqual(connection.sock, mock_client_socket)
        self.assertEqual(addr, ("127.0.0.1", 12345))

    @patch("socket.socket")
    def test_accept_connection_failure(self, mock_socket):
        """Test Verbindungsannahme fehlgeschlagen."""
        # Mock-Konfiguration
        mock_server_socket = MagicMock()
        mock_server_socket.accept.side_effect = OSError("Test-Fehler")

        # Überprüfungen
        with self.assertRaises(TCPConnectionError):
            Connection.accept_connection(mock_server_socket)

    def test_close(self):
        """Test Verbindungsschließung."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        connection = Connection(mock_socket)

        # Test
        connection.close()

        # Überprüfungen
        mock_socket.close.assert_called_once()

    def test_close_with_exception(self):
        """Test Verbindungsschließung mit Ausnahme."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        mock_socket.close.side_effect = Exception("Test-Fehler")
        connection = Connection(mock_socket)

        # Test - sollte keine Ausnahme auslösen
        connection.close()

        # Überprüfungen
        mock_socket.close.assert_called_once()

    def test_recv_exactly_success(self):
        """Test erfolgreiches Empfangen einer bestimmten Anzahl von Bytes."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        mock_socket.recv.side_effect = [b"test", b"data"]
        connection = Connection(mock_socket)

        # Test
        result = connection.recv_exactly(8)

        # Überprüfungen
        self.assertEqual(result, b"testdata")
        mock_socket.recv.assert_has_calls([call(8), call(4)])

    def test_recv_exactly_connection_closed(self):
        """Test Empfangen von Bytes bei geschlossener Verbindung."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        mock_socket.recv.return_value = b""
        connection = Connection(mock_socket)

        # Überprüfungen
        with self.assertRaises(ConnectionClosedError):
            connection.recv_exactly(8)

    def test_recv_exactly_error(self):
        """Test Fehler beim Empfangen von Bytes."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        mock_socket.recv.side_effect = OSError("Test-Fehler")
        connection = Connection(mock_socket)

        # Überprüfungen
        with self.assertRaises(ConnectionReadError):
            connection.recv_exactly(8)

    def test_recv_message_success(self):
        """Test erfolgreiches Empfangen einer Nachricht mit Header."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        # Header mit Länge 8 (\x00\x00\x00\x08) gefolgt von 8 Bytes Daten
        mock_socket.recv.side_effect = [struct.pack("!I", 8), b"testdata"]
        connection = Connection(mock_socket)

        # Patch recv_exactly, um die Implementierung zu vereinfachen
        with patch.object(
            connection, "recv_exactly", side_effect=[struct.pack("!I", 8), b"testdata"]
        ):
            # Test
            result = connection.recv_message()

            # Überprüfungen
            self.assertEqual(result, b"testdata")

    def test_send_message_success(self):
        """Test erfolgreiches Senden einer Nachricht mit Header."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        connection = Connection(mock_socket)

        # Test
        connection.send_message(b"testdata")

        # Überprüfe, ob send_all einmal aufgerufen wurde (mit Header + Daten)
        # Patch die send_all Methode, um die Implementierung zu testen
        with patch.object(connection, "send_all") as mock_send_all:
            connection.send_message(b"testdata")
            # Überprüfe, ob send_all mit dem kombinierten Header und Daten aufgerufen wurde
            mock_send_all.assert_called_once()
            # Überprüfe, ob die Daten korrekt sind (Header + Daten)
            call_args = mock_send_all.call_args[0][0]
            # Die ersten 4 Bytes sollten der Header sein
            header = call_args[:4]
            data = call_args[4:]
            # Überprüfe, ob der Header die richtige Länge enthält
            self.assertEqual(struct.unpack(">I", header)[0], 8)  # Länge von 'testdata'
            # Überprüfe, ob die Daten korrekt sind
            self.assertEqual(data, b"testdata")

    def test_send_message_error(self):
        """Test Fehler beim Senden einer Nachricht."""
        # Mock-Konfiguration
        mock_socket = MagicMock()
        mock_socket.sendall.side_effect = OSError("Test-Fehler")
        connection = Connection(mock_socket)

        # Überprüfungen
        with self.assertRaises(ConnectionWriteError):
            connection.send_message(b"testdata")

    def test_send_all_connection_reset(self):
        """Test Senden mit Connection Reset."""
        mock_socket = MagicMock()
        mock_socket.sendall.side_effect = ConnectionResetError("Connection reset")
        connection = Connection(mock_socket)

        with self.assertRaises(ConnectionClosedError):
            connection.send_all(b"testdata")

    def test_send_all_broken_pipe(self):
        """Test Senden mit Broken Pipe."""
        mock_socket = MagicMock()
        mock_socket.sendall.side_effect = BrokenPipeError("Broken pipe")
        connection = Connection(mock_socket)

        with self.assertRaises(ConnectionClosedError):
            connection.send_all(b"testdata")

    def test_send_all_timeout(self):
        """Test Senden mit Timeout."""
        mock_socket = MagicMock()
        mock_socket.sendall.side_effect = socket.timeout("Socket timeout")
        connection = Connection(mock_socket)

        with self.assertRaises(ConnectionWriteError):
            connection.send_all(b"testdata")

    def test_recv_exactly_connection_reset(self):
        """Test Empfangen mit Connection Reset."""
        mock_socket = MagicMock()
        mock_socket.recv.side_effect = ConnectionResetError("Connection reset")
        connection = Connection(mock_socket)

        with self.assertRaises(ConnectionClosedError):
            connection.recv_exactly(8)

    def test_recv_exactly_broken_pipe(self):
        """Test Empfangen mit Broken Pipe."""
        mock_socket = MagicMock()
        mock_socket.recv.side_effect = BrokenPipeError("Broken pipe")
        connection = Connection(mock_socket)

        with self.assertRaises(ConnectionClosedError):
            connection.recv_exactly(8)

    def test_recv_exactly_timeout(self):
        """Test Empfangen mit Timeout."""
        mock_socket = MagicMock()
        mock_socket.recv.side_effect = socket.timeout("Socket timeout")
        connection = Connection(mock_socket)

        with self.assertRaises(ConnectionReadError):
            connection.recv_exactly(8)


if __name__ == "__main__":
    unittest.main()
