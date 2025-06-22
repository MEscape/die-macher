package com.die_macher.infrastructure.adapter.serializer;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

public class CustomHeaderDeserializer extends ByteArrayLengthHeaderSerializer {

  private static final int FLOW_TYPE_SIZE = 1;

  private final ThreadLocal<Byte> currentFlowType = new ThreadLocal<>();

  public CustomHeaderDeserializer(int headerSize, int maxMessageSize) {
    super(headerSize - FLOW_TYPE_SIZE);

    this.setMaxMessageSize(maxMessageSize);
    this.setInclusive(false);
  }

  @Override
  protected int readHeader(InputStream stream) throws IOException {
    byte[] headerPart = new byte[FLOW_TYPE_SIZE];

    try {
      int status = read(stream, headerPart, true);
      if (status < 0) {
        throw new SoftEndOfStreamException("Stream closed between packet size");
      }

      byte flowType = headerPart[0];
      currentFlowType.set(flowType);

      return super.readHeader(stream);
    } catch (SoftEndOfStreamException ex) {
      throw ex;
    } catch (IOException | RuntimeException ex) {
      publishEvent(ex, headerPart, -1);
      throw ex;
    }
  }

  public byte getCurrentFlowType() {
    Byte ft = currentFlowType.get();
    if (ft == null) {
      throw new IllegalStateException("Flow type not yet read");
    }
    return ft;
  }

  public void clearCurrentFlowType() {
    currentFlowType.remove();
  }
}
