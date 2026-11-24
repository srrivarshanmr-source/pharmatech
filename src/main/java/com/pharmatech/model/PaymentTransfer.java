package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Direct payment transfer entity recording marketplace splits to pharmacy owner accounts.
 */
@Entity
@Table(name = "payment_transfers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"idempotency_key"})
})
public class PaymentTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", nullable = false, length = 100, unique = true)
    private String transferId; // e.g. trf_razorpay_99214

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "hibernateLazyInitializer", "handler"})
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    @JsonIgnoreProperties({"owner", "inventories", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_transaction_id")
    @JsonIgnoreProperties({"order", "hibernateLazyInitializer", "handler"})
    private PaymentTransaction sourceTransaction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payout_account_id")
    @JsonIgnoreProperties({"pharmacy", "owner", "hibernateLazyInitializer", "handler"})
    private PharmacyPayoutAccount payoutAccount;

    @Column(name = "gross_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "commission_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal commissionAmount;

    @Column(name = "owner_share_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal ownerShareAmount;

    @Column(name = "status", length = 40, nullable = false)
    private String status = "SETTLEMENT_PENDING"; // SETTLEMENT_PENDING, PROCESSED, REVERSED, FAILED

    @Column(name = "idempotency_key", length = 120, nullable = false)
    private String idempotencyKey;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    public PaymentTransfer() {}

    public PaymentTransfer(String transferId, Order order, Pharmacy pharmacy,
                           PaymentTransaction sourceTransaction, PharmacyPayoutAccount payoutAccount,
                           BigDecimal grossAmount, BigDecimal commissionAmount, BigDecimal ownerShareAmount,
                           String status, String idempotencyKey, String notes) {
        this.transferId = transferId;
        this.order = order;
        this.pharmacy = pharmacy;
        this.sourceTransaction = sourceTransaction;
        this.payoutAccount = payoutAccount;
        this.grossAmount = grossAmount;
        this.commissionAmount = commissionAmount;
        this.ownerShareAmount = ownerShareAmount;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        if ("PROCESSED".equalsIgnoreCase(status)) {
            this.settledAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public PaymentTransaction getSourceTransaction() {
        return sourceTransaction;
    }

    public void setSourceTransaction(PaymentTransaction sourceTransaction) {
        this.sourceTransaction = sourceTransaction;
    }

    public PharmacyPayoutAccount getPayoutAccount() {
        return payoutAccount;
    }

    public void setPayoutAccount(PharmacyPayoutAccount payoutAccount) {
        this.payoutAccount = payoutAccount;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public BigDecimal getOwnerShareAmount() {
        return ownerShareAmount;
    }

    public void setOwnerShareAmount(BigDecimal ownerShareAmount) {
        this.ownerShareAmount = ownerShareAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("PROCESSED".equalsIgnoreCase(status) && this.settledAt == null) {
            this.settledAt = LocalDateTime.now();
        }
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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
