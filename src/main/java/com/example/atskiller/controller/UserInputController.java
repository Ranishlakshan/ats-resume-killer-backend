package com.example.atskiller.controller;

import com.example.atskiller.service.UserInputService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.IOException;
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

        // Call the service method that returns Mono<Map<String, Object>>
        return userInputService.processUserInput(resumeFile, jobDescription)
                .map(ResponseEntity::ok) // Wrap in ResponseEntity with status 200 OK
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null))); // Handle errors gracefully
    }
}
