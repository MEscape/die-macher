package com.die_macher.tcp;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "tcp")
@Data
public class TcpProperties {
    @Min(1)
    @Max(65535)
    private int port;

    // Only allow "localhost" or valid IPv4 addresses
    @Pattern(regexp = "^(localhost|(?:\\d{1,3}\\.){3}\\d{1,3})$",
            message = "Invalid host. Must be 'localhost' or a valid IPv4 address.")
    private String host;

    @Min(1)
    @Max(4)
    private int headerSize;

    @Min(1024)
    @Max(Integer.MAX_VALUE)
    private int maxMessageSize;
}
