package com.die_macher.tcp_server.handler;

import com.die_macher.tcp_server.events.MessageReceived;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import java.util.Map;

@MessageEndpoint
public class ServerInboundEndpoint {

    private final ApplicationEventPublisher events;
    
    public ServerInboundEndpoint(ApplicationEventPublisher events) {
        this.events = events;
    }
    
    @ServiceActivator(inputChannel = "serverChannel", requiresReply = "true")
    public byte[] handleMessage(Message<Map<String, Object>> message) {

        Map<String, Object> payload = message.getPayload();
        events.publishEvent(new MessageReceived(payload));
        return new byte[0];
        }
}