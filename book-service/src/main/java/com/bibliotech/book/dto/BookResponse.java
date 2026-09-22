package com.bibliotech.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookResponse {
    private Long id;
    private String bookCode;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private String description;
    private String coverImage;
    private Integer totalCopies;
    private Integer availableCopies;
    private String status;
    private BranchDto branch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
