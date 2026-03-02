package com.example.taskplatform.server.ws;

import com.example.taskplatform.common.AgentMessage;
import com.example.taskplatform.common.MessageType;
import com.example.taskplatform.common.TaskDefinition;
import com.example.taskplatform.server.service.RegistryService;
import com.example.taskplatform.server.service.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RegistryService registryService;
    private final TaskService taskService;
    private final Map<String, WebSocketSession> onlineClients = new ConcurrentHashMap<>();

    public AgentWebSocketHandler(ObjectMapper objectMapper, RegistryService registryService, TaskService taskService) {
        this.objectMapper = objectMapper;
        this.registryService = registryService;
        this.taskService = taskService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        AgentMessage incoming = objectMapper.readValue(message.getPayload(), AgentMessage.class);
        if (incoming.type() == MessageType.REGISTER) {
            String clientId = incoming.clientId();
            onlineClients.put(clientId, session);
            Map<String, String> meta = incoming.metadata() == null ? Map.of() : incoming.metadata();
            registryService.registerOrUpdate(clientId, meta.getOrDefault("host", "unknown"), meta.getOrDefault("os", "unknown"), meta.getOrDefault("version", "0.0.0"));
        } else if (incoming.type() == MessageType.TASK_PROGRESS || incoming.type() == MessageType.TASK_RESULT) {
            taskService.appendProgress(incoming.clientId(), incoming.taskId(), incoming.progress(), incoming.text());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        onlineClients.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    public boolean assignTask(String clientId, TaskDefinition taskDefinition) {
        WebSocketSession session = onlineClients.get(clientId);
        if (session == null || !session.isOpen()) {
            return false;
        }
        AgentMessage assign = new AgentMessage(MessageType.TASK_ASSIGN, clientId, taskDefinition.id(), taskDefinition.name(), 0,
                Map.of("linuxCommand", taskDefinition.linuxCommand(), "windowsCommand", taskDefinition.windowsCommand()));
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(assign)));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
