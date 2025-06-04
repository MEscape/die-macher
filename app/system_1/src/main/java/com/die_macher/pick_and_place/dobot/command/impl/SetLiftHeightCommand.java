package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;

public class SetLiftHeightCommand extends AbstractDobotCommand<Boolean> {
  private final float jumpHeight;
  private final float maxHeight;
  private final boolean isQueued;

  public SetLiftHeightCommand(float jumpHeight, float maxHeight, boolean isQueued) {
    this.isQueued = isQueued;
    this.jumpHeight = jumpHeight;
    this.maxHeight = maxHeight;
  }

  @Override
  protected byte[] createMessage() {
    return DobotMessageFactory.createSetPTPJumpParamsMessage(jumpHeight, maxHeight, isQueued);
  }

  @Override
  protected DobotProtocol.Commands getCommandType() {
    return DobotProtocol.Commands.SET_PTP_JUMP_PARAMS;
  }

  @Override
  protected Boolean parseResponse(byte[] response) {
    return true;
  }
}
