package com.pharmatech.model;

import jakarta.persistence.*;

/**
 * Customer entity demonstrating INHERITANCE (extends User) and POLYMORPHISM (method overriding).
 */
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Customer extends User {

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(length = 100)
    private String city;

    @Column(length = 10)
    private String pincode;

    @Column(length = 100)
    private String locality = "Indiranagar";

    @Column(name = "latitude")
    private Double latitude = 12.9716;

    @Column(name = "longitude")
    private Double longitude = 77.5946;

    // Default Constructor
    public Customer() {
        super();
        setRole(Role.ROLE_CUSTOMER);
    }

    // Parameterized Constructor demonstrating Constructor Chaining (super)
    public Customer(String fullName, String email, String phoneNumber, String password,
                    String deliveryAddress, String city, String pincode) {
        super(fullName, email, phoneNumber, password, Role.ROLE_CUSTOMER);
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.pincode = pincode;
        this.locality = city;
        this.latitude = 12.9716;
        this.longitude = 77.5946;
    }

    public Customer(String fullName, String email, String phoneNumber, String password,
                    String deliveryAddress, String city, String pincode, String locality,
                    Double latitude, Double longitude) {
        super(fullName, email, phoneNumber, password, Role.ROLE_CUSTOMER);
        this.deliveryAddress = deliveryAddress;
        this.city = city;
        this.pincode = pincode;
        this.locality = locality;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Polymorphic method overriding base class User.getUserSummary().
     * Demonstrates RUNTIME POLYMORPHISM (Dynamic Method Dispatch).
     */
    @Override
    public String getUserSummary() {
        return "[Customer] " + getFullName() + " | City: " + (city != null ? city : "Not Specified")
                + " | Area: " + (locality != null ? locality : "N/A")
                + " | Address: " + (deliveryAddress != null ? deliveryAddress : "None");
    }

    // Encapsulated Getters and Setters
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
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
}
