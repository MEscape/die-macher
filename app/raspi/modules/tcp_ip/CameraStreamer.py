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

import cv2
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(threadName)s - %(message)s'
)
logger = logging.getLogger(__name__)

class CameraStreamer:
    """Handles camera capture and JPEG encoding."""
    
    def __init__(self, camera_index: int = 0):
        """
        Initializes the CameraStreamer with the specified camera index.
        """
        self.is_initialized = self._initialize_camera(camera_index)

    def _initialize_camera(self, camera_index) -> bool:
        """Initialize the camera"""
        self.cap = cv2.VideoCapture(camera_index)
        if not self.cap.isOpened():
            logger.error(f"Failed to open camera {self.camera_id}")
            return False

        # Read a test frame to verify camera is working
        success, _ = self.cap.read()
        if not success:
            logger.error("Failed to read initial frame from camera")
            return False#

        return True

    def capture_frame(self) -> bytes:
        """Captures a frame, encodes it as JPEG, and returns its byte representation."""
        if not self.is_initialized:
            raise RuntimeError("Camera is not initialized.")

        ret, frame = self.cap.read()
        if not ret:
            raise RuntimeError("Failed to read frame from camera.")
        
        result, jpeg = cv2.imencode(".jpg", frame)
        if not result:
            raise RuntimeError("JPEG encoding failed.")
        
        logger.debug(f"Frame captured and encoded as JPEG. Size: {len(jpeg)} bytes")
        return jpeg.tobytes()
