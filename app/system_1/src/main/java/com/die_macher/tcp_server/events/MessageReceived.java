package com.die_macher.tcp_server.events;

import java.util.Map;

public record MessageReceived(Map<String, Object> message) { }
