#!/usr/bin/env python3
"""
Captures frames from a camera and encodes them as JPEG images.

This class handles the initialization of the camera, captures frames,
encodes them as JPEG images, and provides a method to release the camera
resource when no longer needed.

Features:
- Captures real-time frames from a camera device (default is camera index 0).
- Encodes captured frames as JPEG images.
- Provides error handling for device initialization and frame capture issues.
- Comprehensive logging for debugging and monitoring.

Dependencies:
- OpenCV (cv2): Required for camera capture and image encoding.
- logging: For structured logging of events and errors.
"""

import logging
from typing import Optional

import cv2

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s",
)
logger = logging.getLogger(__name__)


class CameraStreamer:
    """Handles camera capture and JPEG encoding."""

    def __init__(self, camera_index: int = 0):
        """
        Initializes the CameraStreamer with the specified camera index.

        Args:
            camera_index (int): Index of the camera device to use (default: 0).
        """
        self.camera_index = camera_index
        self.cap: Optional[cv2.VideoCapture] = None
        self.is_initialized = self._initialize_camera(camera_index)

    def _initialize_camera(self, camera_index: int) -> bool:
        """Initialize the camera with the specified index.

        Args:
            camera_index (int): Index of the camera device to use.

        Returns:
            bool: True if camera initialization was successful, False otherwise.
        """
        try:
            self.cap = cv2.VideoCapture(camera_index)
            if not self.cap.isOpened():
                logger.error("Failed to open camera %s", camera_index)
                return False

            # Read a test frame to verify camera is working
            success, _ = self.cap.read()
            if not success:
                logger.error("Failed to read initial frame from camera")
                self.release()
                return False

            logger.info("Camera %s initialized successfully", camera_index)
            return True
        except Exception as e:
            logger.error("Error initializing camera %s: %s", camera_index, e)
            self.release()
            return False

    def capture_frame(self) -> bytes:
        """Captures a frame, encodes it as JPEG, and returns its byte representation.

        Returns:
            bytes: JPEG encoded frame as bytes.

        Raises:
            RuntimeError: If camera is not initialized or frame capture fails.
            ValueError: If JPEG encoding fails.
        """
        if not self.is_initialized:
            raise RuntimeError("Camera is not initialized.")

        try:
            if self.cap is None:
                raise RuntimeError("Camera is not initialized.")

            ret, frame = self.cap.read()
            if not ret:
                raise RuntimeError(
                    f"Failed to read frame from camera {self.camera_index}"
                )

            result, jpeg = cv2.imencode(".jpg", frame)
            if not result:
                raise ValueError("JPEG encoding failed.")

            logger.debug(
                "Frame captured and encoded as JPEG. Size: %s bytes", len(jpeg)
            )
            return jpeg.tobytes()
        except Exception as e:
            logger.error("Error capturing frame: %s", e)
            raise

    def release(self) -> None:
        """Releases the camera resources.

        This method should be called when the camera is no longer needed to properly
        release hardware resources.
        """
        try:
            if self.cap is not None:
                self.cap.release()
                logger.info("Camera %s resources released", self.camera_index)
        except Exception as e:
            logger.error("Error releasing camera resources: %s", e)
        finally:
            self.is_initialized = False
            self.cap = None
