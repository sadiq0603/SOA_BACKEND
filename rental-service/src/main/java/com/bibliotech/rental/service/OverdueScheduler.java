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

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueScheduler {

    private final RentalRepository rentalRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 3600000) // Every hour
    public void checkOverdueBooks() {
        log.info("Running overdue book check...");
        LocalDate today = LocalDate.now();

        List<Rental> overdueRentals = rentalRepository.findByDueDateBeforeAndStatus(today, RentalStatus.ISSUED);
        for (Rental rental : overdueRentals) {
            rental.setStatus(RentalStatus.OVERDUE);
            rentalRepository.save(rental);

            Notification notification = Notification.builder()
                    .userId(rental.getUserId())
                    .title("Book Overdue")
                    .message("Your borrowed book is overdue. Please return it as soon as possible.")
                    .type(NotificationType.OVERDUE)
                    .read(false)
                    .build();
            notificationRepository.save(notification);
        }

        // Due tomorrow notifications
        List<Rental> dueTomorrow = rentalRepository.findByDueDateAndStatus(today.plusDays(1), RentalStatus.ISSUED);
        for (Rental rental : dueTomorrow) {
            Notification notification = Notification.builder()
                    .userId(rental.getUserId())
                    .title("Due Tomorrow")
                    .message("Your borrowed book is due tomorrow. Please return it on time.")
                    .type(NotificationType.DUE_TOMORROW)
                    .read(false)
                    .build();
            notificationRepository.save(notification);
        }

        log.info("Overdue check completed. Found {} overdue, {} due tomorrow.",
                overdueRentals.size(), dueTomorrow.size());
    }
}
