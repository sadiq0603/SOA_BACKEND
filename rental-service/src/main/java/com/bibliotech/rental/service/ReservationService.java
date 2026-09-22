package com.bibliotech.rental.service;

import com.bibliotech.rental.client.BookServiceClient;
import com.bibliotech.rental.dto.BookDto;
import com.bibliotech.rental.dto.ReservationResponse;
import com.bibliotech.rental.entity.Reservation;
import com.bibliotech.rental.entity.ReservationStatus;
import com.bibliotech.rental.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BookServiceClient bookServiceClient;

    public ReservationResponse createReservation(Long userId, Long bookId) {
        // Check for duplicate
        boolean exists = reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, Arrays.asList(ReservationStatus.WAITING, ReservationStatus.AVAILABLE));
        if (exists) {
            throw new RuntimeException("You already have an active reservation for this book.");
        }

        int position = reservationRepository.countByBookIdAndStatus(bookId, ReservationStatus.WAITING) + 1;

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .bookId(bookId)
                .queuePosition(position)
                .status(ReservationStatus.WAITING)
                .build();

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
    }

    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void cancelReservation(Long id, Long userId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to cancel this reservation");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        ReservationResponse response = ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .bookId(reservation.getBookId())
                .reservationDate(reservation.getReservationDate())
                .queuePosition(reservation.getQueuePosition())
                .status(reservation.getStatus().name())
                .build();

        try {
            BookDto book = bookServiceClient.getBookById(reservation.getBookId());
            if (book != null) {
                response.setBookTitle(book.getTitle());
                response.setBookAuthor(book.getAuthor());
            }
        } catch (Exception e) {
            log.warn("Could not fetch book details for reservation");
        }

        return response;
    }
}
