package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.AbstractDobotCommand;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;

public class SetMovementConfigCommand extends AbstractDobotCommand<Boolean> {
    private final float xyzVelocity;
    private final float rVelocity;
    private final float xyzAcceleration;
    private final float rAcceleration;

    public SetMovementConfigCommand(float xyzVelocity, float rVelocity,
                                    float xyzAcceleration, float rAcceleration) {
        this.xyzVelocity = xyzVelocity;
        this.rVelocity = rVelocity;
        this.xyzAcceleration = xyzAcceleration;
        this.rAcceleration = rAcceleration;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetPTPCoordinateParamsMessage(
                xyzVelocity, rVelocity, xyzAcceleration, rAcceleration
        );
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_PTP_COORDINATE_PARAMS;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}