package com.example.taskplatform.server.model;

import java.time.Instant;

public record TaskProgressLog(String clientId, String taskId, Integer progress, String message, Instant time) {
}
