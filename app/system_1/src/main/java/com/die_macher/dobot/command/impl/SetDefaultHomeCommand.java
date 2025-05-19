package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

public class SetDefaultHomeCommand extends AbstractDobotCommand<Boolean> {
    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetHomeParamsMessage();
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_HOME_CMD;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}
