package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

public class SetDefaultHomeCommand extends AbstractDobotCommand<Boolean> {
    private final boolean isQueued;

    public SetDefaultHomeCommand(final boolean isQueued) {
        this.isQueued = isQueued;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetHomeParamsMessage(isQueued);
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_HOME_PARAMS;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}
