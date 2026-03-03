package com.example.taskplatform.client.service;

import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class AgentConnectionService {

    private final AgentNettyClientService nettyClientService;

    public AgentConnectionService(AgentNettyClientService nettyClientService) {
        this.nettyClientService = nettyClientService;
    }

    @PostConstruct
    public void connect() {
        nettyClientService.connect();
    }

    @Scheduled(fixedDelay = 5000)
    public void keepAlive() {
        if (!nettyClientService.isConnected()) {
            nettyClientService.connect();
        }
    }
}
