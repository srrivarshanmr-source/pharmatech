package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity tracking physical cash collections by Delivery Partners for Cash on Delivery orders.
 * Maintains independent reconciliation of outstanding cash remittance to the platform.
 */
@Entity
@Table(name = "cash_remittances")
public class CashRemittance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id", nullable = false)
    private DeliveryPartner deliveryPartner;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "amount_collected", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountCollected;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt = LocalDateTime.now();

    @Column(name = "remittance_status", nullable = false, length = 30)
    private String remittanceStatus = "PENDING_REMITTANCE"; // PENDING_REMITTANCE, REMITTED_TO_ADMIN

    @Column(name = "remitted_at")
    private LocalDateTime remittedAt;

    @Column(name = "notes", length = 255)
    private String notes;

    public CashRemittance() {}

    public CashRemittance(DeliveryPartner deliveryPartner, Order order, BigDecimal amountCollected, String notes) {
        this.deliveryPartner = deliveryPartner;
        this.order = order;
        this.amountCollected = amountCollected;
        this.collectedAt = LocalDateTime.now();
        this.remittanceStatus = "PENDING_REMITTANCE";
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public BigDecimal getAmountCollected() {
        return amountCollected;
    }

    public void setAmountCollected(BigDecimal amountCollected) {
        this.amountCollected = amountCollected;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String getRemittanceStatus() {
        return remittanceStatus;
    }

    public void setRemittanceStatus(String remittanceStatus) {
        this.remittanceStatus = remittanceStatus;
    }

    public LocalDateTime getRemittedAt() {
        return remittedAt;
    }

    public void setRemittedAt(LocalDateTime remittedAt) {
        this.remittedAt = remittedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
