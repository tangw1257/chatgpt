package com.example.taskplatform.client.model;

public record SendRequest(
        String taskId,
        String text,
        Integer progress
) {
}
