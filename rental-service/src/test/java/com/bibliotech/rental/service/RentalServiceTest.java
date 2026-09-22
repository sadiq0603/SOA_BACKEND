package com.bibliotech.rental.service;

import com.bibliotech.rental.client.BookServiceClient;
import com.bibliotech.rental.client.FineServiceClient;
import com.bibliotech.rental.dto.BookDto;
import com.bibliotech.rental.dto.FineRequest;
import com.bibliotech.rental.dto.RentalResponse;
import com.bibliotech.rental.entity.Notification;
import com.bibliotech.rental.entity.Rental;
import com.bibliotech.rental.entity.RentalStatus;
import com.bibliotech.rental.exception.BookUnavailableException;
import com.bibliotech.rental.exception.DuplicateRentalException;
import com.bibliotech.rental.exception.RentalNotFoundException;
import com.bibliotech.rental.repository.NotificationRepository;
import com.bibliotech.rental.repository.RentalRepository;
import com.bibliotech.rental.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private BookServiceClient bookServiceClient;

    @Mock
    private FineServiceClient fineServiceClient;

    @InjectMocks
    private RentalService rentalService;

    private BookDto sampleBook;
    private Rental sampleRental;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rentalService, "borrowDurationDays", 14);

        sampleBook = BookDto.builder()
                .id(100L)
                .title("Clean Architecture")
                .author("Robert C. Martin")
                .availableCopies(3)
                .status("AVAILABLE")
                .build();

        sampleRental = Rental.builder()
                .id(1L)
                .userId(10L)
                .bookId(100L)
                .issueDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(6)) // 6 days late
                .status(RentalStatus.ISSUED)
                .build();
    }

    @Test
    void borrowBook_Success() {
        when(rentalRepository.existsByUserIdAndBookIdAndStatusIn(eq(10L), eq(100L), any())).thenReturn(false);
        when(bookServiceClient.getBookById(100L)).thenReturn(sampleBook);
        when(bookServiceClient.issueBook(100L)).thenReturn(sampleBook);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> {
            Rental r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });

        RentalResponse response = rentalService.borrowBook(10L, 100L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10L, response.getUserId());
        assertEquals("Clean Architecture", response.getBookTitle());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void borrowBook_DuplicateRental_ThrowsException() {
        when(rentalRepository.existsByUserIdAndBookIdAndStatusIn(eq(10L), eq(100L), any())).thenReturn(true);

        assertThrows(DuplicateRentalException.class, () -> rentalService.borrowBook(10L, 100L));
        verify(bookServiceClient, never()).issueBook(anyLong());
    }

    @Test
    void borrowBook_NoCopiesAvailable_ThrowsException() {
        sampleBook.setAvailableCopies(0);
        when(rentalRepository.existsByUserIdAndBookIdAndStatusIn(eq(10L), eq(100L), any())).thenReturn(false);
        when(bookServiceClient.getBookById(100L)).thenReturn(sampleBook);

        assertThrows(BookUnavailableException.class, () -> rentalService.borrowBook(10L, 100L));
    }

    @Test
    void returnBook_Overdue_CalculatesFine() {
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(sampleRental));
        when(bookServiceClient.returnBook(100L)).thenReturn(sampleBook);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.findByBookIdAndStatusOrderByQueuePositionAsc(eq(100L), any()))
                .thenReturn(Collections.emptyList());

        RentalResponse response = rentalService.returnBook(1L, 10L);

        assertNotNull(response);
        assertEquals("RETURNED", response.getStatus());
        assertEquals(6, response.getDaysLate()); // 6 days late
        verify(fineServiceClient, times(1)).calculateFine(any(FineRequest.class));
    }

    @Test
    void returnBook_NotFound_ThrowsException() {
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RentalNotFoundException.class, () -> rentalService.returnBook(99L, 10L));
    }
}
