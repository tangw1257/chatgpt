package com.example.taskplatform.server.service;

import com.example.taskplatform.common.TaskDefinition;
import com.example.taskplatform.server.model.TaskProgressLog;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {

    private final Map<String, TaskDefinition> tasks = new ConcurrentHashMap<>();
    private final List<TaskProgressLog> progressLogs = new ArrayList<>();

    public TaskService() {
        TaskDefinition demoTask = new TaskDefinition(
                UUID.randomUUID().toString(),
                "Print Hello World",
                "echo hello world from linux",
                "cmd /c echo hello world from windows"
        );
        tasks.put(demoTask.id(), demoTask);
    }

    public List<TaskDefinition> listTasks() {
        return new ArrayList<>(tasks.values());
    }

    public TaskDefinition saveTask(TaskDefinition task) {
        String taskId = task.id() == null || task.id().isBlank() ? UUID.randomUUID().toString() : task.id();
        TaskDefinition stored = new TaskDefinition(taskId, task.name(), task.linuxCommand(), task.windowsCommand());
        tasks.put(taskId, stored);
        return stored;
    }

    public TaskDefinition getTask(String taskId) {
        return tasks.get(taskId);
    }

    public synchronized void appendProgress(String clientId, String taskId, Integer progress, String text) {
        progressLogs.add(new TaskProgressLog(clientId, taskId, progress, text, Instant.now()));
    }

    public synchronized List<TaskProgressLog> listProgressLogs() {
        return new ArrayList<>(progressLogs);
    }
}
