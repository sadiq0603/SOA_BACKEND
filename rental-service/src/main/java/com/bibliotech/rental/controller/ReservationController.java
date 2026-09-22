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
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody Map<String, Long> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request.get("userId"), request.get("bookId")));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@RequestParam Long userId) {
        return ResponseEntity.ok(reservationService.getMyReservations(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, @RequestParam Long userId) {
        reservationService.cancelReservation(id, userId);
        return ResponseEntity.noContent().build();
    }
}
