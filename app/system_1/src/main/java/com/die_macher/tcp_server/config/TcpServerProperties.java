package com.die_macher.tcp_server.config;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "tcp.server")
@Data
public class TcpServerProperties {
    @Min(1)
    @Max(65535)
    private int port;

    private String host;

    @Min(1)
    @Max(10)
    private int headerSize;

    @Min(1024)
    @Max(Integer.MAX_VALUE)
    private int maxMessageSize;
}