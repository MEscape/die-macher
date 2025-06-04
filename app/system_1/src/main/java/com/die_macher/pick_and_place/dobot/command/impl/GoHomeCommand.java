package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;

public class GoHomeCommand extends AbstractDobotCommand<Boolean> {
  private final boolean isQueued;

  public GoHomeCommand(boolean isQueued) {
    this.isQueued = isQueued;
  }

  @Override
  protected byte[] createMessage() {
    return DobotMessageFactory.createSetHomeCmdMessage(isQueued);
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
