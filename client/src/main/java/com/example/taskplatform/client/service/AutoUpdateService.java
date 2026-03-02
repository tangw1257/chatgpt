package com.example.taskplatform.client.service;

import com.example.taskplatform.client.config.ClientProperties;
import com.example.taskplatform.common.ClientUpdateInfo;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@EnableScheduling
public class AutoUpdateService {

    private final RestClient restClient;
    private final ClientProperties properties;

    public AutoUpdateService(RestClient.Builder builder, ClientProperties properties) {
        this.restClient = builder.baseUrl(properties.serverHttpBase()).build();
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${agent.update-check-ms:30000}")
    public void checkUpdate() {
        ClientUpdateInfo updateInfo = restClient.get().uri("/api/client/update/latest").retrieve().body(ClientUpdateInfo.class);
        if (updateInfo == null || updateInfo.latestVersion().equals(properties.version())) {
            return;
        }
        byte[] file = restClient.get().uri(updateInfo.downloadUrl()).retrieve().body(byte[].class);
        if (file == null) {
            return;
        }
        try {
            Path out = Path.of("updates", "client-" + updateInfo.latestVersion() + ".jar");
            Files.createDirectories(out.getParent());
            Files.write(out, file);
        } catch (IOException ignored) {
        }
    }
}
