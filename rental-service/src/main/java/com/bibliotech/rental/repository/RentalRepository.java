package com.bibliotech.rental.repository;

import com.bibliotech.rental.entity.Rental;
import com.bibliotech.rental.entity.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByUserId(Long userId);
    List<Rental> findByUserIdAndStatus(Long userId, RentalStatus status);
    List<Rental> findByStatus(RentalStatus status);
    Optional<Rental> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, RentalStatus status);
    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<RentalStatus> statuses);
    List<Rental> findByDueDateBeforeAndStatus(LocalDate date, RentalStatus status);
    List<Rental> findByDueDateAndStatus(LocalDate date, RentalStatus status);
    long countByStatus(RentalStatus status);
}
