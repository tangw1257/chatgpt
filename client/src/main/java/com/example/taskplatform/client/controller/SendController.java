package com.example.taskplatform.client.controller;

import com.example.taskplatform.client.model.SendRequest;
import com.example.taskplatform.client.service.AgentNettyClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/send")
public class SendController {

    private final AgentNettyClientService nettyClientService;

    public SendController(AgentNettyClientService nettyClientService) {
        this.nettyClientService = nettyClientService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> send(@RequestBody SendRequest request) {
        try {
            nettyClientService.sendCustomProgress(request.taskId(), request.text(), request.progress());
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("status", "failed", "reason", e.getMessage()));
        }
    }
}
