package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;

public class SetMovementConfigCommand extends AbstractDobotCommand<Boolean> {
    private final float xyzVelocity;
    private final float rVelocity;
    private final float xyzAcceleration;
    private final float rAcceleration;
    private final boolean isQueued;

    public SetMovementConfigCommand(float xyzVelocity, float rVelocity,
                                    float xyzAcceleration, float rAcceleration,
                                    boolean isQueued) {
        this.isQueued = isQueued;
        this.xyzVelocity = xyzVelocity;
        this.rVelocity = rVelocity;
        this.xyzAcceleration = xyzAcceleration;
        this.rAcceleration = rAcceleration;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetPTPCoordinateParamsMessage(
                xyzVelocity, rVelocity, xyzAcceleration, rAcceleration, isQueued
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