package com.bibliotech.fine.service;

import com.bibliotech.fine.dto.FineRequest;
import com.bibliotech.fine.entity.Fine;
import com.bibliotech.fine.entity.FineStatus;
import com.bibliotech.fine.repository.FineRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FineService {
    private static final BigDecimal DAILY_RATE = BigDecimal.valueOf(5);
    private final FineRepository fineRepository;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    public Fine calculate(FineRequest request) {
        if (request.daysLate() <= 0) {
            return null;
        }
        Fine fine = fineRepository.findAll().stream()
                .filter(existing -> request.rentalId().equals(existing.getRentalId()))
                .findFirst().orElseGet(Fine::new);
        fine.setRentalId(request.rentalId());
        fine.setUserId(request.userId());
        fine.setBookId(request.bookId());
        fine.setDaysLate(request.daysLate());
        fine.setAmount(DAILY_RATE.multiply(BigDecimal.valueOf(request.daysLate())));
        return fineRepository.save(fine);
    }

    public List<Fine> findByUser(Long userId) {
        return fineRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Fine findById(Long id) {
        return fineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fine not found"));
    }

    public Fine pay(Long id) {
        Fine fine = findById(id);
        if (fine.getStatus() == FineStatus.PAID) return fine;
        fine.setStatus(FineStatus.PAID);
        fine.setPaidAt(LocalDateTime.now());
        fine.setTransactionId("TXN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        return fineRepository.save(fine);
    }
}
