package com.die_macher.pick_and_place.dobot.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.die_macher.pick_and_place.dobot.config.DobotSerialConnector;
import com.die_macher.pick_and_place.dobot.exception.DobotCommunicationException;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the AbstractDobotCommand class. Tests focus on exception scenarios in the execute
 * method.
 */
@ExtendWith(MockitoExtension.class)
class AbstractDobotCommandTest {

  @Mock private DobotSerialConnector connector;

  private TestDobotCommand command;

  @BeforeEach
  void setUp() {
    command = new TestDobotCommand();
  }

  @Test
  @DisplayName("Should throw exception when not connected to Dobot")
  void testNotConnectedToDobot() {
    // Arrange
    when(connector.isConnected()).thenReturn(false);

    // Act & Assert
    DobotCommunicationException exception =
        assertThrows(
            DobotCommunicationException.class,
            () -> command.execute(connector),
            "Should throw exception when not connected");

    assertEquals(
        "Not connected to Dobot",
        exception.getMessage(),
        "Exception message should indicate connection issue");
    verify(connector).isConnected();
    verify(connector, never()).sendData(any());
    verify(connector, never()).readData(anyInt());
  }

  @Test
  @DisplayName("Should throw exception when failed to send command")
  void testFailedToSendCommand() {
    // Arrange
    when(connector.isConnected()).thenReturn(true);
    when(connector.sendData(any(byte[].class))).thenReturn(false);

    // Act & Assert
    DobotCommunicationException exception =
        assertThrows(
            DobotCommunicationException.class,
            () -> command.execute(connector),
            "Should throw exception when send fails");

    assertEquals(
        "Failed to send command to Dobot",
        exception.getMessage(),
        "Exception message should indicate send failure");
    verify(connector).isConnected();
    verify(connector).sendData(any());
    verify(connector, never()).readData(anyInt());
  }

  @Test
  @DisplayName("Should throw exception when no response received")
  void testNoResponseReceived() {
    // Arrange
    when(connector.isConnected()).thenReturn(true);
    when(connector.sendData(any(byte[].class))).thenReturn(true);
    when(connector.readData(anyInt())).thenReturn(null);

    // Act & Assert
    DobotCommunicationException exception =
        assertThrows(
            DobotCommunicationException.class,
            () -> command.execute(connector),
            "Should throw exception when no response");

    assertEquals(
        "No response received for command",
        exception.getMessage(),
        "Exception message should indicate no response");
    verify(connector).isConnected();
    verify(connector).sendData(any());
    verify(connector).readData(anyInt());
  }

  @Test
  @DisplayName("Should throw exception when empty response received")
  void testEmptyResponseReceived() {
    // Arrange
    when(connector.isConnected()).thenReturn(true);
    when(connector.sendData(any(byte[].class))).thenReturn(true);
    when(connector.readData(anyInt())).thenReturn(new byte[0]);

    // Act & Assert
    DobotCommunicationException exception =
        assertThrows(
            DobotCommunicationException.class,
            () -> command.execute(connector),
            "Should throw exception when empty response");

    assertEquals(
        "No response received for command",
        exception.getMessage(),
        "Exception message should indicate no response");
    verify(connector).isConnected();
    verify(connector).sendData(any());
    verify(connector).readData(anyInt());
  }

  /** Test implementation of AbstractDobotCommand for testing purposes. */
  private static class TestDobotCommand extends AbstractDobotCommand<String> {

    @Override
    protected byte[] createMessage() {
      return new byte[] {0x01, 0x02, 0x03};
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
      return DobotProtocol.Commands.GET_DEVICE_SN; // Using an existing command for testing
    }

    @Override
    protected String parseResponse(byte[] response) {
      // Default implementation that would be overridden in spy
      return new String(response);
    }

    // Helper method to make testing easier
    protected boolean validateResponseFormat(byte[] response) {
      return DobotProtocol.validateResponseFormat(response, getCommandType());
    }
  }
}
