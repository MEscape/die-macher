package com.die_macher.dobot.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Factory class for creating Dobot protocol messages.
 * This class provides various methods to generate properly formatted message packets
 * for different commands to be sent to the Dobot device.
 * Each method corresponds to a specific Dobot command as defined in the protocol.
 */
public class DobotMessageFactory {

    /**
     * Creates a message to retrieve the Device Serial Number (SN) from the Dobot.
     * This is a read-only command and does not require a payload.
     *
     * @return A byte array containing the complete message for GET_DEVICE_SN.
     */
    public static byte[] createGetDeviceSNMessage() {
        return DobotMessageBuilder.command(DobotProtocol.Commands.GET_DEVICE_SN)
                .control(false, false)
                .build();
    }

    /**
     * Creates a message to retrieve the Device Name from the Dobot.
     * This is a read-only command and does not require a payload.
     *
     * @return A byte array containing the complete message for GET_DEVICE_NAME.
     */
    public static byte[] createGetDeviceNameMessage() {
        return DobotMessageBuilder.command(DobotProtocol.Commands.GET_DEVICE_NAME)
                .control(false, false)
                .build();
    }

    /**
     * Creates a message to move the Dobot to a specified position (x, y, z, r).
     * The coordinates are serialized in little-endian format as floats.
     *
     * @param x         The X-coordinate for the target position.
     * @param y         The Y-coordinate for the target position.
     * @param z         The Z-coordinate for the target position.
     * @param r         The rotation for the target position.
     * @param isQueued  Whether the command should be queued for execution.
     * @return A byte array containing the complete message for SET_PTP_CMD.
     */
    public static byte[] createSetPTPCmdMessage(float x, float y, float z, float r, boolean isQueued) {
        // Allocate a ByteBuffer with 16 bytes (4 floats * 4 bytes each)
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(r);

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_PTP_CMD)
                .control(true, isQueued)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to set the Dobot to its home position.
     * This is a queued command, and the payload is reserved (4 bytes set to zero).
     *
     * @param isQueued  Whether the command should be queued for execution.
     * @return A byte array containing the complete message for SET_HOME_CMD.
     */
    public static byte[] createSetHomeCmdMessage(boolean isQueued) {
        // Allocate a ByteBuffer with 4 bytes (reserved 4 bytes, should be zeros)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0);

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_HOME_CMD)
                .control(true , isQueued)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to toggle the end effector suction cup on or off.
     * The state of suction is represented as a boolean and serialized as bytes.
     *
     * @param isSucked  If true, the suction cup is enabled; otherwise, it is disabled.
     * @param isQueued  Whether the command should be queued for execution.
     * @return A byte array containing the complete message for SET_END_EFFECTOR_SUCTION_CUP.
     */
    public static byte[] createSetEndEffectorSuctionCupMessage(boolean isSucked, boolean isQueued) {
        // Allocate a ByteBuffer with 2 bytes (2 booleans * 1 byte each)
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        byte isSuckedAsByte = (byte) (isSucked ? 1 : 0);
        buffer.put(isSuckedAsByte);
        buffer.put(isSuckedAsByte);

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_END_EFFECTOR_SUCTION_CUP)
                .control(true , isQueued)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to start execution of queued commands.
     *
     * @return A byte array containing the complete message for SET_QUEUED_CMD_START_EXEC.
     */
    public static byte[] createSetQueuedCmdStartExecMessage() {
        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_QUEUED_CMD_START_EXEC)
                .control(true , false)
                .build();
    }

    /**
     * Creates a message to clear all commands from the queue.
     *
     * @return A byte array containing the complete message for SET_QUEUED_CMD_CLEAR.
     */
    public static byte[] createSetQueuedCmdClearMessage() {
        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_QUEUED_CMD_CLEAR)
                .control(true , false)
                .build();
    }

    /**
     * Creates a message to stop the execution of queued commands.
     *
     * @return A byte array containing the complete message for SET_QUEUED_CMD_STOP.
     */
    public static byte[] createSetQueuedCmdStopMessage() {
        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_QUEUED_CMD_STOP)
                .control(true , false)
                .build();
    }

    /**
     * Creates a message to set the home parameters of the Dobot.
     * This sets the coordinates the Dobot considers its home position.
     *
     * @return A byte array containing the complete message for SET_HOME_PARAMS.
     */
    public static byte[] createSetHomeParamsMessage() {
        // Allocate a ByteBuffer with 16 bytes (4 floats * 4 bytes each)
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putFloat(0.0f);   // X-coordinate
        buffer.putFloat(45.0f);  // Y-coordinate
        buffer.putFloat(45.0f);  // Z-coordinate
        buffer.putFloat(0.0f);   // Rotation

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_HOME_PARAMS)
                .control(true, false)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to configure the PTP coordinate parameters for the Dobot.
     *
     * @param xyzVelocity       Velocity for the XYZ axes.
     * @param rVelocity         Velocity for the rotation axis.
     * @param xyzAcceleration   Acceleration for the XYZ axes.
     * @param rAcceleration     Acceleration for the rotation axis.
     * @return A byte array containing the complete message for SET_PTP_COORDINATE_PARAMS.
     */
    public static byte[] createSetPTPCoordinateParamsMessage(float xyzVelocity, float rVelocity,
                                                             float xyzAcceleration, float rAcceleration) {
        // Allocate a ByteBuffer with 16 bytes (4 floats * 4 bytes each)
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putFloat(xyzVelocity);
        buffer.putFloat(rVelocity);
        buffer.putFloat(xyzAcceleration);
        buffer.putFloat(rAcceleration);

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_PTP_COORDINATE_PARAMS)
                .control(true, false)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to configure the PTP joint parameters for the Dobot.
     *
     * @param velocity     The velocity for each joint.
     * @param acceleration The acceleration for each joint.
     * @return A byte array containing the complete message for SET_PTP_JOINT_PARAMS.
     */
    public static byte[] createSetPTPJointParamsMessage(float velocity, float acceleration) {
        // Allocate a ByteBuffer with 32 bytes (4 * 2 floats * 4 bytes each)
        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < 4; i++) {
            buffer.putFloat(velocity);
        }

        for (int i = 0; i < 4; i++) {
            buffer.putFloat(acceleration);
        }

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_PTP_JOINT_PARAMS)
                .control(true, false)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to configure the PTP common parameters for the Dobot.
     *
     * @param velocityRatio       The velocity ratio for all movements.
     * @param accelerationRatio   The acceleration ratio for all movements.
     * @return A byte array containing the complete message for SET_PTP_COMMON_PARAMS.
     */
    public static byte[] createSetPTPCommonParamsMessage(float velocityRatio, float accelerationRatio) {
        // Allocate a ByteBuffer with 8 bytes (2 floats * 4 bytes each)
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putFloat(velocityRatio);
        buffer.putFloat(accelerationRatio);

        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_PTP_COMMON_PARAMS)
                .control(true, false)
                .payload(buffer.array())
                .build();
    }

    /**
     * Creates a message to set the device name for the Dobot.
     *
     * @param deviceName The new name for the Dobot.
     * @return A byte array containing the complete message for SET_DEVICE_NAME.
     */
    public static byte[] createSetDeviceNameMessage(String deviceName) {
        return DobotMessageBuilder.command(DobotProtocol.Commands.SET_DEVICE_NAME)
                .control(true, false)
                .payload(deviceName.getBytes(StandardCharsets.UTF_8))
                .build();
    }
}