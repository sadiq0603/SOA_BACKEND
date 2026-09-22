package com.bibliotech.book.repository;

import com.bibliotech.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookCode(String bookCode);

        @Query("SELECT b FROM Book b WHERE " +
            "(COALESCE(:search, '') = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (COALESCE(:category, '') = '' OR LOWER(b.category) = LOWER(:category)) " +
           "AND (:branchId IS NULL OR b.branch.id = :branchId) " +
           "AND (:available IS NULL OR (:available = true AND b.availableCopies > 0) OR (:available = false AND b.availableCopies = 0))")
    Page<Book> searchBooks(@Param("search") String search,
                           @Param("category") String category,
                           @Param("branchId") Long branchId,
                           @Param("available") Boolean available,
                           Pageable pageable);

    long countByAvailableCopiesGreaterThan(int count);
}
