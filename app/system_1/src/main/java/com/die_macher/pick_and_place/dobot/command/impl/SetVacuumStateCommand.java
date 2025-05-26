package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;

public class SetVacuumStateCommand extends AbstractDobotCommand<Boolean> {
    private final boolean isQueued;
    private final boolean isSucked;

    public SetVacuumStateCommand(boolean isSucked, boolean isQueued) {
        this.isSucked = isSucked;
        this.isQueued = isQueued;
    }

    @Override
    protected byte[] createMessage() {
        return DobotMessageFactory.createSetEndEffectorSuctionCupMessage(isSucked, isQueued);
    }

    @Override
    protected DobotProtocol.Commands getCommandType() {
        return DobotProtocol.Commands.SET_END_EFFECTOR_SUCTION_CUP;
    }

    @Override
    protected Boolean parseResponse(byte[] response) {
        return true;
    }
}
