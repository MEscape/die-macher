package com.die_macher.infrastructure.adapter.serializer;

import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;

public class CustomHeaderSerializer extends ByteArrayLengthHeaderSerializer {
  private static final int FLOW_TYPE_SIZE = 1;

  public CustomHeaderSerializer(int headerSize, int maxMessageSize) {
    super(headerSize - FLOW_TYPE_SIZE);

    this.setMaxMessageSize(maxMessageSize);
    this.setInclusive(false);
  }
}
