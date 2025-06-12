package com.example.atskiller.controller;

import com.example.atskiller.service.LinkedinScoreService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class LinkedinScoreController {

    private final LinkedinScoreService linkedinScoreService;

    @Autowired
    public LinkedinScoreController(LinkedinScoreService linkedinScoreService) {
        this.linkedinScoreService = linkedinScoreService;
    }

    @PostMapping("/analyze-linkedin-profile")
    public Mono<ResponseEntity<Map<String, String>>> analyzeLinkedinProfile(@RequestBody Map<String, String> request) {
        String linkedinUrl = request.get("linkedinUrl");

        return linkedinScoreService.analyzeProfile(linkedinUrl)
                .map(reviewHtml -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("reviewHtml", reviewHtml);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(e -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("reviewHtml", "<div style='color:red;'>Error analyzing profile.</div>");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
                });
    }
}
