package com.pharmatech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Medicine entity representing healthcare products and drugs cataloged in PharmaTech.
 */
@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Medicine name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Brand is required")
    @Column(nullable = false, length = 100)
    private String brand;

    @NotBlank(message = "Strength/Dosage is required")
    @Column(nullable = false, length = 50)
    private String strength;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 80)
    private String category;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "prescription_required", nullable = false)
    private boolean prescriptionRequired = false;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Default Constructor
    public Medicine() {}

    // Parameterized Constructor
    public Medicine(String name, String brand, String strength, String category,
                    String description, BigDecimal basePrice, boolean prescriptionRequired, String imageUrl) {
        this.name = name;
        this.brand = brand;
        this.strength = strength;
        this.category = category;
        this.description = description;
        this.basePrice = basePrice;
        this.prescriptionRequired = prescriptionRequired;
        this.imageUrl = imageUrl;
    }

    // Encapsulated Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
