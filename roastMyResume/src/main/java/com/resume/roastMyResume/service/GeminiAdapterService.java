package com.resume.roastMyResume.service;
import com.resume.roastMyResume.config.GeminiConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
public class GeminiAdapterService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final RateLimiterService rateLimiterService;

    public GeminiAdapterService(GeminiConfig geminiConfig, RateLimiterService rateLimiterService) {
        this.geminiConfig = geminiConfig;
        this.restTemplate = new RestTemplate();
        this.rateLimiterService = rateLimiterService;
    }

    public String callGeminiApi(String resumeText, String clientIp) {
        if (rateLimiterService.isRateLimited(clientIp)) {
            return "Rate limit exceeded! Please try again later.";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Roast my resume in short:\n\n" + resumeText;

            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String urlWithKey = geminiConfig.getBaseUrl() + "?key=" + geminiConfig.getApiKey();

            ResponseEntity<Map> response = restTemplate.postForEntity(urlWithKey, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> firstCandidate = candidates.get(0);
                        Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                        List<Map<String, String>> parts = (List<Map<String, String>>) contentMap.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return parts.get(0).get("text");
                        }
                    }
                }
                return "Gemini responded but no content was found!";
            } else {
                return fallbackResponse();
            }
        } catch (RestClientException ex) {
            ex.printStackTrace();
            return fallbackResponse();
        }
    }

    private String fallbackResponse() {
        return "Fallback: Your resume is so fire, even Gemini couldn't handle roasting it!";
    }
}
