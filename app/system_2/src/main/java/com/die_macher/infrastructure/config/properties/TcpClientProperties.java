package com.die_macher.infrastructure.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.integration.tcp.client")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
public class TcpClientProperties {

    @NotNull private Integer port = 8025;

    @Min(0)
    private Integer backlog = 100;

    @NotNull private String host = "127.0.0.1";

    @Min(0)
    private Integer soTimeout = 30000;

    @Min(2048)
    @Max(Integer.MAX_VALUE)
    private Integer maxMessageSize = 1024 * 1024;

    @Min(1)
    @Max(4)
    private Integer headerSize = 2;
}
