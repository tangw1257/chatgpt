package com.example.taskplatform.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent")
public record ClientProperties(
        String id,
        String version,
        String serverHttpBase,
        String serverWsUrl,
        long updateCheckMs,
        int tcpPort
) {
}
