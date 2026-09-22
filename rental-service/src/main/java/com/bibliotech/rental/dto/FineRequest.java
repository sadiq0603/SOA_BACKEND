package com.bibliotech.rental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FineRequest {
    private Long rentalId;
    private Long userId;
    private Long bookId;
    private long daysLate;
}
