package com.example.taskplatform.server.service;

import com.example.taskplatform.common.ClientUpdateInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UpdateService {

    private volatile ClientUpdateInfo current = new ClientUpdateInfo("1.0.0", "/downloads/client-1.0.0.jar", "demo-checksum", "init");

    @Value("${server.public-base-url:http://localhost:8080}")
    private String serverBaseUrl;

    public ClientUpdateInfo getLatest() {
        return new ClientUpdateInfo(
                current.latestVersion(),
                serverBaseUrl + current.downloadUrl(),
                current.checksum(),
                current.notes()
        );
    }

    public void publish(ClientUpdateInfo updateInfo) {
        this.current = updateInfo;
    }
}
