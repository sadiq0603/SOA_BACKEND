package com.bibliotech.fine.dto;

public record FineRequest(Long rentalId, Long userId, Long bookId, long daysLate) {
}
