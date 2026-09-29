package com.bibliotech.book.config;

import com.bibliotech.book.entity.Book;
import com.bibliotech.book.entity.BookStatus;
import com.bibliotech.book.entity.Branch;
import com.bibliotech.book.repository.BookRepository;
import com.bibliotech.book.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class BookDataInitializer implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        if (branchRepository.count() == 0) {
            log.info("Seeding initial branches and books into bibliotech_books...");

            Branch branchVijayawada = branchRepository.save(Branch.builder()
                    .name("Vijayawada Central")
                    .location("Vijayawada")
                    .active(true)
                    .build());

            Branch branchHyderabad = branchRepository.save(Branch.builder()
                    .name("Hyderabad Knowledge Center")
                    .location("Hyderabad")
                    .active(true)
                    .build());

            Branch branchVizag = branchRepository.save(Branch.builder()
                    .name("Visakhapatnam Academic Library")
                    .location("Visakhapatnam")
                    .active(true)
                    .build());

            Branch branchGuntur = branchRepository.save(Branch.builder()
                    .name("Guntur Engineering Library")
                    .location("Guntur")
                    .active(true)
                    .build());

            bookRepository.save(Book.builder()
                    .bookCode("BK-JAV-001")
                    .isbn("9780134685991")
                    .title("Effective Java (3rd Edition)")
                    .author("Joshua Bloch")
                    .category("Computer Science")
                    .description("The definitive guide to Java best practices, design patterns, and idiomatic coding.")
                    .totalCopies(4)
                    .availableCopies(4)
                    .status(BookStatus.AVAILABLE)
                    .branch(branchVijayawada)
                    .build());

            bookRepository.save(Book.builder()
                    .bookCode("BK-DB-001")
                    .isbn("9780078022159")
                    .title("Database System Concepts")
                    .author("Silberschatz, Korth & Sudarshan")
                    .category("Computer Science")
                    .description("A comprehensive introduction to database architecture, design, and implementation.")
                    .totalCopies(2)
                    .availableCopies(2)
                    .status(BookStatus.AVAILABLE)
                    .branch(branchHyderabad)
                    .build());

            bookRepository.save(Book.builder()
                    .bookCode("BK-DATA-001")
                    .isbn("9781449373320")
                    .title("Designing Data-Intensive Applications")
                    .author("Martin Kleppmann")
                    .category("Engineering")
                    .description("The big ideas behind reliable, scalable, and maintainable data systems.")
                    .totalCopies(3)
                    .availableCopies(3)
                    .status(BookStatus.AVAILABLE)
                    .branch(branchVizag)
                    .build());

            bookRepository.save(Book.builder()
                    .bookCode("BK-ARCH-001")
                    .isbn("9780134494166")
                    .title("Clean Architecture")
                    .author("Robert C. Martin")
                    .category("Software Design")
                    .description("Principles and practices for building maintainable software systems.")
                    .totalCopies(6)
                    .availableCopies(6)
                    .status(BookStatus.AVAILABLE)
                    .branch(branchGuntur)
                    .build());

            log.info("Seeding completed successfully.");
        }
    }
}
