package com.resume.roastMyResume.controller;
import com.resume.roastMyResume.dto.TextRequest;
import com.resume.roastMyResume.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/gemini")
public class ResumeController {

    private final GeminiService geminiService;

    public ResumeController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<String> callGeminiApi(
            @RequestHeader(value = "ip-address") String ipAddress,
            @Valid @RequestBody TextRequest textRequest) {


        String result = geminiService.processRequest(ipAddress, textRequest.getText());

        if ("Limit reached for today".equals(result)) {
            return ResponseEntity.status(429).body(result);
        }

        return ResponseEntity.ok(result);
    }
}
