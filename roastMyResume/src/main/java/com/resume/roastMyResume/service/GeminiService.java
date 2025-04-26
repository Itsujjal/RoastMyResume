package com.resume.roastMyResume.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    private static final int LIMIT_PER_DAY = 4;
    private final Map<String, List<Instant>> ipCallTracker = new ConcurrentHashMap<>();

    private final GeminiAdapterService geminiAdapterService;

    public GeminiService(GeminiAdapterService geminiAdapterService) {
        this.geminiAdapterService = geminiAdapterService;
    }

    public String processRequest(String ipAddress, String text) {
        cleanupOldEntries(ipAddress);

        List<Instant> callTimes = ipCallTracker.getOrDefault(ipAddress, List.of());

        if (callTimes.size() >= LIMIT_PER_DAY) {
            return "Limit reached for today";
        }

        ipCallTracker.merge(ipAddress, List.of(Instant.now()), (existing, newCall) -> {
            existing.addAll(newCall);
            return existing;
        });
        return geminiAdapterService.callGeminiApi(text, ipAddress);
    }

    private void cleanupOldEntries(String ipAddress) {
        List<Instant> callTimes = ipCallTracker.get(ipAddress);

        if (callTimes != null) {
            Instant twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60);

            List<Instant> updatedCalls = callTimes.stream()
                    .filter(time -> time.isAfter(twentyFourHoursAgo))
                    .collect(Collectors.toList());

            ipCallTracker.put(ipAddress, updatedCalls);
        }
    }
}
