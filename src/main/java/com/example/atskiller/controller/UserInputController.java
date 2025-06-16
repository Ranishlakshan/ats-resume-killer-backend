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
//@CrossOrigin(origins = "http://localhost:5173") // Optional if CORS config is global
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
                .timeout(Duration.ofSeconds(65)) // How long to wait for async processing
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    Map<String, Object> errorBody = new HashMap<>();

                    String message;
                    if (e instanceof java.util.concurrent.TimeoutException) {
                        message = "Request timed out (waited >65s for response).";
                    } else if (e != null && e.getMessage() != null) {
                        message = e.getMessage();
                    } else {
                        message = "Unexpected server error.";
                    }

                    errorBody.put("error", message);

                    // Print full stack trace only if exception exists
                    if (e != null) {
                        System.err.println("Exception occurred during async processing:");
                        e.printStackTrace();
                    }

                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody));
                });
    }
}
