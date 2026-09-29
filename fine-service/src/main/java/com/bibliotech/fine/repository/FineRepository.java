package com.bibliotech.fine.repository;

import com.bibliotech.fine.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Fine> findByRentalId(Long rentalId);
    boolean existsByRentalId(Long rentalId);
}
