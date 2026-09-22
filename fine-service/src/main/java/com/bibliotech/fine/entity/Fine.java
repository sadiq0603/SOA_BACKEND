package com.bibliotech.fine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long rentalId;
    private Long userId;
    private Long bookId;
    private long daysLate;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private FineStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String transactionId;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = FineStatus.UNPAID;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRentalId() { return rentalId; }
    public void setRentalId(Long rentalId) { this.rentalId = rentalId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public long getDaysLate() { return daysLate; }
    public void setDaysLate(long daysLate) { this.daysLate = daysLate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public FineStatus getStatus() { return status; }
    public void setStatus(FineStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getTransactionId() { return transactionId; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
