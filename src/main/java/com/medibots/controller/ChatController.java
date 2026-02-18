package com.medibots.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("message", "Chat is not available in this version. Use the dashboard for claims and analytics."));
    }
}
