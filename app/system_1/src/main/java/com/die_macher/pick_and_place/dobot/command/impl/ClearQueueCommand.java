package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;

public class ClearQueueCommand extends AbstractDobotCommand<Boolean> {
    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetQueuedCmdClearMessage();
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_QUEUED_CMD_CLEAR;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}