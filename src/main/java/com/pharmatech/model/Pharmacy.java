package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pharmacy entity representing a partner pharmacy store registered on PharmaTech.
 */
@Entity
@Table(name = "pharmacies")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Pharmacy name is required")
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank(message = "License number is required")
    @Column(name = "license_number", nullable = false, length = 100)
    private String licenseNumber;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, length = 255)
    private String address;

    @NotBlank(message = "City is required")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "PIN code is required")
    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "rating", nullable = false)
    private Double rating = 4.8;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = true;

    @Column(name = "latitude")
    private Double latitude = 12.9716;

    @Column(name = "longitude")
    private Double longitude = 77.5946;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours = "08:00 AM - 11:00 PM";

    @Column(name = "merchant_upi_id", length = 100)
    private String merchantUpiId = "pharmatech.pay@upi";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private Double distanceKm;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    @JsonIgnore
    private PharmacyOwner owner;

    @OneToMany(mappedBy = "pharmacy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Inventory> inventories = new ArrayList<>();

    // Default Constructor
    public Pharmacy() {}

    // Parameterized Constructor
    public Pharmacy(String name, String licenseNumber, String address, String city, String pincode, String phoneNumber, PharmacyOwner owner) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
        this.phoneNumber = phoneNumber;
        this.owner = owner;
        this.rating = 4.8;
        this.verified = true;
        this.createdAt = LocalDateTime.now();
    }

    public Pharmacy(String name, String licenseNumber, String address, String city, String pincode, String phoneNumber,
                    Double latitude, Double longitude, String operatingHours, String merchantUpiId, PharmacyOwner owner) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.operatingHours = operatingHours;
        this.merchantUpiId = merchantUpiId;
        this.owner = owner;
        this.rating = 4.8;
        this.verified = true;
        this.createdAt = LocalDateTime.now();
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

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PharmacyOwner getOwner() {
        return owner;
    }

    public void setOwner(PharmacyOwner owner) {
        this.owner = owner;
    }

    public List<Inventory> getInventories() {
        return inventories;
    }

    public void setInventories(List<Inventory> inventories) {
        this.inventories = inventories;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(String operatingHours) {
        this.operatingHours = operatingHours;
    }

    public String getMerchantUpiId() {
        return merchantUpiId;
    }

    public void setMerchantUpiId(String merchantUpiId) {
        this.merchantUpiId = merchantUpiId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
