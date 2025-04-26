package com.resume.roastMyResume.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS = 5;
    private static final long TIME_FRAME = TimeUnit.HOURS.toMillis(24);
    private Map<String, LinkedList<Long>> ipRequests = new HashMap<>();


    public boolean isRateLimited(String ipAddress) {
        long currentTime = System.currentTimeMillis();
        LinkedList<Long> timestamps = ipRequests.getOrDefault(ipAddress, new LinkedList<>());
        while (!timestamps.isEmpty() && currentTime - timestamps.peekFirst() > TIME_FRAME) {
            timestamps.pollFirst();
        }
        if (timestamps.size() >= MAX_REQUESTS) {
            return true;
        }

        timestamps.add(currentTime);
        ipRequests.put(ipAddress, timestamps);
        return false;
    }
}
