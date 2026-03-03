package com.example.taskplatform.client.service;

import com.example.taskplatform.client.config.ClientProperties;
import com.example.taskplatform.client.model.SendRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TcpSendServer {

    private static final Logger log = LoggerFactory.getLogger(TcpSendServer.class);

    private final ClientProperties properties;
    private final ObjectMapper objectMapper;
    private final AgentNettyClientService nettyClientService;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;

    public TcpSendServer(ClientProperties properties, ObjectMapper objectMapper, AgentNettyClientService nettyClientService) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.nettyClientService = nettyClientService;
    }

    @PostConstruct
    public void start() {
        executor.submit(() -> {
            try (ServerSocket ss = new ServerSocket(properties.tcpPort())) {
                this.serverSocket = ss;
                log.info("tcp send server started on {}", properties.tcpPort());
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = ss.accept();
                    executor.submit(() -> handleSocket(socket));
                }
            } catch (Exception e) {
                log.warn("tcp send server stopped", e);
            }
        });
    }

    private void handleSocket(Socket socket) {
        try (socket; BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                return;
            }
            SendRequest request = objectMapper.readValue(line, SendRequest.class);
            nettyClientService.sendCustomProgress(request.taskId(), request.text(), request.progress());
        } catch (Exception e) {
            log.warn("handle tcp send request failed", e);
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception ignored) {
        }
        executor.shutdownNow();
    }
}
