package com.bibliotech.rental.client;

import com.bibliotech.rental.dto.BookDto;
import org.springframework.stereotype.Component;

@Component
public class BookServiceClientFallback implements BookServiceClient {

    @Override
    public BookDto getBookById(Long id) {
        return null;
    }

    @Override
    public BookDto issueBook(Long id) {
        return null;
    }

    @Override
    public BookDto returnBook(Long id) {
        return null;
    }
}
