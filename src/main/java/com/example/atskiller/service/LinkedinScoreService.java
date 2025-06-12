package com.example.atskiller.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class LinkedinScoreService {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = ""; // <- Replace with your API key!

    private final WebClient webClient = WebClient.builder()
            .baseUrl(OPENAI_API_URL)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + OPENAI_API_KEY)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public Mono<String> analyzeProfile(String linkedinUrl) {
        return generateLinkedinScoreFromOpenAI(linkedinUrl)
                .map(jsonObject -> jsonObject.optString("reviewHtml",
                        "<div style='color:red;background:#fff;padding:16px;'>Error: No reviewHtml key in OpenAI response.</div>"));
    }

    public Mono<JSONObject> generateLinkedinScoreFromOpenAI(String linkedinUrl) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4-turbo");
            requestBody.put("max_tokens", 1500);

            String prompt =
                    "User is already gave permission to access linkedin profile.You are a professional LinkedIn profile reviewer and career coach.\n\n" +
                            "Your task is to analyze the following LinkedIn profile (provided as a public URL) and generate a detailed, visual, and actionable review for a job seeker who wants to improve their profile for tech roles. " +
                            "Score the profile out of 100 based on headline, summary, experience, skills, projects, recommendations, profile picture, and engagement.\n\n" +
                            "Strict Requirements:\n" +
                            "- Return your answer as a JSON object with a single key 'reviewHtml'. The value must be a string of HTML with inline CSS (NO external or embedded CSS).\n" +
                            "- The review must have a white background, and use only these colors: black, ash green (#28b07b), and red for all text and icons (use Unicode or emoji, not images).\n" +
                            "- Display the overall score visually with a large number and color indicator (ash green for good, red for problems).\n" +
                            "- Give clear, actionable feedback as bullet points: use ash green icons (✔ or 🟢) for strengths and red icons (❌ or 🔴) for weaknesses.\n" +
                            "- Include a highlighted “Next Steps” box at the bottom, bordered and text in ash green.\n" +
                            "- Do NOT use images, scripts, or external resources.\n" +
                            "- If the profile cannot be analyzed, return an HTML message in this style saying so.\n\n" +
                            "LinkedIn Profile URL: " + linkedinUrl;

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

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
            return Mono.just(new JSONObject().put("reviewHtml", "<div style='color:red;background:#fff;padding:16px;'>Error: Exception occurred generating profile review.</div>"));
        }
    }

    private JSONObject parseResponse(Map<String, Object> response) {
        System.out.println("Raw OpenAI LinkedIn Review response: " + response);

        if (response != null && response.containsKey("choices")) {
            Map<String, Object> choice = (Map<String, Object>) ((java.util.List<?>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message.containsKey("content")) {
                String jsonResponse = (String) message.get("content");

                // Remove markdown and trailing slashes
                jsonResponse = jsonResponse.replaceAll("```json", "")
                        .replaceAll("```", "")
                        .replaceAll("\\\\\n", "") // Remove backslash followed by newline
                        .replaceAll("\\\\", "")   // Remove any remaining backslashes
                        .trim();

                try {
                    return new JSONObject(jsonResponse);
                } catch (Exception e) {
                    System.err.println("Invalid JSON response: " + jsonResponse);
                    return new JSONObject().put("reviewHtml", "<div style='color:red;background:#fff;padding:16px;'>Error: Invalid JSON format received from OpenAI.</div>");
                }
            }
        }
        return new JSONObject().put("reviewHtml", "<div style='color:red;background:#fff;padding:16px;'>Error: No valid response from OpenAI.</div>");
    }

}
