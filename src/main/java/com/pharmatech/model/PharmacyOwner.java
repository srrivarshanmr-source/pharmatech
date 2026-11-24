package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * PharmacyOwner entity demonstrating INHERITANCE (extends User) and POLYMORPHISM (method overriding).
 */
@Entity
@Table(name = "pharmacy_owners")
@PrimaryKeyJoinColumn(name = "user_id")
public class PharmacyOwner extends User {

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "subscription_active", nullable = false)
    private boolean subscriptionActive = false;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("owner")
    private Pharmacy pharmacy;

    // Default Constructor
    public PharmacyOwner() {
        super();
        setRole(Role.ROLE_PHARMACY_OWNER);
    }

    // Parameterized Constructor
    public PharmacyOwner(String fullName, String email, String phoneNumber, String password, String licenseNumber) {
        super(fullName, email, phoneNumber, password, Role.ROLE_PHARMACY_OWNER);
        this.licenseNumber = licenseNumber;
        this.subscriptionActive = false;
    }

    /**
     * Polymorphic method overriding base class User.getUserSummary().
     * Demonstrates RUNTIME POLYMORPHISM.
     */
    @Override
    public String getUserSummary() {
        String pharmacyName = (pharmacy != null) ? pharmacy.getName() : "No Registered Pharmacy";
        return "[Pharmacy Owner] " + getFullName() + " | Pharmacy: " + pharmacyName
                + " | License: " + (licenseNumber != null ? licenseNumber : "N/A")
                + " | Subscription: " + (subscriptionActive ? "ACTIVE" : "INACTIVE");
    }

    // Encapsulated Getters and Setters
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public boolean isSubscriptionActive() {
        return subscriptionActive;
    }

    public void setSubscriptionActive(boolean subscriptionActive) {
        this.subscriptionActive = subscriptionActive;
    }

    public LocalDateTime getSubscriptionExpiresAt() {
        return subscriptionExpiresAt;
    }

    public void setSubscriptionExpiresAt(LocalDateTime subscriptionExpiresAt) {
        this.subscriptionExpiresAt = subscriptionExpiresAt;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
        if (pharmacy != null && pharmacy.getOwner() != this) {
            pharmacy.setOwner(this);
        }
    }
}
