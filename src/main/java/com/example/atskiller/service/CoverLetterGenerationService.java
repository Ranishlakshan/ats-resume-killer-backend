package com.example.atskiller.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class CoverLetterGenerationService {

    private final WebClient webClient;

    public CoverLetterGenerationService(
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url}") String openAiApiUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(openAiApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<JSONObject> generateCoverLetterFromOpenAI(String jobDescription, String resumeText) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4-turbo");
            requestBody.put("max_tokens", 1500);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content",
                    "You are a professional cover letter writer. Based on the following resume and job description, write a complete, tailored cover letter suitable for applying to this job. " +
                            "Make it professional, concise, and relevant. Return the response as a JSON object with a single key 'coverLetter' containing the letter text as a string. " +
                            "If the result cannot be generated, return {\"coverLetter\": \"Error: Could not generate cover letter.\"}\n\n" +
                            "Job Description: " + jobDescription + "\n\nResume: " + resumeText);

            messages.put(userMessage);
            requestBody.put("messages", messages);

            return webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(this::parseResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just(new JSONObject().put("coverLetter", "Error: Exception occurred generating cover letter."));
        }
    }

    private JSONObject parseResponse(Map<String, Object> response) {
        System.out.println("Raw OpenAI Cover Letter response: " + response);

        if (response != null && response.containsKey("choices")) {
            Map<String, Object> choice = (Map<String, Object>) ((java.util.List<?>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");

            if (message.containsKey("content")) {
                String jsonResponse = (String) message.get("content");
                // Clean up for valid JSON if needed
                jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
                try {
                    return new JSONObject(jsonResponse);
                } catch (Exception e) {
                    System.err.println("Invalid JSON response: " + jsonResponse);
                    return new JSONObject().put("coverLetter", "Error: Invalid JSON format received from OpenAI.");
                }
            }
        }
        return new JSONObject().put("coverLetter", "Error: No valid response from OpenAI.");
    }
}
