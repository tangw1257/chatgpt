package com.example.taskplatform.common;

public record ClientUpdateInfo(
        String latestVersion,
        String downloadUrl,
        String checksum,
        String notes
) {
}
