package com.die_macher.tcp_server.serializer;

import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.MapJsonSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;


public class JsonSerializer extends MapJsonSerializer {

	public JsonSerializer(int maxMessageSize) {
		ByteArrayLengthHeaderSerializer packetSerializer = new ByteArrayLengthHeaderSerializer(4);
		packetSerializer.setMaxMessageSize(maxMessageSize);
		setPacketSerializer(packetSerializer);
		setPacketDeserializer(packetSerializer);
	}

	@Override
	public void serialize(Map<?, ?> object, OutputStream outputStream) throws IOException {

		if (object.isEmpty()) {
			outputStream.flush();
			return;
		}
		super.serialize(object, outputStream);

	}
}
