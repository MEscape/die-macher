package com.die_macher.dobot.service;

import com.die_macher.dobot.command.DobotCommandExecutor;
import com.die_macher.dobot.config.DobotProperties;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.exception.DobotCommunicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-Tests für die DobotServiceImpl-Klasse.
 * Diese Tests überprüfen die Funktionalität des Dobot-Service mit gemockten Abhängigkeiten.
 */
@ExtendWith(MockitoExtension.class)
public class DobotServiceTest {

    @Mock
    private DobotProperties properties;

    @Mock
    private DobotSerialConnector connector;

    @Mock
    private DobotCommandExecutor commandExecutor;

    @InjectMocks
    private DobotServiceImpl dobotService;

    @BeforeEach
    public void setUp() {
        // Ersetze den automatisch erstellten CommandExecutor durch unseren Mock
        try {
            // Verwende Reflection, um den privaten commandExecutor zu ersetzen
            java.lang.reflect.Field field = DobotServiceImpl.class.getDeclaredField("commandExecutor");
            field.setAccessible(true);
            field.set(dobotService, commandExecutor);
        } catch (Exception e) {
            fail("Konnte den CommandExecutor nicht ersetzen: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Sollte erfolgreich zum Dobot verbinden")
    public void testConnectToDobot() throws DobotCommunicationException {
        // Setze Standard-Werte für die Properties
        when(properties.getPortName()).thenReturn("COM3");
        when(properties.getTimeoutMillis()).thenReturn(5000);

        // Arrange
        when(connector.isConnected()).thenReturn(false);
        when(connector.connect(anyString(), anyInt())).thenReturn(true);

        // Act
        dobotService.connectToDobot();

        // Assert
        verify(connector).connect("COM3", 5000);
    }

    @Test
    @DisplayName("Sollte keine Verbindung herstellen, wenn bereits verbunden")
    public void testConnectToDobotWhenAlreadyConnected() throws DobotCommunicationException {
        // Arrange
        when(connector.isConnected()).thenReturn(true);

        // Act
        dobotService.connectToDobot();

        // Assert
        verify(connector, never()).connect(anyString(), anyInt());
    }

    @Test
    @DisplayName("Sollte Exception werfen, wenn Verbindung fehlschlägt")
    public void testConnectToDobotFailure() {
        // Setze Standard-Werte für die Properties
        when(properties.getPortName()).thenReturn("COM3");
        when(properties.getTimeoutMillis()).thenReturn(5000);

        // Arrange
        when(connector.isConnected()).thenReturn(false);
        when(connector.connect(anyString(), anyInt())).thenReturn(false);

        // Act & Assert
        assertThrows(DobotCommunicationException.class, () -> dobotService.connectToDobot());
    }

    @Test
    @DisplayName("Sollte vom Dobot trennen")
    public void testDisconnectFromDobot() {
        // Act
        dobotService.disconnectFromDobot();

        // Assert
        verify(connector).disconnect();
        assertFalse(dobotService.isInitialized());
    }

    @Test
    @DisplayName("Sollte den Verbindungsstatus korrekt zurückgeben")
    public void testIsConnected() {
        // Arrange
        when(connector.isConnected()).thenReturn(true);

        // Act & Assert
        assertTrue(dobotService.isConnected());

        // Arrange für den negativen Fall
        when(connector.isConnected()).thenReturn(false);

        // Act & Assert für den negativen Fall
        assertFalse(dobotService.isConnected());
    }

    @Test
    @DisplayName("Sollte den Initialisierungsstatus korrekt zurückgeben")
    public void testIsInitialized() {
        // Der Standardwert ist false
        assertFalse(dobotService.isInitialized());

        // Setze den Status auf true mit Reflection
        try {
            java.lang.reflect.Field field = DobotServiceImpl.class.getDeclaredField("isInitialized");
            field.setAccessible(true);
            field.set(dobotService, true);

            // Überprüfe, dass der Status jetzt true ist
            assertTrue(dobotService.isInitialized());
        } catch (Exception e) {
            fail("Konnte den isInitialized-Status nicht setzen: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Sollte den Dobot erfolgreich pingen")
    public void testPingDobotSuccess() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.getDeviceSN()).thenReturn("SN12345");

        // Act
        boolean result = dobotService.pingDobot();

        // Assert
        assertTrue(result);
        verify(commandExecutor).getDeviceSN();
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn Ping fehlschlägt")
    public void testPingDobotFailure() throws DobotCommunicationException {
        // Arrange - leere Seriennummer
        when(commandExecutor.getDeviceSN()).thenReturn("");

        // Act
        boolean result = dobotService.pingDobot();

        // Assert
        assertFalse(result);

        // Arrange - null Seriennummer
        when(commandExecutor.getDeviceSN()).thenReturn(null);

        // Act
        result = dobotService.pingDobot();

        // Assert
        assertFalse(result);

        // Arrange - Exception
        when(commandExecutor.getDeviceSN()).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        result = dobotService.pingDobot();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte den Gerätenamen korrekt abrufen")
    public void testGetDeviceName() throws DobotCommunicationException {
        // Arrange
        String expectedName = "TestDobot";
        when(commandExecutor.getDeviceName()).thenReturn(expectedName);

        // Act
        String result = dobotService.getDeviceName();

        // Assert
        assertEquals(expectedName, result);
        verify(commandExecutor).getDeviceName();
    }

    @Test
    @DisplayName("Sollte null zurückgeben, wenn das Abrufen des Gerätenamens fehlschlägt")
    public void testGetDeviceNameFailure() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.getDeviceName()).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        String result = dobotService.getDeviceName();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Sollte den Dobot erfolgreich zu einer Position bewegen")
    public void testMoveToPosition() throws DobotCommunicationException {
        // Arrange
        float x = 100.0f;
        float y = 150.0f;
        float z = 50.0f;
        float r = 30.0f;
        when(commandExecutor.moveToPosition(x, y, z, r, true)).thenReturn(true);

        // Act
        boolean result = dobotService.moveToPosition(x, y, z, r);

        // Assert
        assertTrue(result);
        verify(commandExecutor).moveToPosition(x, y, z, r, true);
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn die Bewegung fehlschlägt")
    public void testMoveToPositionFailure() throws DobotCommunicationException {
        // Arrange
        float x = 100.0f;
        float y = 150.0f;
        float z = 50.0f;
        float r = 30.0f;
        when(commandExecutor.moveToPosition(x, y, z, r, true))
                .thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.moveToPosition(x, y, z, r);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte den Dobot erfolgreich zur Home-Position bewegen")
    public void testGoHome() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.goHome(true)).thenReturn(true);

        // Act
        boolean result = dobotService.goHome();

        // Assert
        assertTrue(result);
        verify(commandExecutor).goHome(true);
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn die Home-Bewegung fehlschlägt")
    public void testGoHomeFailure() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.goHome(true)).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.goHome();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte den Vakuumzustand erfolgreich setzen")
    public void testSetVacuumState() throws DobotCommunicationException {
        // Arrange
        boolean isSucked = true;
        when(commandExecutor.setVacuumState(isSucked, true)).thenReturn(true);

        // Act
        boolean result = dobotService.setVacuumState(isSucked);

        // Assert
        assertTrue(result);
        verify(commandExecutor).setVacuumState(isSucked, true);

        // Test mit isSucked = false
        when(commandExecutor.setVacuumState(false, true)).thenReturn(true);
        result = dobotService.setVacuumState(false);
        assertTrue(result);
        verify(commandExecutor).setVacuumState(false, true);
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn das Setzen des Vakuumzustands fehlschlägt")
    public void testSetVacuumStateFailure() throws DobotCommunicationException {
        // Arrange
        boolean isSucked = true;
        when(commandExecutor.setVacuumState(isSucked, true))
                .thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.setVacuumState(isSucked);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte die Warteschlange erfolgreich ausführen")
    public void testExecuteQueue() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.executeQueue()).thenReturn(true);

        // Act
        boolean result = dobotService.executeQueue();

        // Assert
        assertTrue(result);
        verify(commandExecutor).executeQueue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn die Ausführung der Warteschlange fehlschlägt")
    public void testExecuteQueueFailure() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.executeQueue()).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.executeQueue();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte die Standard-Home-Position erfolgreich setzen")
    public void testSetDefaultHome() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.setDefaultHomeCommand()).thenReturn(true);

        // Act
        boolean result = dobotService.setDefaultHome();

        // Assert
        assertTrue(result);
        verify(commandExecutor).setDefaultHomeCommand();
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn das Setzen der Standard-Home-Position fehlschlägt")
    public void testSetDefaultHomeFailure() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.setDefaultHomeCommand()).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.setDefaultHome();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte die Bewegungskonfiguration erfolgreich setzen")
    public void testSetMovementConfig() throws DobotCommunicationException {
        // Arrange
        float xyzVelocity = 100.0f;
        float rVelocity = 100.0f;
        float xyzAcceleration = 80.0f;
        float rAcceleration = 80.0f;
        when(commandExecutor.setMovementConfig(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration))
                .thenReturn(true);

        // Act
        boolean result = dobotService.setMovementConfig(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);

        // Assert
        assertTrue(result);
        verify(commandExecutor).setMovementConfig(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn das Setzen der Bewegungskonfiguration fehlschlägt")
    public void testSetMovementConfigFailure() throws DobotCommunicationException {
        // Arrange
        float xyzVelocity = 100.0f;
        float rVelocity = 100.0f;
        float xyzAcceleration = 80.0f;
        float rAcceleration = 80.0f;
        when(commandExecutor.setMovementConfig(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration))
                .thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.setMovementConfig(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte den Gerätenamen erfolgreich setzen")
    public void testSetDeviceName() throws DobotCommunicationException {
        // Arrange
        String deviceName = "NewDobotName";
        when(commandExecutor.setDeviceName(deviceName)).thenReturn(true);

        // Act
        boolean result = dobotService.setDeviceName(deviceName);

        // Assert
        assertTrue(result);
        verify(commandExecutor).setDeviceName(deviceName);
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn das Setzen des Gerätenamens fehlschlägt")
    public void testSetDeviceNameFailure() throws DobotCommunicationException {
        // Arrange
        String deviceName = "NewDobotName";
        when(commandExecutor.setDeviceName(deviceName)).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.setDeviceName(deviceName);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte die Warteschlange erfolgreich leeren")
    public void testClearQueue() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.clearQueue()).thenReturn(true);

        // Act
        boolean result = dobotService.clearQueue();

        // Assert
        assertTrue(result);
        verify(commandExecutor).clearQueue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn das Leeren der Warteschlange fehlschlägt")
    public void testClearQueueFailure() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.clearQueue()).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.clearQueue();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte die Ausführung der Warteschlange erfolgreich stoppen")
    public void testStopExecuteQueue() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.stopExecuteQueue()).thenReturn(true);

        // Act
        boolean result = dobotService.stopExecuteQueue();

        // Assert
        assertTrue(result);
        verify(commandExecutor).stopExecuteQueue();
    }

    @Test
    @DisplayName("Sollte false zurückgeben, wenn das Stoppen der Warteschlange fehlschlägt")
    public void testStopExecuteQueueFailure() throws DobotCommunicationException {
        // Arrange
        when(commandExecutor.stopExecuteQueue()).thenThrow(new DobotCommunicationException("Test-Fehler"));

        // Act
        boolean result = dobotService.stopExecuteQueue();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Sollte die Initialisierung korrekt durchführen")
    public void testInitialize() throws DobotCommunicationException {
        // Arrange
        when(connector.isConnected()).thenReturn(true);
        when(commandExecutor.getDeviceSN()).thenReturn("SN12345");

        // Act
        dobotService.initialize();

        // Assert
        verify(commandExecutor).getDeviceSN();
        assertTrue(dobotService.isInitialized());
    }

    @Test
    @DisplayName("Sollte die Initialisierung behandeln, wenn die Verbindung fehlschlägt")
    public void testInitializeConnectionFailure() throws DobotCommunicationException {
        // Setze Standard-Werte für die Properties
        when(properties.getPortName()).thenReturn("COM3");
        when(properties.getTimeoutMillis()).thenReturn(5000);

        // Arrange - Verbindung schlägt fehl
        when(connector.isConnected()).thenReturn(false);
        when(connector.connect(anyString(), anyInt())).thenReturn(false);

        // Act
        dobotService.initialize();

        // Assert
        assertFalse(dobotService.isInitialized());
    }

    @Test
    @DisplayName("Sollte die Initialisierung behandeln, wenn der Ping fehlschlägt")
    public void testInitializePingFailure() throws DobotCommunicationException {
        // Arrange - Verbindung erfolgreich, aber Ping schlägt fehl
        when(connector.isConnected()).thenReturn(true);
        when(commandExecutor.getDeviceSN()).thenReturn(null);

        // Act
        dobotService.initialize();

        // Assert
        assertFalse(dobotService.isInitialized());
    }

    @Test
    @DisplayName("Sollte die Initialisierung behandeln, wenn eine Exception auftritt")
    public void testInitializeException() throws DobotCommunicationException {
        // Setze Standard-Werte für die Properties
        when(properties.getPortName()).thenReturn("COM3");
        when(properties.getTimeoutMillis()).thenReturn(5000);

        // Arrange - Exception während der Verbindung
        when(connector.isConnected()).thenReturn(false);
        when(connector.connect(anyString(), anyInt())).thenThrow(new RuntimeException("Test-Fehler"));

        // Act
        dobotService.initialize();

        // Assert
        assertFalse(dobotService.isInitialized());
    }

    @Test
    @DisplayName("Sollte die Aufräumarbeiten korrekt durchführen")
    public void testCleanup() {
        // Act
        dobotService.cleanup();

        // Assert
        verify(connector).disconnect();
        assertFalse(dobotService.isInitialized());
    }
}
