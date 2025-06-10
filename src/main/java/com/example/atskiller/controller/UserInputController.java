package com.example.atskiller.controller;

import com.example.atskiller.service.UserInputService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // Allow requests from React app
public class UserInputController {

    private final UserInputService userInputService;

    @Autowired
    public UserInputController(UserInputService userInputService) {
        this.userInputService = userInputService;
    }

    @PostMapping(value = "/scan", consumes = {"multipart/form-data"})
    public Mono<ResponseEntity<Map<String, Object>>> processUserInput(
            @RequestPart("resume") MultipartFile resumeFile,
            @RequestPart("jobDescription") String jobDescription) throws IOException {

        return userInputService.processUserInput(resumeFile, jobDescription)
                .timeout(Duration.ofSeconds(65)) // <-- Add timeout here!
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    Map<String, Object> errorBody = new HashMap<>();
                    String message = (e instanceof java.util.concurrent.TimeoutException)
                            ? "Request timed out (waited >65s for response)."
                            : (e.getMessage() != null ? e.getMessage() : "Unknown error");
                    errorBody.put("error", message);
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody));
                });
    }
}
