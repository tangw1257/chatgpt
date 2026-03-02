package com.example.taskplatform.server.service;

import com.example.taskplatform.server.model.ClientInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistryService {

    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public void registerOrUpdate(String clientId, String host, String os, String version) {
        clients.compute(clientId, (id, existing) -> {
            if (existing == null) {
                return new ClientInfo(clientId, host, os, version);
            }
            existing.setHostName(host);
            existing.setOsName(os);
            existing.setCurrentVersion(version);
            existing.touch();
            return existing;
        });
    }

    public List<ClientInfo> listClients() {
        return new ArrayList<>(clients.values());
    }
}
