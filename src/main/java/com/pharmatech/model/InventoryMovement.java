package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable audit log record for any inventory movement (Purchase, Sale, Reversal, Adjustment).
 */
@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    @JsonIgnoreProperties({"inventories", "owner", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    @JsonIgnoreProperties({"pharmacy", "medicine", "hibernateLazyInitializer", "handler"})
    private Inventory inventory;

    @Column(name = "movement_type", nullable = false, length = 40)
    private String movementType; // INVOICE_PURCHASE, COUNTER_SALE, ONLINE_ORDER, REVERSAL, MANUAL_ADJUSTMENT

    @Column(name = "quantity_delta", nullable = false)
    private Integer quantityDelta;

    @Column(name = "previous_quantity", nullable = false)
    private Integer previousQuantity = 0;

    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity = 0;

    @Column(name = "reference_number", length = 80)
    private String referenceNumber; // e.g. INV-947875, BILL-1234, ORD-5678

    @Column(name = "batch_number", length = 60)
    private String batchNumber;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public InventoryMovement() {}

    public InventoryMovement(Pharmacy pharmacy, Medicine medicine, Inventory inventory,
                             String movementType, Integer quantityDelta, Integer previousQuantity,
                             Integer newQuantity, String referenceNumber, String batchNumber,
                             BigDecimal unitPrice, String notes) {
        this.pharmacy = pharmacy;
        this.medicine = medicine;
        this.inventory = inventory;
        this.movementType = movementType;
        this.quantityDelta = quantityDelta;
        this.previousQuantity = previousQuantity != null ? previousQuantity : 0;
        this.newQuantity = newQuantity != null ? newQuantity : 0;
        this.referenceNumber = referenceNumber;
        this.batchNumber = batchNumber;
        this.unitPrice = unitPrice;
        this.notes = notes;
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

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public Integer getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(Integer quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public Integer getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(Integer previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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
}
