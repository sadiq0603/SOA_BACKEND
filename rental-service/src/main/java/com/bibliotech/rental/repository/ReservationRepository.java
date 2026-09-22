package com.bibliotech.rental.repository;

import com.bibliotech.rental.entity.Reservation;
import com.bibliotech.rental.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByBookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);
    boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, List<ReservationStatus> statuses);
    Optional<Reservation> findFirstByBookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);
    int countByBookIdAndStatus(Long bookId, ReservationStatus status);
}
