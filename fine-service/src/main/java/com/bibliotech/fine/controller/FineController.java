package com.bibliotech.fine.controller;

import com.bibliotech.fine.dto.FineRequest;
import com.bibliotech.fine.entity.Fine;
import com.bibliotech.fine.service.FineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fines")
public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@RequestBody FineRequest request) {
        if (request.daysLate() <= 0) {
            return ResponseEntity.ok(Map.of("amount", 0, "daysLate", 0));
        }
        return ResponseEntity.ok(fineService.calculate(request));
    }

    @GetMapping
    public ResponseEntity<List<Fine>> getAllFines(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = (userId != null) ? userId : headerUserId;
        if (effectiveUserId != null) {
            return ResponseEntity.ok(fineService.findByUser(effectiveUserId));
        }
        return ResponseEntity.ok(fineService.findAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Fine>> getMyFines(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = (userId != null) ? userId : headerUserId;
        if (effectiveUserId != null) {
            return ResponseEntity.ok(fineService.findByUser(effectiveUserId));
        }
        return ResponseEntity.ok(fineService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Fine>> findByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(fineService.findByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fine> findById(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.findById(id));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Fine> pay(@PathVariable Long id) {
        return ResponseEntity.ok(fineService.pay(id));
    }
}
