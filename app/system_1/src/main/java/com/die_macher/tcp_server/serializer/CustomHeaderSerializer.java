package com.die_macher.tcp_server.serializer;

import com.die_macher.tcp_server.context.FlowTypeContextHolder;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;

import java.io.IOException;
import java.io.OutputStream;

public class CustomHeaderSerializer extends ByteArrayLengthHeaderSerializer {

	private static final int FLOW_TYPE_SIZE = 1;

	public CustomHeaderSerializer(int headerSize, int maxMessageSize) {
		super(headerSize - FLOW_TYPE_SIZE);
		this.setMaxMessageSize(maxMessageSize);
		this.setInclusive(false);
	}

	@Override
	protected void writeHeader(OutputStream stream, int messageLength) throws IOException {
		try {
			Byte flowType = FlowTypeContextHolder.get();

			if (flowType == null) {
				flowType = (byte) 'D';
			}

			byte[] flowTypeBytes = new byte[FLOW_TYPE_SIZE];
			flowTypeBytes[0] = flowType;
			stream.write(flowTypeBytes);

			super.writeHeader(stream, messageLength);

		} catch (IOException | RuntimeException ex) {
			publishEvent(ex, new byte[0], messageLength);
			throw ex;
		}
	}
}