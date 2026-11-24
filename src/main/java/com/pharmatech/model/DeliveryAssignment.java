package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks delivery dispatch requests sent to delivery riders with acceptance/rejection timestamps.
 */
@Entity
@Table(name = "delivery_assignments")
public class DeliveryAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "customer", "pharmacy", "deliveryPartner", "hibernateLazyInitializer", "handler"})
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_partner_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DeliveryPartner deliveryPartner;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, ACCEPTED, REJECTED, EXPIRED, CANCELLED

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    public DeliveryAssignment() {}

    public DeliveryAssignment(Order order, DeliveryPartner deliveryPartner, String status) {
        this.order = order;
        this.deliveryPartner = deliveryPartner;
        this.status = status;
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
