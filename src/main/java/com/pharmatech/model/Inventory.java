package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inventory entity linking a Pharmacy with a catalog Medicine, holding stock, price, and batch info.
 * Demonstrates ENCAPSULATION of business rules (e.g. low-stock detection, stock adjustments).
 */
@Entity
@Table(name = "inventories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pharmacy_id", "medicine_id", "batch_number"})
})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    @JsonIgnoreProperties({"inventories", "owner"})
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    // Default Constructor
    public Inventory() {}

    // Parameterized Constructor
    public Inventory(Pharmacy pharmacy, Medicine medicine, Integer quantity,
                     BigDecimal price, LocalDate expiryDate, String batchNumber) {
        this.pharmacy = pharmacy;
        this.medicine = medicine;
        this.quantity = quantity;
        this.price = price;
        this.expiryDate = expiryDate;
        this.batchNumber = batchNumber;
        this.available = (quantity != null && quantity > 0);
        this.lastUpdated = LocalDateTime.now();
    }

    // Encapsulated Business Logic Methods
    public boolean isLowStock() {
        return this.quantity != null && this.quantity > 0 && this.quantity <= 15;
    }

    public boolean isOutOfStock() {
        return this.quantity == null || this.quantity <= 0;
    }

    public void updateStock(int newQuantity) {
        this.quantity = Math.max(0, newQuantity);
        this.available = (this.quantity > 0);
        this.lastUpdated = LocalDateTime.now();
    }

    public void addStock(int delta) {
        updateStock((this.quantity != null ? this.quantity : 0) + delta);
    }

    // Getters and Setters
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.available = (quantity != null && quantity > 0);
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
