package com.bibliotech.fine.controller;

import com.bibliotech.fine.dto.FineRequest;
import com.bibliotech.fine.entity.Fine;
import com.bibliotech.fine.service.FineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<?> calculate(@Valid @RequestBody FineRequest request) {
        if (request.daysLate() <= 0) return ResponseEntity.ok().body(java.util.Map.of("amount", 0, "daysLate", 0));
        return ResponseEntity.ok(fineService.calculate(request));
    }

    @GetMapping("/user/{userId}")
    public List<Fine> findByUser(@PathVariable Long userId) {
        return fineService.findByUser(userId);
    }

    @GetMapping("/{id}")
    public Fine findById(@PathVariable Long id) {
        return fineService.findById(id);
    }

    @PutMapping("/{id}/pay")
    public Fine pay(@PathVariable Long id) {
        return fineService.pay(id);
    }
}
