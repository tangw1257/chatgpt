package com.example.taskplatform.server.controller;

import com.example.taskplatform.common.ClientUpdateInfo;
import com.example.taskplatform.common.TaskDefinition;
import com.example.taskplatform.server.service.RegistryService;
import com.example.taskplatform.server.service.TaskService;
import com.example.taskplatform.server.service.UpdateService;
import com.example.taskplatform.server.ws.AgentWebSocketHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConsoleController {

    private final RegistryService registryService;
    private final TaskService taskService;
    private final AgentWebSocketHandler wsHandler;
    private final UpdateService updateService;

    public ConsoleController(RegistryService registryService, TaskService taskService, AgentWebSocketHandler wsHandler, UpdateService updateService) {
        this.registryService = registryService;
        this.taskService = taskService;
        this.wsHandler = wsHandler;
        this.updateService = updateService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("clients", registryService.listClients());
        model.addAttribute("tasks", taskService.listTasks());
        model.addAttribute("logs", taskService.listProgressLogs());
        model.addAttribute("update", updateService.getLatest());
        return "index";
    }

    @PostMapping("/tasks")
    public String saveTask(@ModelAttribute TaskDefinition taskDefinition) {
        taskService.saveTask(taskDefinition);
        return "redirect:/";
    }

    @PostMapping("/tasks/{taskId}/assign/{clientId}")
    public String assignTask(@PathVariable String taskId, @PathVariable String clientId) {
        TaskDefinition task = taskService.getTask(taskId);
        if (task != null) {
            wsHandler.assignTask(clientId, task);
        }
        return "redirect:/";
    }

    @ResponseBody
    @RequestMapping("/api/client/update/latest")
    public ClientUpdateInfo latestUpdate() {
        return updateService.getLatest();
    }

    @ResponseBody
    @PostMapping("/api/client/update/publish")
    public String publishUpdate(@RequestBody ClientUpdateInfo request) {
        updateService.publish(request);
        return "ok";
    }
}
