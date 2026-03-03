package com.example.taskplatform.server.ws;

import com.example.taskplatform.common.AgentMessage;
import com.example.taskplatform.common.MessageType;
import com.example.taskplatform.common.TaskDefinition;
import com.example.taskplatform.server.service.RegistryService;
import com.example.taskplatform.server.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentMessageRouter {

    private final ObjectMapper objectMapper;
    private final RegistryService registryService;
    private final TaskService taskService;
    private final Map<String, Channel> onlineClients = new ConcurrentHashMap<>();

    public AgentMessageRouter(ObjectMapper objectMapper, RegistryService registryService, TaskService taskService) {
        this.objectMapper = objectMapper;
        this.registryService = registryService;
        this.taskService = taskService;
    }

    public void handleInbound(Channel channel, String payload) throws Exception {
        AgentMessage incoming = objectMapper.readValue(payload, AgentMessage.class);
        if (incoming.type() == MessageType.REGISTER) {
            String clientId = incoming.clientId();
            onlineClients.put(clientId, channel);
            Map<String, String> meta = incoming.metadata() == null ? Map.of() : incoming.metadata();
            registryService.registerOrUpdate(clientId,
                    meta.getOrDefault("host", "unknown"),
                    meta.getOrDefault("os", "unknown"),
                    meta.getOrDefault("version", "0.0.0"));
            return;
        }

        if (incoming.type() == MessageType.TASK_PROGRESS || incoming.type() == MessageType.TASK_RESULT) {
            taskService.appendProgress(incoming.clientId(), incoming.taskId(), incoming.progress(), incoming.text());
        }
    }

    public void removeChannel(Channel channel) {
        onlineClients.entrySet().removeIf(entry -> entry.getValue().id().equals(channel.id()));
    }

    public boolean assignTask(String clientId, TaskDefinition taskDefinition) {
        Channel channel = onlineClients.get(clientId);
        if (channel == null || !channel.isActive()) {
            return false;
        }
        AgentMessage assign = new AgentMessage(
                MessageType.TASK_ASSIGN,
                clientId,
                taskDefinition.id(),
                taskDefinition.name(),
                0,
                Map.of("linuxCommand", taskDefinition.linuxCommand(), "windowsCommand", taskDefinition.windowsCommand()));
        try {
            String json = objectMapper.writeValueAsString(assign);
            channel.writeAndFlush(new io.netty.handler.codec.http.websocketx.TextWebSocketFrame(json));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
