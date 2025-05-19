package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

public class SetDeviceNameCommand extends AbstractDobotCommand<Boolean> {
    private final String deviceName;

    public SetDeviceNameCommand(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetDeviceNameMessage(deviceName);
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_DEVICE_NAME;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}

