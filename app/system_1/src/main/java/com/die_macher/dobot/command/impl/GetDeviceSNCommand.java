package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

import java.nio.charset.StandardCharsets;

public class GetDeviceSNCommand extends AbstractDobotCommand<String> {
    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createGetDeviceSNMessage();
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.GET_DEVICE_SN;
    }

    @Override
    protected String parseResponse(byte[] response) {
        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null ? new String(payload, StandardCharsets.UTF_8) : null;
    }
}