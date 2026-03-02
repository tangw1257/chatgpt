package com.example.taskplatform.client.service;

import com.example.taskplatform.client.config.ClientProperties;
import com.example.taskplatform.client.ws.AgentClientWebSocketHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

@Service
@EnableScheduling
public class AgentConnectionService {

    private final AgentClientWebSocketHandler handler;
    private final ClientProperties properties;

    public AgentConnectionService(AgentClientWebSocketHandler handler, ClientProperties properties) {
        this.handler = handler;
        this.properties = properties;
    }

    @PostConstruct
    public void connect() {
        doConnect();
    }

    @Scheduled(fixedDelay = 5000)
    public void keepAlive() {
        if (!handler.isConnected()) {
            doConnect();
        }
    }

    private void doConnect() {
        try {
            new StandardWebSocketClient().execute(handler, properties.serverWsUrl());
        } catch (Exception ignored) {
        }
    }
}
