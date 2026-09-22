package com.bibliotech.rental.client;

import com.bibliotech.rental.dto.FineRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "fine-service", fallback = FineServiceClientFallback.class)
public interface FineServiceClient {

    @PostMapping("/api/fines/calculate")
    Map<String, Object> calculateFine(@RequestBody FineRequest request);
}
