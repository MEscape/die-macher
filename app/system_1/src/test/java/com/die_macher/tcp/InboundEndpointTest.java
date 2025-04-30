package com.die_macher.tcp;

import com.die_macher.common.util.HexDump;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;

import static org.mockito.Mockito.*;

class InboundEndpointTest {
    private static Message<byte[]> createMessage(byte[] payload) {
        return new GenericMessage<>(payload, new HashMap<>() {{
            put(IpHeaders.CONNECTION_ID, "CONNECTION_ID");
        }});
    }

    @Test
    void onMessage_withEmptyPayload_shouldLogAndCallHexDump() {
        byte[] payload = new byte[0];
        Message<byte[]> message = createMessage(payload);
        InboundEndpoint endpoint = new InboundEndpoint();

        try (MockedStatic<HexDump> hexDumpMock = mockStatic(HexDump.class)) {
            hexDumpMock.when(() -> HexDump.hexDump(payload)).thenReturn("<empty>");

            endpoint.onMessage(message);

            hexDumpMock.verify(() -> HexDump.hexDump(payload), times(1));
        }
    }

    @Test
    void onMessage_withPayload_shouldCallHexDump() {
        byte[] payload = new byte[]{0x01, 0x02, 0x03};
        Message<byte[]> message = createMessage(payload);
        InboundEndpoint endpoint = new InboundEndpoint();

        try (MockedStatic<HexDump> hexDumpMock = mockStatic(HexDump.class)) {
            hexDumpMock.when(() -> HexDump.hexDump(payload)).thenReturn("01 02 03");

            endpoint.onMessage(message);

            hexDumpMock.verify(() -> HexDump.hexDump(payload), times(1));
        }
    }
}