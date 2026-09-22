package com.bibliotech.fine.repository;

import com.bibliotech.fine.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByRentalId(Long rentalId);
}
