import socket
from unittest.mock import patch, MagicMock

import numpy as np

from modules.tcp_ip.server import (
    setup_server_socket,
    stream_camera,
    serve_webcam_stream
)


class TestSetupServerSocket:
    """Tests for the setup_server_socket function."""

    def test_setup_server_socket_success(self):
        """Test successful server socket setup."""
        with patch("socket.socket") as mock_socket:
            mock_server = MagicMock()
            mock_socket.return_value = mock_server

            result = setup_server_socket("localhost", 8000)

            mock_socket.assert_called_once_with(socket.AF_INET, socket.SOCK_STREAM)
            
            mock_server.setsockopt.assert_called_once_with(
                socket.SOL_SOCKET, socket.SO_REUSEADDR, 1
            )
            
            mock_server.bind.assert_called_once_with(("localhost", 8000))
            
            mock_server.listen.assert_called_once_with(1)
            
            assert result == mock_server


class TestStreamCamera:
    """Tests for the stream_camera function."""

    def test_camera_not_opened(self):
        """Test behavior when camera cannot be opened."""
        mock_conn = MagicMock()
        
        with patch("cv2.VideoCapture") as mock_capture, \
             patch("sys.stderr") as mock_stderr:
            
            mock_camera = MagicMock()
            mock_camera.isOpened.return_value = False
            mock_capture.return_value = mock_camera
            
            stream_camera(mock_conn)
            
            assert any(
                "Error: Could not open video capture device." in str(call_args)
                for call_args in mock_stderr.write.call_args_list
            )

    def test_read_frame_failure(self):
        """Test behavior when reading a frame fails."""
        mock_conn = MagicMock()
        
        with patch("cv2.VideoCapture") as mock_capture, \
             patch("sys.stderr") as mock_stderr:
            
            mock_camera = MagicMock()
            mock_camera.isOpened.return_value = True
            mock_camera.read.return_value = (False, None)  # ret=False
            mock_capture.return_value = mock_camera
            
            stream_camera(mock_conn)
            
            assert any(
                "Warning: Failed to read frame from camera." in str(call_args)
                for call_args in mock_stderr.write.call_args_list
            )
            
            mock_camera.release.assert_called_once()

    def test_jpeg_encoding_failure(self):
        """Test behavior when JPEG encoding fails."""
        mock_conn = MagicMock()
        
        with patch("cv2.VideoCapture") as mock_capture, \
             patch("cv2.imencode") as mock_imencode, \
             patch("sys.stderr") as mock_stderr:
            
            mock_camera = MagicMock()
            mock_camera.isOpened.return_value = True
            mock_camera.read.return_value = (True, np.zeros((480, 640, 3), dtype=np.uint8))
            mock_capture.return_value = mock_camera
            
            mock_imencode.return_value = (False, None)
            
            stream_camera(mock_conn)
            
            assert any(
                "Warning: JPEG encoding failed." in str(call_args)
                for call_args in mock_stderr.write.call_args_list
            )
            
            mock_camera.release.assert_called_once()

    def test_client_disconnection(self):
        """Test behavior when client disconnects."""
        mock_conn = MagicMock()
        mock_conn.sendall.side_effect = BrokenPipeError("Connection broken")
        
        with patch("cv2.VideoCapture") as mock_capture, \
             patch("cv2.imencode") as mock_imencode, \
             patch("time.sleep") as mock_sleep:
            
            mock_camera = MagicMock()
            mock_camera.isOpened.return_value = True
            mock_camera.read.return_value = (True, np.zeros((480, 640, 3), dtype=np.uint8))
            mock_capture.return_value = mock_camera
            
            mock_jpeg = np.zeros((10, 10), dtype=np.uint8)
            mock_imencode.return_value = (True, mock_jpeg)
            
            stream_camera(mock_conn)
            
            mock_camera.release.assert_called_once()
            
            mock_sleep.assert_not_called()

    def test_connection_reset(self):
        """Test behavior when connection is reset."""
        mock_conn = MagicMock()
        mock_conn.sendall.side_effect = ConnectionResetError("Connection reset")
        
        with patch("cv2.VideoCapture") as mock_capture, \
             patch("cv2.imencode") as mock_imencode:
            
            mock_camera = MagicMock()
            mock_camera.isOpened.return_value = True
            mock_camera.read.return_value = (True, np.zeros((480, 640, 3), dtype=np.uint8))
            mock_capture.return_value = mock_camera
            
            mock_jpeg = np.zeros((10, 10), dtype=np.uint8)
            mock_imencode.return_value = (True, mock_jpeg)
            
            stream_camera(mock_conn)
            
            mock_camera.release.assert_called_once()

    def test_successful_frame_sending(self):
        """Test successful frame sending with sleep between frames."""
        mock_conn = MagicMock()
        
        with patch("cv2.VideoCapture") as mock_capture, \
             patch("cv2.imencode") as mock_imencode, \
             patch("time.sleep") as mock_sleep:
            
            mock_camera = MagicMock()
            mock_camera.isOpened.return_value = True
            
            mock_camera.read.side_effect = [
                (True, np.zeros((480, 640, 3), dtype=np.uint8)),
                (False, None)
            ]
            mock_capture.return_value = mock_camera
            
            mock_jpeg = np.zeros((10, 10), dtype=np.uint8)
            mock_imencode.return_value = (True, mock_jpeg)
            
            stream_camera(mock_conn)
            
            mock_conn.sendall.assert_called_once()
            
            mock_sleep.assert_called_once_with(10)
            
            mock_camera.release.assert_called_once()


class TestServeWebcamStream:
    """Tests for the serve_webcam_stream function."""

    def test_successful_connection_and_streaming(self):
        """Test successful connection acceptance and streaming."""
        with patch("modules.tcp_ip.server.setup_server_socket") as mock_setup, \
             patch("modules.tcp_ip.server.stream_camera") as mock_stream:
            
            mock_server = MagicMock()
            mock_conn = MagicMock()
            mock_addr = ("127.0.0.1", 12345)
            
            mock_server.__enter__.return_value = mock_server
            mock_server.accept.return_value = (mock_conn, mock_addr)
            
            mock_conn.__enter__.return_value = mock_conn
            
            mock_setup.return_value = mock_server
            
            serve_webcam_stream("localhost", 8000)
            
            mock_setup.assert_called_once_with("localhost", 8000)
            
            mock_server.accept.assert_called_once()
            
            mock_stream.assert_called_once_with(mock_conn)

    def test_socket_error_handling(self):
        """Test handling of socket errors."""
        with patch("modules.tcp_ip.server.setup_server_socket") as mock_setup, \
             patch("sys.stderr") as mock_stderr:
            
            mock_setup.side_effect = socket.error("Test socket error")
            
            serve_webcam_stream()
            
            assert any(
                "Network or I/O error: Test socket error" in str(call_args)
                for call_args in mock_stderr.write.call_args_list
            )

    def test_io_error_handling(self):
        """Test handling of IO errors."""
        with patch("modules.tcp_ip.server.setup_server_socket") as mock_setup, \
             patch("sys.stderr") as mock_stderr:
            
            mock_setup.side_effect = IOError("Test IO error")
            
            serve_webcam_stream()
            
            assert any(
                "Network or I/O error: Test IO error" in str(call_args)
                for call_args in mock_stderr.write.call_args_list
            )

    def test_general_exception_handling(self):
        """Test handling of general exceptions."""
        with patch("modules.tcp_ip.server.setup_server_socket") as mock_setup, \
             patch("sys.stderr") as mock_stderr:
            
            mock_setup.side_effect = Exception("Test general error")
            
            serve_webcam_stream()

            assert any(
                "Unexpected error: Test general error" in str(call_args)
                for call_args in mock_stderr.write.call_args_list
            )