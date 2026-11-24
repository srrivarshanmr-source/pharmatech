package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Subscription entity representing partner membership plans for pharmacy owners.
 */
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"pharmacy", "password"})
    private PharmacyOwner owner;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(nullable = false, length = 30)
    private String status; // ACTIVE, PENDING, EXPIRED

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    // Default Constructor
    public Subscription() {}

    // Parameterized Constructor
    public Subscription(PharmacyOwner owner, String planName, BigDecimal monthlyPrice,
                        String status, LocalDateTime startDate, LocalDateTime endDate, String transactionReference) {
        this.owner = owner;
        this.planName = planName;
        this.monthlyPrice = monthlyPrice;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.transactionReference = transactionReference;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status) &&
                endDate != null && endDate.isAfter(LocalDateTime.now());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PharmacyOwner getOwner() {
        return owner;
    }

    public void setOwner(PharmacyOwner owner) {
        this.owner = owner;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
