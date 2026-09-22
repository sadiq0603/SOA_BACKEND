package com.bibliotech.rental.controller;

import com.bibliotech.rental.dto.BorrowRequest;
import com.bibliotech.rental.dto.RentalResponse;
import com.bibliotech.rental.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // =========================================================
    // BORROW BOOK
    // =========================================================

    @PostMapping("/borrow")
    public ResponseEntity<RentalResponse> borrowBook(
            @RequestBody BorrowRequest request) {

        RentalResponse response =
                rentalService.borrowBook(
                        request.getUserId(),
                        request.getBookId()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================================================
    // RETURN BOOK
    // =========================================================

    @PostMapping("/{id}/return")
    public ResponseEntity<RentalResponse> returnBook(
            @PathVariable Long id,
            @RequestParam Long userId) {

        RentalResponse response =
                rentalService.returnBook(id, userId);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // MY RENTALS
    // =========================================================

    @GetMapping("/my")
    public ResponseEntity<List<RentalResponse>> getMyRentals(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                rentalService.getMyRentals(userId)
        );
    }

    // =========================================================
    // ACTIVE RENTALS
    // =========================================================

    @GetMapping("/active")
    public ResponseEntity<List<RentalResponse>> getActiveRentals() {

        return ResponseEntity.ok(
                rentalService.getActiveRentals()
        );
    }

    // =========================================================
    // OVERDUE RENTALS
    // =========================================================

    @GetMapping("/overdue")
    public ResponseEntity<List<RentalResponse>> getOverdueRentals() {

        return ResponseEntity.ok(
                rentalService.getOverdueRentals()
        );
    }

    // =========================================================
    // ALL RENTALS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<RentalResponse>> getAllRentals() {

        return ResponseEntity.ok(
                rentalService.getAllRentals()
        );
    }

    // =========================================================
    // ACTIVE RENTAL COUNT
    // =========================================================

    @GetMapping("/stats/active")
    public ResponseEntity<Long> getActiveCount() {

        return ResponseEntity.ok(
                rentalService.getActiveRentalCount()
        );
    }

    // =========================================================
    // OVERDUE RENTAL COUNT
    // =========================================================

    @GetMapping("/stats/overdue")
    public ResponseEntity<Long> getOverdueCount() {

        return ResponseEntity.ok(
                rentalService.getOverdueRentalCount()
        );
    }
}