package com.example.taskplatform.client.ws;

import com.example.taskplatform.client.config.ClientProperties;
import com.example.taskplatform.client.service.CommandExecutorService;
import com.example.taskplatform.common.AgentMessage;
import com.example.taskplatform.common.MessageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class AgentClientWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final CommandExecutorService executorService;
    private final ClientProperties properties;
    private WebSocketSession session;

    public AgentClientWebSocketHandler(ObjectMapper objectMapper, CommandExecutorService executorService, ClientProperties properties) {
        this.objectMapper = objectMapper;
        this.executorService = executorService;
        this.properties = properties;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        AgentMessage register = new AgentMessage(MessageType.REGISTER, properties.id(), null, "register", 0,
                Map.of("host", java.net.InetAddress.getLocalHost().getHostName(), "os", System.getProperty("os.name"), "version", properties.version()));
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(register)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        AgentMessage incoming = objectMapper.readValue(message.getPayload(), AgentMessage.class);
        if (incoming.type() == MessageType.TASK_ASSIGN) {
            String os = System.getProperty("os.name").toLowerCase();
            String command = os.contains("win") ? incoming.metadata().get("windowsCommand") : incoming.metadata().get("linuxCommand");
            sendProgress(incoming.taskId(), 20, "received command: " + command);
            String result = executorService.execute(command);
            sendProgress(incoming.taskId(), 80, "command executed");
            AgentMessage resultMsg = new AgentMessage(MessageType.TASK_RESULT, properties.id(), incoming.taskId(), result, 100, Map.of());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resultMsg)));
        }
    }

    private void sendProgress(String taskId, int progress, String message) throws Exception {
        AgentMessage progressMsg = new AgentMessage(MessageType.TASK_PROGRESS, properties.id(), taskId, message, progress, Map.of());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(progressMsg)));
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }
}
