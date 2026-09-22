package com.bibliotech.rental.service;

import com.bibliotech.rental.client.BookServiceClient;
import com.bibliotech.rental.client.FineServiceClient;
import com.bibliotech.rental.dto.*;
import com.bibliotech.rental.entity.*;
import com.bibliotech.rental.exception.*;
import com.bibliotech.rental.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;
    private final BookServiceClient bookServiceClient;
    private final FineServiceClient fineServiceClient;

    @Value("${rental.borrow-duration-days:14}")
    private int borrowDurationDays;

    @Transactional
    @CircuitBreaker(name = "bookService", fallbackMethod = "borrowFallback")
    public RentalResponse borrowBook(Long userId, Long bookId) {
        // Check for duplicate active rental
        boolean hasActive = rentalRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, Arrays.asList(RentalStatus.ISSUED, RentalStatus.OVERDUE));
        if (hasActive) {
            throw new DuplicateRentalException("You already have this book issued.");
        }

        // Get book details and check availability
        BookDto book = bookServiceClient.getBookById(bookId);
        if (book == null) {
            throw new ServiceUnavailableException("Book service is temporarily unavailable. Please try again.");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BookUnavailableException("No available copies for this book.");
        }

        // Issue book (decrease inventory)
        BookDto issued = bookServiceClient.issueBook(bookId);
        if (issued == null) {
            throw new ServiceUnavailableException("Failed to issue book. Please try again.");
        }

        // Create rental
        LocalDate now = LocalDate.now();
        Rental rental = Rental.builder()
                .userId(userId)
                .bookId(bookId)
                .issueDate(now)
                .dueDate(now.plusDays(borrowDurationDays))
                .status(RentalStatus.ISSUED)
                .build();

        rental = rentalRepository.save(rental);

        // Create notification
        createNotification(userId, "Book Borrowed",
                book.getTitle() + " has been successfully borrowed. Due: " + rental.getDueDate(),
                NotificationType.BORROW_SUCCESS);

        return mapToResponse(rental, book);
    }

    @Transactional
    @CircuitBreaker(name = "bookService", fallbackMethod = "returnFallback")
    public RentalResponse returnBook(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException("Rental not found with id: " + rentalId));

        if (rental.getStatus() == RentalStatus.RETURNED) {
            throw new RuntimeException("Book has already been returned.");
        }

        LocalDate returnDate = LocalDate.now();
        rental.setReturnDate(returnDate);
        rental.setStatus(RentalStatus.RETURNED);

        // Return book (increase inventory)
        BookDto book = bookServiceClient.returnBook(rental.getBookId());

        // Check if overdue and calculate fine
        long daysLate = 0;
        if (returnDate.isAfter(rental.getDueDate())) {
            daysLate = ChronoUnit.DAYS.between(rental.getDueDate(), returnDate);
            try {
                FineRequest fineRequest = FineRequest.builder()
                        .rentalId(rental.getId())
                        .userId(rental.getUserId())
                        .bookId(rental.getBookId())
                        .daysLate(daysLate)
                        .build();
                fineServiceClient.calculateFine(fineRequest);

                createNotification(userId, "Fine Generated",
                        "A fine of ₹" + (daysLate * 5) + " has been generated for late return.",
                        NotificationType.FINE_GENERATED);
            } catch (Exception e) {
                log.error("Failed to calculate fine: {}", e.getMessage());
            }
        }

        rental = rentalRepository.save(rental);

        // Check reservations for this book
        checkReservationsForBook(rental.getBookId());

        createNotification(userId, "Book Returned",
                (book != null ? book.getTitle() : "Book") + " has been successfully returned.",
                NotificationType.RETURN_SUCCESS);

        RentalResponse response = mapToResponse(rental, book);
        response.setDaysLate(daysLate);
        return response;
    }

    public List<RentalResponse> getMyRentals(Long userId) {
        return rentalRepository.findByUserId(userId).stream()
                .map(rental -> {
                    BookDto book = null;
                    try {
                        book = bookServiceClient.getBookById(rental.getBookId());
                    } catch (Exception e) {
                        log.warn("Could not fetch book details for bookId: {}", rental.getBookId());
                    }
                    return mapToResponse(rental, book);
                })
                .collect(Collectors.toList());
    }

    public List<RentalResponse> getActiveRentals() {
        return rentalRepository.findByStatus(RentalStatus.ISSUED).stream()
                .map(rental -> {
                    BookDto book = null;
                    try {
                        book = bookServiceClient.getBookById(rental.getBookId());
                    } catch (Exception e) {
                        log.warn("Could not fetch book details for bookId: {}", rental.getBookId());
                    }
                    return mapToResponse(rental, book);
                })
                .collect(Collectors.toList());
    }

    public List<RentalResponse> getOverdueRentals() {
        return rentalRepository.findByStatus(RentalStatus.OVERDUE).stream()
                .map(rental -> {
                    BookDto book = null;
                    try {
                        book = bookServiceClient.getBookById(rental.getBookId());
                    } catch (Exception e) {
                        log.warn("Could not fetch book details for bookId: {}", rental.getBookId());
                    }
                    RentalResponse response = mapToResponse(rental, book);
                    response.setDaysLate(ChronoUnit.DAYS.between(rental.getDueDate(), LocalDate.now()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(rental -> {
                    BookDto book = null;
                    try {
                        book = bookServiceClient.getBookById(rental.getBookId());
                    } catch (Exception e) {
                        log.warn("Could not fetch book details for bookId: {}", rental.getBookId());
                    }
                    return mapToResponse(rental, book);
                })
                .collect(Collectors.toList());
    }

    public long getActiveRentalCount() {
        return rentalRepository.countByStatus(RentalStatus.ISSUED);
    }

    public long getOverdueRentalCount() {
        return rentalRepository.countByStatus(RentalStatus.OVERDUE);
    }

    private void checkReservationsForBook(Long bookId) {
        reservationRepository
                .findFirstByBookIdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.WAITING)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.AVAILABLE);
                    reservationRepository.save(reservation);
                    createNotification(reservation.getUserId(), "Reservation Available",
                            "A book you reserved is now available!",
                            NotificationType.RESERVATION_AVAILABLE);
                });
    }

    private void createNotification(Long userId, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .build();
        notificationRepository.save(notification);
    }

    private RentalResponse mapToResponse(Rental rental, BookDto book) {
        RentalResponse response = RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUserId())
                .bookId(rental.getBookId())
                .issueDate(rental.getIssueDate())
                .dueDate(rental.getDueDate())
                .returnDate(rental.getReturnDate())
                .status(rental.getStatus().name())
                .createdAt(rental.getCreatedAt())
                .build();

        if (book != null) {
            response.setBookTitle(book.getTitle());
            response.setBookAuthor(book.getAuthor());
        }

        if (rental.getStatus() != RentalStatus.RETURNED && rental.getDueDate().isBefore(LocalDate.now())) {
            response.setDaysLate(ChronoUnit.DAYS.between(rental.getDueDate(), LocalDate.now()));
        }

        return response;
    }

    // Fallback methods
    public RentalResponse borrowFallback(Long userId, Long bookId, Exception e) {
        if (e instanceof DuplicateRentalException || e instanceof BookUnavailableException || e instanceof ServiceUnavailableException) {
            throw (RuntimeException) e;
        }
        throw new ServiceUnavailableException("Book service is temporarily unavailable. Please try again later.");
    }

    public RentalResponse returnFallback(Long rentalId, Long userId, Exception e) {
        if (e instanceof RentalNotFoundException || e instanceof ServiceUnavailableException) {
            throw (RuntimeException) e;
        }
        throw new ServiceUnavailableException("Service is temporarily unavailable. Please try again later.");
    }
}
