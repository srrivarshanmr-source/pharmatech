package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks earnings, commission deductions, and net payouts settled to pharmacy partners.
 */
@Entity
@Table(name = "pharmacy_settlement_ledger")
public class PharmacySettlementLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "hibernateLazyInitializer", "handler"})
    private Order order;

    @Column(name = "transaction_reference", nullable = false, length = 100)
    private String transactionReference;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // UPI, CARD, COD

    @Column(name = "gross_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "commission_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionRate;

    @Column(name = "commission_deducted", precision = 10, scale = 2, nullable = false)
    private BigDecimal commissionDeducted;

    @Column(name = "net_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal netAmount;

    @Column(name = "settlement_status", length = 50, nullable = false)
    private String settlementStatus; // DEMO_SETTLED, REVERSED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PharmacySettlementLedger() {}

    public PharmacySettlementLedger(Pharmacy pharmacy, Order order, String transactionReference,
                                    String paymentMethod, BigDecimal grossAmount, BigDecimal commissionRate,
                                    BigDecimal commissionDeducted, BigDecimal netAmount, String settlementStatus) {
        this.pharmacy = pharmacy;
        this.order = order;
        this.transactionReference = transactionReference;
        this.paymentMethod = paymentMethod;
        this.grossAmount = grossAmount;
        this.commissionRate = commissionRate;
        this.commissionDeducted = commissionDeducted;
        this.netAmount = netAmount;
        this.settlementStatus = settlementStatus;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getCommissionDeducted() {
        return commissionDeducted;
    }

    public void setCommissionDeducted(BigDecimal commissionDeducted) {
        this.commissionDeducted = commissionDeducted;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
