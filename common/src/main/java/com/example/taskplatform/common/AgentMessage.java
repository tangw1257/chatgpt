package com.example.taskplatform.common;

import java.util.Map;

public record AgentMessage(
        MessageType type,
        String clientId,
        String taskId,
        String text,
        Integer progress,
        Map<String, String> metadata
) {
}
