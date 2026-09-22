package com.bibliotech.rental.client;

import com.bibliotech.rental.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "book-service", fallback = BookServiceClientFallback.class)
public interface BookServiceClient {

    @GetMapping("/api/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    @PutMapping("/api/books/{id}/issue")
    BookDto issueBook(@PathVariable("id") Long id);

    @PutMapping("/api/books/{id}/return")
    BookDto returnBook(@PathVariable("id") Long id);
}
