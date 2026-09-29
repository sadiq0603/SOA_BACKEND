package com.bibliotech.rental.service;

import com.bibliotech.rental.entity.Notification;
import com.bibliotech.rental.entity.NotificationType;
import com.bibliotech.rental.entity.Rental;
import com.bibliotech.rental.entity.RentalStatus;
import com.bibliotech.rental.repository.NotificationRepository;
import com.bibliotech.rental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueScheduler {

    private final RentalRepository rentalRepository;
    private final NotificationRepository notificationRepository;

    // Runs once per day at midnight to avoid duplicate notifications
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkOverdueBooks() {
        log.info("Running overdue book check...");
        LocalDate today = LocalDate.now();

        // Mark overdue rentals and batch-save notifications
        List<Rental> overdueRentals = rentalRepository.findByDueDateBeforeAndStatus(today, RentalStatus.ISSUED);
        if (!overdueRentals.isEmpty()) {
            overdueRentals.forEach(rental -> rental.setStatus(RentalStatus.OVERDUE));
            rentalRepository.saveAll(overdueRentals);

            List<Notification> overdueNotifications = overdueRentals.stream()
                    .map(rental -> Notification.builder()
                            .userId(rental.getUserId())
                            .title("Book Overdue")
                            .message("Your borrowed book (ID: " + rental.getBookId() + ") is overdue. Please return it as soon as possible.")
                            .type(NotificationType.OVERDUE)
                            .read(false)
                            .build())
                    .collect(Collectors.toList());
            notificationRepository.saveAll(overdueNotifications);
        }

        // Due tomorrow notifications — batch save
        List<Rental> dueTomorrow = rentalRepository.findByDueDateAndStatus(today.plusDays(1), RentalStatus.ISSUED);
        if (!dueTomorrow.isEmpty()) {
            List<Notification> dueTomorrowNotifications = dueTomorrow.stream()
                    .map(rental -> Notification.builder()
                            .userId(rental.getUserId())
                            .title("Due Tomorrow")
                            .message("Your borrowed book (ID: " + rental.getBookId() + ") is due tomorrow. Please return it on time.")
                            .type(NotificationType.DUE_TOMORROW)
                            .read(false)
                            .build())
                    .collect(Collectors.toList());
            notificationRepository.saveAll(dueTomorrowNotifications);
        }

        log.info("Overdue check completed. Found {} overdue, {} due tomorrow.",
                overdueRentals.size(), dueTomorrow.size());
    }
}
