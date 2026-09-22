package com.bibliotech.fine.service;

import com.bibliotech.fine.dto.FineRequest;
import com.bibliotech.fine.entity.Fine;
import com.bibliotech.fine.entity.FineStatus;
import com.bibliotech.fine.repository.FineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private FineService fineService;

    private Fine sampleFine;

    @BeforeEach
    void setUp() {
        sampleFine = new Fine();
        sampleFine.setId(1L);
        sampleFine.setRentalId(10L);
        sampleFine.setUserId(100L);
        sampleFine.setBookId(50L);
        sampleFine.setDaysLate(4L);
        sampleFine.setAmount(BigDecimal.valueOf(20)); // 4 * 5
        sampleFine.setStatus(FineStatus.UNPAID);
    }

    @Test
    void calculate_Success() {
        FineRequest request = new FineRequest(10L, 100L, 50L, 4L);

        when(fineRepository.findAll()).thenReturn(Collections.emptyList());
        when(fineRepository.save(any(Fine.class))).thenAnswer(i -> i.getArgument(0));

        Fine result = fineService.calculate(request);

        assertNotNull(result);
        assertEquals(10L, result.getRentalId());
        assertEquals(4L, result.getDaysLate());
        assertEquals(BigDecimal.valueOf(20), result.getAmount()); // 4 * 5 = 20
    }

    @Test
    void calculate_ZeroDaysLate_ReturnsNull() {
        FineRequest request = new FineRequest(10L, 100L, 50L, 0L);

        Fine result = fineService.calculate(request);

        assertNull(result);
        verify(fineRepository, never()).save(any());
    }

    @Test
    void pay_Success() {
        when(fineRepository.findById(1L)).thenReturn(Optional.of(sampleFine));
        when(fineRepository.save(any(Fine.class))).thenAnswer(i -> i.getArgument(0));

        Fine paid = fineService.pay(1L);

        assertNotNull(paid);
        assertEquals(FineStatus.PAID, paid.getStatus());
        assertNotNull(paid.getPaidAt());
        assertTrue(paid.getTransactionId().startsWith("TXN-"));
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(fineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> fineService.findById(99L));
    }
}
