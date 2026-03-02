package com.example.taskplatform.common;

public record TaskDefinition(
        String id,
        String name,
        String linuxCommand,
        String windowsCommand
) {
}
