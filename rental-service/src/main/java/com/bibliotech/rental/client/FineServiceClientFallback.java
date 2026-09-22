package com.bibliotech.rental.client;

import com.bibliotech.rental.dto.FineRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FineServiceClientFallback implements FineServiceClient {

    @Override
    public Map<String, Object> calculateFine(FineRequest request) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("message", "Fine service is temporarily unavailable");
        fallback.put("status", "UNAVAILABLE");
        return fallback;
    }
}
