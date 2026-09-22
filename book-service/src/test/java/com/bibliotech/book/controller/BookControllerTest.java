package com.bibliotech.book.controller;

import com.bibliotech.book.dto.BookRequest;
import com.bibliotech.book.dto.BookResponse;
import com.bibliotech.book.entity.BookStatus;
import com.bibliotech.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    void getBookById_Returns200Ok() throws Exception {
        BookResponse response = BookResponse.builder()
                .id(1L)
                .bookCode("BK-100")
                .title("Design Patterns")
                .author("GoF")
                .availableCopies(3)
                .status("AVAILABLE")
                .build();

        when(bookService.getBookById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Design Patterns"));
    }

    @Test
    void createBook_Returns201Created() throws Exception {
        BookRequest request = BookRequest.builder()
                .title("Refactoring")
                .author("Martin Fowler")
                .isbn("978-0201485677")
                .totalCopies(2)
                .build();

        BookResponse response = BookResponse.builder()
                .id(2L)
                .title("Refactoring")
                .author("Martin Fowler")
                .availableCopies(2)
                .status("AVAILABLE")
                .build();

        when(bookService.createBook(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Refactoring"));
    }

    @Test
    void issueBook_Returns200Ok() throws Exception {
        BookResponse response = BookResponse.builder()
                .id(1L)
                .title("Design Patterns")
                .availableCopies(2)
                .status("AVAILABLE")
                .build();

        when(bookService.issueBook(eq(1L))).thenReturn(response);

        mockMvc.perform(put("/api/books/1/issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCopies").value(2));
    }
}
