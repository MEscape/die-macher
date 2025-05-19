package com.die_macher.dobot.command.impl;


import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

public class MoveToPositionCommand extends AbstractDobotCommand<Boolean> {
    private final float x;
    private final float y;
    private final float z;
    private final float r;
    private final boolean isQueued;

    public MoveToPositionCommand(float x, float y, float z, float r, boolean isQueued) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.isQueued = isQueued;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetPTPCmdMessage(x, y, z, r, isQueued);
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_PTP_CMD;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}
