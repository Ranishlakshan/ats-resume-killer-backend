package com.example.atskiller.controller;

import com.example.atskiller.service.CoverLetterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:5173") // Allow requests from React app
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @Autowired
    public CoverLetterController(CoverLetterService coverLetterService) {
        this.coverLetterService = coverLetterService;
    }

    @PostMapping(value = "/generate-cover-letter", consumes = {"multipart/form-data"})
    public Mono<ResponseEntity<Map<String, String>>> generateCoverLetter(
            @RequestPart("resume") MultipartFile resumeFile,
            @RequestPart("jobDescription") String jobDescription) {

        return coverLetterService.generateCoverLetter(resumeFile, jobDescription)
                .map(coverLetter -> {
                    Map<String, String> result = new HashMap<>();
                    result.put("coverLetter", coverLetter);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(e -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("coverLetter", "Error generating cover letter: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
                });
    }
}
