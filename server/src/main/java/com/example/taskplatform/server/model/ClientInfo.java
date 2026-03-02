package com.example.taskplatform.server.model;

import java.time.Instant;

public class ClientInfo {
    private final String clientId;
    private String hostName;
    private String osName;
    private String currentVersion;
    private Instant lastSeen;

    public ClientInfo(String clientId, String hostName, String osName, String currentVersion) {
        this.clientId = clientId;
        this.hostName = hostName;
        this.osName = osName;
        this.currentVersion = currentVersion;
        this.lastSeen = Instant.now();
    }

    public String getClientId() { return clientId; }
    public String getHostName() { return hostName; }
    public String getOsName() { return osName; }
    public String getCurrentVersion() { return currentVersion; }
    public Instant getLastSeen() { return lastSeen; }

    public void setHostName(String hostName) { this.hostName = hostName; }
    public void setOsName(String osName) { this.osName = osName; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
    public void touch() { this.lastSeen = Instant.now(); }
}
