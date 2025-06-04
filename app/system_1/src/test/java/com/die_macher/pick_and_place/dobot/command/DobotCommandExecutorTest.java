package com.die_macher.pick_and_place.dobot.command;

import static com.die_macher.pick_and_place.dobot.protocol.DobotProtocol.HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.die_macher.pick_and_place.dobot.config.DobotSerialConnector;
import com.die_macher.pick_and_place.dobot.exception.DobotCommunicationException;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;
import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the DobotCommandExecutor class. Tests the command execution and interaction with
 * the DobotSerialConnector.
 */
@ExtendWith(MockitoExtension.class)
class DobotCommandExecutorTest {

  @Mock private DobotSerialConnector connector;

  private DobotCommandExecutor commandExecutor;

  // Common Test Constants
  private static final String DEVICE_SN = "SN12345678";
  private static final String DEVICE_NAME = "MyDobot";
  private static final String NEW_DEVICE_NAME = "NewDobotName";

  @BeforeEach
  void setUp() {
    commandExecutor = new DobotCommandExecutor(connector);
  }

  // ===== Helper Methods =====
  private void mockSuccessfulCommunication(DobotProtocol.Commands commandId, byte[] payload) {
    byte[] response = new byte[DobotProtocol.Indices.MIN_MESSAGE_SIZE + payload.length - 1];

    // Set Header
    response[0] = HEADER[0];
    response[1] = HEADER[1];

    // Set Command ID
    response[DobotProtocol.Indices.COMMAND_INDEX] = (byte) commandId.getValue();

    // Set Controll Byte
    response[DobotProtocol.Indices.CONTROL_INDEX] = 0x00;

    // Set Payload Length
    response[DobotProtocol.Indices.LENGTH_INDEX] = (byte) (payload.length + 2);

    // Set Payload
    System.arraycopy(payload, 0, response, DobotProtocol.Indices.PAYLOAD_INDEX, payload.length);

    // Compute and set checksum
    byte checksum = DobotProtocol.calculateChecksum(response);

    // Create final message with checksum
    byte[] completeMessage = new byte[response.length + 1];
    System.arraycopy(response, 0, completeMessage, 0, response.length);
    completeMessage[completeMessage.length - 1] = checksum;

    // Mock connector behavior
    when(connector.isConnected()).thenReturn(true);
    when(connector.sendData(any(byte[].class))).thenReturn(true);
    doAnswer(invocation -> completeMessage).when(connector).readData(anyInt());
  }

  private void verifyCommunication() {
    verify(connector, atLeastOnce()).isConnected();
    verify(connector, atLeastOnce()).sendData(any(byte[].class));
    verify(connector, atLeastOnce()).readData(anyInt());
  }

  // ===== Get Commands =====
  @Test
  @DisplayName("Should get device serial number successfully")
  void shouldGetDeviceSerialNumber() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.GET_DEVICE_SN, DEVICE_SN.getBytes());
    String result = commandExecutor.getDeviceSN();
    assertEquals(DEVICE_SN, result, "Should return the correct serial number");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should get device name successfully")
  void shouldGetDeviceName() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.GET_DEVICE_NAME, DEVICE_NAME.getBytes());
    String result = commandExecutor.getDeviceName();
    assertEquals(DEVICE_NAME, result, "Should return the correct device name");
    verifyCommunication();
  }

  // ===== Set Commands =====
  @Test
  @DisplayName("Should set device name successfully")
  void shouldSetDeviceName() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_DEVICE_NAME, new byte[] {1});
    boolean result = commandExecutor.setDeviceName(NEW_DEVICE_NAME);
    assertTrue(result, "Should return true for successful device name setting");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should set lift height successfully")
  void shouldSetLiftHeight() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_PTP_JUMP_PARAMS, new byte[] {1});
    boolean result = commandExecutor.setLiftHeight(50.0f, 150.0f, true);
    assertTrue(result, "Should return true for successful height setting");
    verifyCommunication();
  }

  // ===== Movement Commands =====
  @Test
  @DisplayName("Should move to position successfully")
  void shouldMoveToPosition() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_PTP_CMD, new byte[] {1});
    boolean result =
        commandExecutor.moveToPosition(PTPModes.MOVJ_XYZ, 100.0f, 150.0f, 50.0f, 30.0f, true);
    assertTrue(result, "Should return true for successful move command");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should go home successfully")
  void shouldGoHome() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_HOME_CMD, new byte[] {1});
    boolean result = commandExecutor.goHome(true);
    assertTrue(result, "Should return true for successful go home command");
    verifyCommunication();
  }

  // ===== Queue Commands =====
  @Test
  @DisplayName("Should execute queue successfully")
  void shouldExecuteQueue() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_QUEUED_CMD_START_EXEC, new byte[] {1});
    boolean result = commandExecutor.executeQueue();
    assertTrue(result, "Should return true for successful queue execution");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should clear queue successfully")
  void shouldClearQueue() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_QUEUED_CMD_CLEAR, new byte[] {1});
    boolean result = commandExecutor.clearQueue();
    assertTrue(result, "Should return true for successful queue clearing");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should stop queue execution successfully")
  void shouldStopExecuteQueue() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_QUEUED_CMD_STOP, new byte[] {1});
    boolean result = commandExecutor.stopExecuteQueue();
    assertTrue(result, "Should return true for successful queue stopping");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should set default home command successfully")
  void shouldSetDefaultHomeCommand() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_HOME_PARAMS, new byte[] {1});
    boolean result = commandExecutor.setDefaultHomeCommand(1.0F, 1.0F, 1.0F, 0.0F, true);
    assertTrue(result, "Should return true for successful default home command");
    verifyCommunication();
  }

  // ===== Additional Tests for Missing Methods =====

  @Test
  @DisplayName("Should set movement configuration successfully")
  void shouldSetMovementConfig() throws DobotCommunicationException {
    mockSuccessfulCommunication(DobotProtocol.Commands.SET_PTP_COORDINATE_PARAMS, new byte[] {1});
    boolean result = commandExecutor.setMovementConfig(100.0f, 50.0f, 200.0f, 100.0f, true);
    assertTrue(result, "Should return true for successful movement configuration");
    verifyCommunication();
  }

  @Test
  @DisplayName("Should set vacuum state successfully")
  void shouldSetVacuumState() throws DobotCommunicationException {
    mockSuccessfulCommunication(
        DobotProtocol.Commands.SET_END_EFFECTOR_SUCTION_CUP, new byte[] {1});
    boolean result = commandExecutor.setVacuumState(true, true);
    assertTrue(result, "Should return true for successful vacuum state change");
    verifyCommunication();
  }
}
