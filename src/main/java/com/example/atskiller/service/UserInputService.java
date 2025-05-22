package com.example.atskiller.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInputService {

    private final KeywordExtractionService keywordExtractionService;

    public UserInputService(KeywordExtractionService keywordExtractionService) {
        this.keywordExtractionService = keywordExtractionService;
    }

    public Mono<Map<String, Object>> processUserInput(MultipartFile resumeFile, String jobDescription) throws IOException {
        String extractedText = extractPdfText(resumeFile);
        System.out.println("Extracted Resume Text: " + extractedText);
        System.out.println("Received Job Description: " + jobDescription);

        return keywordExtractionService.getKeywordsFromJobDescription(jobDescription, extractedText)
                .map(keywordsJson -> {
                    Map<String, Object> keywordsMap = jsonToMap(keywordsJson);

                    Map<String, Object> response = new HashMap<>();
                    response.put("extractedResumeText", extractedText);
                    response.put("scannedJobDescription", jobDescription);
                    response.put("keywordsjd", keywordsMap.getOrDefault("keywordsjd", ""));
                    response.put("missingKeywords", keywordsMap.getOrDefault("missingKeywords", ""));
                    response.put("name", keywordsMap.getOrDefault("name", ""));
                    response.put("structure", keywordsMap.getOrDefault("structure", ""));
                    response.put("overallScore", keywordsMap.getOrDefault("overallScore", ""));
                    response.put("wordcount", keywordsMap.getOrDefault("wordcount", 0));
                    response.put("address", keywordsMap.getOrDefault("address", ""));
                    response.put("noofHardskillsre", keywordsMap.getOrDefault("noofHardskillsre", 0));
                    response.put("softskillsjd", keywordsMap.getOrDefault("softskillsjd", ""));
                    response.put("linkedin", keywordsMap.getOrDefault("linkedin", ""));
                    response.put("matchingjdre", keywordsMap.getOrDefault("matchingjdre", ""));
                    response.put("noofHardskillsjd", keywordsMap.getOrDefault("noofHardskillsjd", 0));
                    response.put("softskillsre", keywordsMap.getOrDefault("softskillsre", ""));
                    response.put("hardskillsjd", keywordsMap.getOrDefault("hardskillsjd", ""));
                    response.put("noofSoftskillsjd", keywordsMap.getOrDefault("noofSoftskillsjd", 0));
                    response.put("phone", keywordsMap.getOrDefault("phone", ""));
                    response.put("hardskillsre", keywordsMap.getOrDefault("hardskillsre", ""));
                    response.put("keywordsre", keywordsMap.getOrDefault("keywordsre", ""));
                    response.put("noofSoftskillsre", keywordsMap.getOrDefault("noofSoftskillsre", 0));
                    response.put("email", keywordsMap.getOrDefault("email", ""));

                    return response;
                });
    }

    private String extractPdfText(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new IOException("Error reading PDF file", e);
        }
    }

    private Map<String, Object> jsonToMap(JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof org.json.JSONArray) {
                value = ((org.json.JSONArray) value).toList();
            }
            map.put(key, value);
        }
        return map;
    }
}