package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;

public class SetDefaultHomeCommand extends AbstractDobotCommand<Boolean> {
    private final float x;
    private final float y;
    private final float z;
    private final float r;
    private final boolean isQueued;

    public SetDefaultHomeCommand(float x, float y, float z, float r, final boolean isQueued) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.isQueued = isQueued;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetHomeParamsMessage(x, y, z, r, isQueued);
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
