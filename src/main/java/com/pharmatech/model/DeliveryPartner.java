package com.pharmatech.model;

import jakarta.persistence.*;

/**
 * Delivery Partner entity extending User (Inheritance & Polymorphism).
 * Represents delivery personnel responsible for fulfilling medicine orders.
 */
@Entity
@Table(name = "delivery_partners")
@PrimaryKeyJoinColumn(name = "user_id")
public class DeliveryPartner extends User {

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "driving_license", length = 50)
    private String drivingLicense;

    @Column(name = "assigned_city", length = 100)
    private String assignedCity;

    @Column(name = "is_available")
    private boolean available = true;

    @Column(name = "is_online")
    private boolean online = true;

    @Column(name = "latitude")
    private Double latitude = 12.9716;

    @Column(name = "longitude")
    private Double longitude = 77.5946;

    @Column(name = "current_area", length = 100)
    private String currentArea = "Indiranagar";

    @Column(name = "rating")
    private Double rating = 4.9;

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    public DeliveryPartner() {
        super();
        setRole(Role.ROLE_DELIVERY_PARTNER);
    }

    public DeliveryPartner(String fullName, String email, String phoneNumber, String password,
                           String vehicleNumber, String drivingLicense, String assignedCity) {
        super(fullName, email, phoneNumber, password, Role.ROLE_DELIVERY_PARTNER);
        this.vehicleNumber = vehicleNumber;
        this.drivingLicense = drivingLicense;
        this.assignedCity = assignedCity;
        this.available = true;
        this.online = true;
        this.totalDeliveries = 0;
        this.rating = 4.9;
    }

    public DeliveryPartner(String fullName, String email, String phoneNumber, String password,
                           String vehicleNumber, String drivingLicense, String assignedCity,
                           String currentArea, Double latitude, Double longitude) {
        super(fullName, email, phoneNumber, password, Role.ROLE_DELIVERY_PARTNER);
        this.vehicleNumber = vehicleNumber;
        this.drivingLicense = drivingLicense;
        this.assignedCity = assignedCity;
        this.currentArea = currentArea;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = true;
        this.online = true;
        this.totalDeliveries = 0;
        this.rating = 4.9;
    }

    @Override
    public String getUserSummary() {
        return "[Delivery Partner] " + getFullName() + " | Vehicle: " + (vehicleNumber != null ? vehicleNumber : "N/A")
                + " | Area: " + (currentArea != null ? currentArea : assignedCity)
                + " | " + (online ? (available ? "Online (Available)" : "Online (Busy)") : "Offline");
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getDrivingLicense() {
        return drivingLicense;
    }

    public void setDrivingLicense(String drivingLicense) {
        this.drivingLicense = drivingLicense;
    }

    public String getAssignedCity() {
        return assignedCity;
    }

    public void setAssignedCity(String assignedCity) {
        this.assignedCity = assignedCity;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Integer getTotalDeliveries() {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries) {
        this.totalDeliveries = totalDeliveries;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
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

    public String getCurrentArea() {
        return currentArea;
    }

    public void setCurrentArea(String currentArea) {
        this.currentArea = currentArea;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
