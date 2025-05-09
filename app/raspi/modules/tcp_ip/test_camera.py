# coverage: ignore
#!/usr/bin/env python3
"""
Einfaches Testskript zur Überprüfung der Kamera-Funktionalität.
Zeigt ein Vorschaufenster mit dem Kamerabild an und prüft grundlegende Funktionen.
"""
import sys
import time

import cv2


def test_camera() -> bool:
    """Test camera functionality by displaying preview window and checking basic features.

    Returns:
        bool: True if camera test succeeds, False if any errors occur.
    """
    print("Starte Kamera-Test...")

    # Initialisiere die Kamera
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Fehler: Kamera konnte nicht geöffnet werden!", file=sys.stderr)
        return False

    print("✓ Kamera erfolgreich initialisiert")

    # Zeige Kamera-Eigenschaften
    width = cap.get(cv2.CAP_PROP_FRAME_WIDTH)
    height = cap.get(cv2.CAP_PROP_FRAME_HEIGHT)
    fps = cap.get(cv2.CAP_PROP_FPS)
    print("Kamera-Eigenschaften:")
    print(f"- Auflösung: {width}x{height}")
    print(f"- FPS: {fps}")

    success = True

    try:
        print("\nÖffne Vorschaufenster (drücken Sie 'q' zum Beenden)...")
        frames_captured = 0
        start_time = time.time()

        while True:
            # Erfasse ein Frame
            ret, frame = cap.read()
            if not ret:
                print("Fehler: Frame konnte nicht gelesen werden!", file=sys.stderr)
                return False

            frames_captured += 1

            # Zeige das Frame an
            cv2.imshow("Kamera-Test", frame)

            # Beende mit 'q'
            if cv2.waitKey(1) & 0xFF == ord("q"):
                break

            # Zeige FPS nach 5 Sekunden
            if frames_captured == 1:
                print("✓ Erstes Frame erfolgreich erfasst")
            elif time.time() - start_time >= 5:
                actual_fps = frames_captured / (time.time() - start_time)
                print(f"✓ Aktuelle FPS: {actual_fps:.1f}")
                break

    except Exception as e:
        print(f"Unerwarteter Fehler: {e}", file=sys.stderr)
        success = False
    finally:
        # Aufräumen
        cap.release()
        cv2.destroyAllWindows()
        print("\nKamera-Test abgeschlossen")

    return success


def main() -> None:
    """Run the camera test and display results.

    Executes the camera test and provides feedback about the test results,
    including next steps for the user.
    """
    if test_camera():
        print("\n✓ Kamera-Test erfolgreich!")
        print("Der Server kann nun mit 'python main.py' gestartet werden.")
    else:
        print("\n✗ Kamera-Test fehlgeschlagen!")
        print("Bitte überprüfen Sie die Kamera-Verbindung und Berechtigungen.")


if __name__ == "__main__":
    main()
