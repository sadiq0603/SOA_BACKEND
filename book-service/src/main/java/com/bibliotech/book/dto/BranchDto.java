package com.bibliotech.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BranchDto {
    private Long id;
    private String name;
    private String location;
    private boolean active;
}
