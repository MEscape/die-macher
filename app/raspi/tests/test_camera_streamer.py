#!/usr/bin/env python3
"""
Tests für das CameraStreamer-Modul.

Diese Tests überprüfen die Funktionalität des CameraStreamer-Moduls,
einschließlich Kamerainitialisierung, Frame-Erfassung und Ressourcenfreigabe.
"""

import unittest
from unittest.mock import MagicMock, patch

from modules.tcp_ip.camera_streamer import CameraStreamer


class TestCameraStreamer(unittest.TestCase):
    """Testklasse für das CameraStreamer-Modul."""

    @patch("cv2.VideoCapture")
    def test_initialize_camera_success(self, mock_video_capture):
        """Test erfolgreiche Kamerainitialisierung."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        mock_instance.read.return_value = (True, MagicMock())

        # Test
        streamer = CameraStreamer(camera_index=0)

        # Überprüfungen
        self.assertTrue(streamer.is_initialized)
        mock_video_capture.assert_called_once_with(0)
        mock_instance.isOpened.assert_called_once()
        mock_instance.read.assert_called_once()

    @patch("cv2.VideoCapture")
    def test_initialize_camera_failure_not_opened(self, mock_video_capture):
        """Test Kamerainitialisierung fehlgeschlagen - Kamera kann nicht geöffnet werden."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = False

        # Test
        streamer = CameraStreamer(camera_index=0)

        # Überprüfungen
        self.assertFalse(streamer.is_initialized)
        mock_video_capture.assert_called_once_with(0)
        mock_instance.isOpened.assert_called_once()

    @patch("cv2.VideoCapture")
    def test_initialize_camera_failure_read_error(self, mock_video_capture):
        """Test Kamerainitialisierung fehlgeschlagen - Fehler beim Lesen des ersten Frames."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        mock_instance.read.return_value = (False, None)

        # Test
        streamer = CameraStreamer(camera_index=0)

        # Überprüfungen
        self.assertFalse(streamer.is_initialized)
        mock_video_capture.assert_called_once_with(0)
        mock_instance.isOpened.assert_called_once()
        mock_instance.read.assert_called_once()

    @patch("cv2.VideoCapture")
    def test_capture_frame_success(self, mock_video_capture):
        """Test erfolgreiche Frame-Erfassung und JPEG-Kodierung."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        mock_instance.read.return_value = (True, MagicMock())

        # Mock für cv2.imencode
        mock_frame = MagicMock()
        mock_jpeg = MagicMock()
        mock_jpeg.tobytes.return_value = b"test_jpeg_data"

        with patch("cv2.imencode", return_value=(True, mock_jpeg)):
            # Test
            streamer = CameraStreamer(camera_index=0)
            result = streamer.capture_frame()

            # Überprüfungen
            self.assertEqual(result, b"test_jpeg_data")
            mock_instance.read.assert_called_with()
            mock_jpeg.tobytes.assert_called_once()

    @patch("cv2.VideoCapture")
    def test_capture_frame_not_initialized(self, mock_video_capture):
        """Test Frame-Erfassung fehlgeschlagen - Kamera nicht initialisiert."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = False

        # Test
        streamer = CameraStreamer(camera_index=0)

        # Überprüfungen
        with self.assertRaises(RuntimeError):
            streamer.capture_frame()

    @patch("cv2.VideoCapture")
    def test_capture_frame_read_error(self, mock_video_capture):
        """Test Frame-Erfassung fehlgeschlagen - Fehler beim Lesen des Frames."""
        # Mock-Konfiguration für Initialisierung
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        # Erster Aufruf für Initialisierung erfolgreich, zweiter Aufruf für capture_frame fehlgeschlagen
        mock_instance.read.side_effect = [(True, MagicMock()), (False, None)]

        # Test
        streamer = CameraStreamer(camera_index=0)

        # Überprüfungen
        with self.assertRaises(RuntimeError):
            streamer.capture_frame()

    @patch("cv2.VideoCapture")
    def test_capture_frame_encoding_error(self, mock_video_capture):
        """Test Frame-Erfassung fehlgeschlagen - Fehler bei der JPEG-Kodierung."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        mock_instance.read.return_value = (True, MagicMock())

        # Mock für cv2.imencode mit Fehler
        with patch("cv2.imencode", return_value=(False, None)):
            # Test
            streamer = CameraStreamer(camera_index=0)

            # Überprüfungen
            with self.assertRaises(ValueError):
                streamer.capture_frame()

    @patch("cv2.VideoCapture")
    def test_release(self, mock_video_capture):
        """Test Ressourcenfreigabe."""
        # Mock-Konfiguration
        mock_instance = mock_video_capture.return_value
        mock_instance.isOpened.return_value = True
        mock_instance.read.return_value = (True, MagicMock())

        # Test
        streamer = CameraStreamer(camera_index=0)
        streamer.release()

        # Überprüfungen
        mock_instance.release.assert_called_once()
        self.assertFalse(streamer.is_initialized)

    def test_release_with_exception(self):
        """Test Ressourcenfreigabe mit Exception."""
        # Mock-Konfiguration
        mock_cap = MagicMock()
        mock_cap.release.side_effect = Exception("Release error")

        streamer = CameraStreamer()
        streamer.cap = mock_cap
        streamer.is_initialized = True

        # Test
        streamer.release()

        # Überprüfungen
        self.assertIsNone(streamer.cap)
        self.assertFalse(streamer.is_initialized)

    def test_init_camera_exception(self):
        """Test Kamerainitialisierung mit Exception."""
        streamer = CameraStreamer()
        with patch("cv2.VideoCapture") as mock_capture:
            mock_capture.side_effect = Exception("Camera initialization error")
            result = streamer._initialize_camera(0)
            self.assertFalse(result)
            self.assertIsNone(streamer.cap)
            self.assertFalse(streamer.is_initialized)

    def test_capture_frame_not_initialized(self):
        """Test that capture_frame raises an error if the camera is not initialized."""
        streamer = CameraStreamer(camera_index=0)
        streamer.is_initialized = False

        with self.assertRaises(RuntimeError) as context:
            streamer.capture_frame()

        self.assertEqual(str(context.exception), "Camera is not initialized.")

    def test_capture_frame_cap_is_none(self):
        """Test that capture_frame raises an error if the camera cap is None, even if initialized."""
        streamer = CameraStreamer(camera_index=0)
        streamer.is_initialized = True
        streamer.cap = None

        with self.assertRaises(RuntimeError) as context:
            streamer.capture_frame()

        # Check that the error message matches
        self.assertEqual(str(context.exception), "Camera is not initialized.")


if __name__ == "__main__":
    unittest.main()
