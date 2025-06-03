#!/usr/bin/env python3
"""
Tests für das CommandHandler-Modul.

Diese Tests überprüfen die Funktionalität des CommandHandler-Moduls,
einschließlich Befehlsverarbeitung, Thread-Verwaltung und Fehlerbehandlung.
"""

import threading
import unittest
from queue import Queue
from unittest.mock import MagicMock, patch

from modules.tcp_ip.command_handler import CommandHandler
from modules.tcp_ip.connection import (
    Connection,
    ConnectionClosedError,
    TCPConnectionError,
)


class TestCommandHandler(unittest.TestCase):
    """Testklasse für das CommandHandler-Modul."""

    def setUp(self):
        """Testumgebung einrichten."""
        self.mock_connection = MagicMock(spec=Connection)
        self.command_queue = Queue()
        self.on_connection_closed_called = False

        def on_connection_closed():
            self.on_connection_closed_called = True

        self.on_connection_closed = on_connection_closed

    def test_init(self):
        """Test Initialisierung des CommandHandlers."""
        handler = CommandHandler(
            connection=self.mock_connection,
            command_queue=self.command_queue,
            on_connection_closed=self.on_connection_closed,
        )

        self.assertEqual(handler.connection, self.mock_connection)
        self.assertEqual(handler.command_queue, self.command_queue)
        self.assertEqual(handler.on_connection_closed, self.on_connection_closed)
        self.assertTrue(handler._running)
        self.assertTrue(handler.daemon)  # Überprüfe, ob der Thread als Daemon läuft

    def test_stop(self):
        """Test Stoppen des CommandHandlers."""
        handler = CommandHandler(
            connection=self.mock_connection, command_queue=self.command_queue
        )

        handler.stop()
        self.assertFalse(handler._running)

    @patch.object(threading.Thread, "start")
    def test_run_process_commands(self, mock_start):
        """Test Ausführung des CommandHandlers mit erfolgreicher Befehlsverarbeitung."""
        # Mock für _process_commands
        with patch.object(CommandHandler, "_process_commands") as mock_process:
            # Test
            handler = CommandHandler(
                connection=self.mock_connection, command_queue=self.command_queue
            )
            handler.start()  # Dies ruft intern run() auf

            # Simuliere Beendigung des Threads
            handler.run()

            # Überprüfungen
            mock_process.assert_called_once()
            self.mock_connection.close.assert_called_once()

    @patch.object(threading.Thread, "start")
    def test_run_connection_closed_error(self, mock_start):
        """Test Ausführung des CommandHandlers mit ConnectionClosedError."""
        # Mock für _process_commands mit Ausnahme
        with patch.object(CommandHandler, "_process_commands") as mock_process:
            mock_process.side_effect = ConnectionClosedError("Verbindung geschlossen")

            # Test
            handler = CommandHandler(
                connection=self.mock_connection, command_queue=self.command_queue
            )
            handler.start()  # Dies ruft intern run() auf

            # Simuliere Beendigung des Threads
            handler.run()

            # Überprüfungen
            mock_process.assert_called_once()
            self.mock_connection.close.assert_called_once()

    @patch.object(threading.Thread, "start")
    def test_run_connection_error(self, mock_start):
        """Test Ausführung des CommandHandlers mit TCPConnectionError."""
        # Mock für _process_commands mit Ausnahme
        with patch.object(CommandHandler, "_process_commands") as mock_process:
            mock_process.side_effect = TCPConnectionError("Verbindungsfehler")

            # Test
            handler = CommandHandler(
                connection=self.mock_connection, command_queue=self.command_queue
            )
            handler.start()  # Dies ruft intern run() auf

            # Simuliere Beendigung des Threads
            handler.run()

            # Überprüfungen
            mock_process.assert_called_once()
            self.mock_connection.close.assert_called_once()

    @patch.object(threading.Thread, "start")
    def test_run_unexpected_error(self, mock_start):
        """Test Ausführung des CommandHandlers mit unerwarteter Ausnahme."""
        # Mock für _process_commands mit Ausnahme
        with patch.object(CommandHandler, "_process_commands") as mock_process:
            mock_process.side_effect = Exception("Unerwarteter Fehler")

            # Test
            handler = CommandHandler(
                connection=self.mock_connection, command_queue=self.command_queue
            )
            handler.start()  # Dies ruft intern run() auf

            # Simuliere Beendigung des Threads
            handler.run()

            # Überprüfungen
            mock_process.assert_called_once()
            self.mock_connection.close.assert_called_once()

    def test_process_commands(self):
        """Test Befehlsverarbeitungsschleife."""
        # Mock-Konfiguration
        self.mock_connection.recv_message.return_value = b"test_command"

        # Test
        handler = CommandHandler(
            connection=self.mock_connection, command_queue=self.command_queue
        )

        # Setze _running auf False nach einem Durchlauf
        def side_effect():
            handler._running = False
            return b"test_command"

        self.mock_connection.recv_message.side_effect = side_effect

        # Führe _process_commands aus
        handler._process_commands()

        # Überprüfungen
        self.mock_connection.recv_message.assert_called_once()
        self.assertEqual(self.command_queue.qsize(), 1)
        self.assertEqual(self.command_queue.get(), "test_command")

    def test_connection_closed_callback_error(self):
        """Test Fehlerbehandlung im Connection-Closed-Callback."""
        mock_callback = MagicMock()
        mock_callback.side_effect = Exception("Callback error")

        handler = CommandHandler(
            connection=self.mock_connection,
            command_queue=self.command_queue,
            on_connection_closed=mock_callback,
        )

        handler.start()
        handler.stop()
        handler.join()

        mock_callback.assert_called_once()
        self.mock_connection.close.assert_called_once()

    def test_process_commands_decode_error(self):
        """Test Befehlsverarbeitung mit Dekodierungsfehler."""
        self.mock_connection.recv_message.return_value = b"\xff\xff"  # Invalid UTF-8

        handler = CommandHandler(
            connection=self.mock_connection, command_queue=self.command_queue
        )
        handler._running = True

        # Setze _running auf False nach einem Durchlauf
        def side_effect():
            handler._running = False
            return b"\xff\xff"

        self.mock_connection.recv_message.side_effect = side_effect

        # Run command processing
        handler._process_commands()

        # Verify error handling
        self.mock_connection.recv_message.assert_called_once()
        self.assertTrue(self.command_queue.empty())

    @patch("modules.tcp_ip.command_handler.logger")
    def test_process_commands_connection_closed_error(self, mock_logger):
        """Test that ConnectionClosedError is handled and logged correctly."""
        handler = CommandHandler(
            connection=self.mock_connection, command_queue=self.command_queue
        )

        self.mock_connection.recv_message.side_effect = ConnectionClosedError(
            "Connection closed"
        )

        # Run _process_commands
        handler._process_commands()

        # Assertions
        mock_logger.info.assert_called_once_with(
            "Connection closed by peer, stopping command handler"
        )
        self.assertTrue(self.command_queue.empty())


if __name__ == "__main__":
    unittest.main()
