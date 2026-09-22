package com.bibliotech.rental.controller;

import com.bibliotech.rental.dto.BorrowRequest;
import com.bibliotech.rental.dto.RentalResponse;
import com.bibliotech.rental.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RentalService rentalService;

    @Test
    void borrowBook_Returns201Created() throws Exception {
        BorrowRequest request = BorrowRequest.builder().userId(10L).bookId(100L).build();
        RentalResponse response = RentalResponse.builder()
                .id(1L)
                .userId(10L)
                .bookId(100L)
                .bookTitle("Clean Code")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .status("ISSUED")
                .build();

        when(rentalService.borrowBook(10L, 100L)).thenReturn(response);

        mockMvc.perform(post("/api/rentals/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Clean Code"));
    }

    @Test
    void returnBook_Returns200Ok() throws Exception {
        RentalResponse response = RentalResponse.builder()
                .id(1L)
                .userId(10L)
                .bookId(100L)
                .status("RETURNED")
                .daysLate(0L)
                .build();

        when(rentalService.returnBook(eq(1L), eq(10L))).thenReturn(response);

        mockMvc.perform(post("/api/rentals/1/return")
                        .param("userId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }
}
