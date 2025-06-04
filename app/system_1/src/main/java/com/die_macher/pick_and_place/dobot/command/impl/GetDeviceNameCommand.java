package com.die_macher.pick_and_place.dobot.command.impl;

import com.die_macher.pick_and_place.dobot.command.AbstractDobotCommand;
import com.die_macher.pick_and_place.dobot.protocol.DobotMessageFactory;
import com.die_macher.pick_and_place.dobot.protocol.DobotProtocol;
import java.nio.charset.StandardCharsets;

public class GetDeviceNameCommand extends AbstractDobotCommand<String> {
  @Override
  protected byte[] createMessage() {
    return DobotMessageFactory.createGetDeviceNameMessage();
  }

  @Override
  protected DobotProtocol.Commands getCommandType() {
    return DobotProtocol.Commands.GET_DEVICE_NAME;
  }

  @Override
  protected String parseResponse(byte[] response) {
    byte[] payload = DobotProtocol.extractResponsePayload(response);
    return payload != null ? new String(payload, StandardCharsets.UTF_8) : null;
  }
}
