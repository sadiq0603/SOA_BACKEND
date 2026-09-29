package com.bibliotech.rental.controller;

import com.bibliotech.rental.dto.ReservationResponse;
import com.bibliotech.rental.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody Map<String, Long> request,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = request.containsKey("userId") && request.get("userId") != null
                ? request.get("userId")
                : headerUserId;
        Long bookId = request.get("bookId");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(userId, bookId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = userId != null ? userId : headerUserId;
        return ResponseEntity.ok(reservationService.getMyReservations(effectiveUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long effectiveUserId = userId != null ? userId : headerUserId;
        reservationService.cancelReservation(id, effectiveUserId);
        return ResponseEntity.noContent().build();
    }
}
