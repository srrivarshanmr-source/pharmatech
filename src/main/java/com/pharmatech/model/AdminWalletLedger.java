package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Internal Admin Demo Wallet Ledger tracking all customer prepaid and COD payments,
 * platform commission deduction, and settlement allocations.
 */
@Entity
@Table(name = "admin_wallet_ledger")
public class AdminWalletLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, length = 100)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "hibernateLazyInitializer", "handler"})
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacy_id")
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // UPI, CARD, COD

    @Column(name = "gross_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "commission_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionRate; // e.g. 5.00 (%)

    @Column(name = "commission_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal commissionAmount;

    @Column(name = "net_settlement_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal netSettlementAmount;

    @Column(name = "payment_status", length = 50, nullable = false)
    private String paymentStatus; // TEST_SUCCESS, COD_COLLECTED, COD_PENDING, TEST_FAILED, TEST_CANCELLED

    @Column(name = "settlement_status", length = 50, nullable = false)
    private String settlementStatus; // DEMO_SETTLED, PENDING_SETTLEMENT, REVERSED, NONE

    @Column(name = "masked_identifier", length = 100)
    private String maskedIdentifier; // Masked UPI ID or Masked Card Last 4

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public AdminWalletLedger() {}

    public AdminWalletLedger(String transactionReference, Order order, Customer customer, Pharmacy pharmacy,
                             String paymentMethod, BigDecimal grossAmount, BigDecimal commissionRate,
                             BigDecimal commissionAmount, BigDecimal netSettlementAmount,
                             String paymentStatus, String settlementStatus, String maskedIdentifier, String notes) {
        this.transactionReference = transactionReference;
        this.order = order;
        this.customer = customer;
        this.pharmacy = pharmacy;
        this.paymentMethod = paymentMethod;
        this.grossAmount = grossAmount;
        this.commissionRate = commissionRate;
        this.commissionAmount = commissionAmount;
        this.netSettlementAmount = netSettlementAmount;
        this.paymentStatus = paymentStatus;
        this.settlementStatus = settlementStatus;
        this.maskedIdentifier = maskedIdentifier;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        if ("DEMO_SETTLED".equalsIgnoreCase(settlementStatus)) {
            this.settledAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
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

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getNetSettlementAmount() {
        return netSettlementAmount;
    }

    public void setNetSettlementAmount(BigDecimal netSettlementAmount) {
        this.netSettlementAmount = netSettlementAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public String getMaskedIdentifier() {
        return maskedIdentifier;
    }

    public void setMaskedIdentifier(String maskedIdentifier) {
        this.maskedIdentifier = maskedIdentifier;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }
}
