package com.example.atskiller.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;
import java.util.Map;

import io.netty.channel.ChannelOption;

@Service
public class KeywordExtractionService {

    private final WebClient webClient;

    public KeywordExtractionService(
            @Value("${openai.api.key}") String openAiApiKey,
            @Value("${openai.api.url}") String openAiApiUrl
    ) {
        // Step 1: Setup TCP connection timeout
        TcpClient tcpClient = TcpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); // 10s connect timeout

        // Step 2: Wrap into HttpClient and set response timeout
        HttpClient httpClient = HttpClient.from(tcpClient)
                .responseTimeout(Duration.ofSeconds(65)); // Wait up to 65s for full response

        // Step 3: Build WebClient
        this.webClient = WebClient.builder()
                .baseUrl(openAiApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public Mono<JSONObject> getKeywordsFromJobDescription(String jobDescription, String extractedText) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4-turbo");
            requestBody.put("max_tokens", 4000);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content",
                    "Extract folllowing keywords and phrases from this job description and resume as much as you can but meaningful." +
                            "name with key name,if no name then 'found nothing' ," +
                            "structure of the resume. use keyword 'structure' : one of words(good,bad,normal)," +
                            "" +
                            "Only job related words. Use key 'keywordsjd'." +
                            "get all the keyword list(soft skills ,hard skills and job specific key words" +
                            " that have in job description but not in resume.Use key missingKeywords)\n" +
                            "Extract soft skills with key 'softskillsjd'.\n" +
                            "Extract hard skills with key 'hardskillsjd'.\n" +
                            "Total soft skills count: 'noofSoftskillsjd'.\n" +
                            "Total hard skills count: 'noofHardskillsjd'.\n" +
                            "Now extract relevant keywords from this extractedText (Resume). " +
                            "Use key 'keywordsre'.\n" +
                            "Extract matching soft skills from Resume to job description: 'softskillsre'.\n" +
                            "Extract matching hard skills from Resume to job description: 'hardskillsre'.\n" +
                            "Total matching soft skills count from Resume to job description: 'noofSoftskillsre'.\n" +
                            "Total matching hard skills count from Resume to job description: 'noofHardskillsre'.\n" +
                            "Find all the matching job specific words from both job description & resume (all skills" +
                            ",soft skills,hard skills,keywords).dont add if not available that word in job description or resume: 'matchingjdre'.\n" +
                            "Extract address if possible, else return 'found nothing': 'address'.\n" +
                            "Extract email: 'email' if no data then 'found nothing'.\n" +
                            "Extract LinkedIn URL: 'linkedin' if no data then 'found nothing'.\n" +
                            "Extract phone number: 'phone' if no data then 'found nothing'.\n" +
                            "Extract word count from Resume: 'wordcount'" +
                            "" +
                            "match and give overall keyword matching score .use key overallScore\n" +
                            "\n" +
                            "score should be out of 100%\n" +
                            "5% for structure of the resume\n" +
                            "50% for keyword matching. look strictly\n" +
                            "25% experience matching. look strictly\n" +
                            "5% education matching\n" +
                            "10% for better to have qualifications.look strictly " +
                            ".\n" +
                            "Job Description: " + jobDescription + "\n" +
                            "Resume: " + extractedText);

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
            return Mono.just(new JSONObject().put("error", "Error extracting keywords"));
        }
    }

    private JSONObject parseResponse(Map<String, Object> response) {
        System.out.println("Raw OpenAI response: " + response);

        if (response != null && response.containsKey("choices")) {
            Map<String, Object> choice = (Map<String, Object>) ((java.util.List<?>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");

            if (message.containsKey("content")) {
                String jsonResponse = (String) message.get("content");

                // Ensure the response is valid JSON (remove backticks if needed)
                jsonResponse = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();

                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    // Ensure `hardskillsjd` is an array, convert if necessary
                    if (jsonObject.has("hardskillsjd")) {
                        Object hardskillsjdObj = jsonObject.get("hardskillsjd");

                        if (hardskillsjdObj instanceof String) {
                            try {
                                JSONArray hardskillsArray = new JSONArray((String) hardskillsjdObj);
                                jsonObject.put("hardskillsjd", hardskillsArray);
                            } catch (Exception e) {
                                System.err.println("Invalid hardskillsjd format: " + hardskillsjdObj);
                                jsonObject.put("hardskillsjd", new JSONArray());
                            }
                        } else if (!(hardskillsjdObj instanceof JSONArray)) {
                            jsonObject.put("hardskillsjd", new JSONArray());
                        }
                    }

                    return jsonObject;
                } catch (Exception e) {
                    System.err.println("Invalid JSON response: " + jsonResponse);
                    return new JSONObject().put("error", "Invalid JSON format received");
                }
            }
        }
        return new JSONObject().put("error", "No valid JSON response found");
    }
}
