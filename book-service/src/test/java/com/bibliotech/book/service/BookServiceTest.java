package com.bibliotech.book.service;

import com.bibliotech.book.dto.BookRequest;
import com.bibliotech.book.dto.BookResponse;
import com.bibliotech.book.entity.Book;
import com.bibliotech.book.entity.BookStatus;
import com.bibliotech.book.exception.BookNotFoundException;
import com.bibliotech.book.exception.BookUnavailableException;
import com.bibliotech.book.repository.BookRepository;
import com.bibliotech.book.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .bookCode("BK-12345678")
                .isbn("978-0134685991")
                .title("Effective Java")
                .author("Joshua Bloch")
                .category("Computer Science")
                .totalCopies(5)
                .availableCopies(3)
                .status(BookStatus.AVAILABLE)
                .build();
    }

    @Test
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.getBookById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Effective Java", response.getTitle());
    }

    @Test
    void getBookById_NotFound_ThrowsException() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBookById(99L));
    }

    @Test
    void createBook_Success() {
        BookRequest request = BookRequest.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .category("Software Engineering")
                .totalCopies(3)
                .build();

        Book newBook = Book.builder()
                .id(2L)
                .bookCode("BK-87654321")
                .title("Clean Code")
                .author("Robert C. Martin")
                .isbn("978-0132350884")
                .category("Software Engineering")
                .totalCopies(3)
                .availableCopies(3)
                .status(BookStatus.AVAILABLE)
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(newBook);

        BookResponse response = bookService.createBook(request);

        assertNotNull(response);
        assertEquals("Clean Code", response.getTitle());
        assertEquals(3, response.getAvailableCopies());
    }

    @Test
    void issueBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.issueBook(1L);

        assertNotNull(response);
        assertEquals(2, response.getAvailableCopies());
    }

    @Test
    void issueBook_Unavailable_ThrowsException() {
        sampleBook.setAvailableCopies(0);
        sampleBook.setStatus(BookStatus.UNAVAILABLE);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        assertThrows(BookUnavailableException.class, () -> bookService.issueBook(1L));
    }

    @Test
    void returnBook_Success() {
        sampleBook.setAvailableCopies(2);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.returnBook(1L);

        assertNotNull(response);
        assertEquals(3, response.getAvailableCopies());
        assertEquals("AVAILABLE", response.getStatus());
    }
}
