package com.bibliotech.book.service;

import com.bibliotech.book.dto.*;
import com.bibliotech.book.entity.Book;
import com.bibliotech.book.entity.BookStatus;
import com.bibliotech.book.entity.Branch;
import com.bibliotech.book.exception.BookNotFoundException;
import com.bibliotech.book.exception.BookUnavailableException;
import com.bibliotech.book.repository.BookRepository;
import com.bibliotech.book.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BranchRepository branchRepository;

    public Page<BookResponse> searchBooks(String search, String category, Long branchId, Boolean available, Pageable pageable) {
        return bookRepository.searchBooks(search, category, branchId, available, pageable)
                .map(this::mapToResponse);
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));
        return mapToResponse(book);
    }

    public BookResponse getBookByCode(String bookCode) {
        Book book = bookRepository.findByBookCode(bookCode)
                .orElseThrow(() -> new BookNotFoundException("Book not found with code: " + bookCode));
        return mapToResponse(book);
    }

    @Transactional
    public BookResponse createBook(BookRequest request) {
        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId()).orElse(null);
        }

        String bookCode = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Book book = Book.builder()
                .bookCode(bookCode)
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(request.getAuthor())
                .category(request.getCategory())
                .description(request.getDescription())
                .coverImage(request.getCoverImage())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .status(BookStatus.AVAILABLE)
                .branch(branch)
                .build();

        book = bookRepository.save(book);
        return mapToResponse(book);
    }

    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setCoverImage(request.getCoverImage());

        int diff = request.getTotalCopies() - book.getTotalCopies();
        book.setTotalCopies(request.getTotalCopies());
        int newAvailable = Math.max(0, book.getAvailableCopies() + diff);
        book.setAvailableCopies(newAvailable);
        if (newAvailable == 0) {
            book.setStatus(BookStatus.UNAVAILABLE);
        } else {
            book.setStatus(BookStatus.AVAILABLE);
        }

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId()).orElse(null);
            book.setBranch(branch);
        }

        book = bookRepository.save(book);
        return mapToResponse(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Transactional
    public BookResponse issueBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No available copies for book: " + book.getTitle());
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        if (book.getAvailableCopies() == 0) {
            book.setStatus(BookStatus.UNAVAILABLE);
        }

        book = bookRepository.save(book);
        return mapToResponse(book);
    }

    @Transactional
    public BookResponse returnBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + id));

        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        if (book.getAvailableCopies() > 0) {
            book.setStatus(BookStatus.AVAILABLE);
        }

        book = bookRepository.save(book);
        return mapToResponse(book);
    }

    public long getTotalBooks() {
        return bookRepository.count();
    }

    public long getAvailableBooks() {
        return bookRepository.countByAvailableCopiesGreaterThan(0);
    }

    private BookResponse mapToResponse(Book book) {
        BranchDto branchDto = null;
        if (book.getBranch() != null) {
            branchDto = BranchDto.builder()
                    .id(book.getBranch().getId())
                    .name(book.getBranch().getName())
                    .location(book.getBranch().getLocation())
                    .active(book.getBranch().isActive())
                    .build();
        }

        return BookResponse.builder()
                .id(book.getId())
                .bookCode(book.getBookCode())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .category(book.getCategory())
                .description(book.getDescription())
                .coverImage(book.getCoverImage())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .status(book.getStatus().name())
                .branch(branchDto)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
