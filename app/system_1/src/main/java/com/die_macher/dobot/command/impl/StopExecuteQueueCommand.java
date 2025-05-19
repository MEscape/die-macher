package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

public class StopExecuteQueueCommand extends AbstractDobotCommand<Boolean> {
    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetQueuedCmdStopMessage();
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_QUEUED_CMD_STOP;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}