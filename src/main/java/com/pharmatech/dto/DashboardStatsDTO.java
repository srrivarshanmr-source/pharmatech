package com.pharmatech.dto;

import java.util.List;

public class DashboardStatsDTO {

    private long totalMedicines;
    private long availableMedicines;
    private long lowStockMedicines;
    private long outOfStockMedicines;
    private long pendingOrders;
    private String pharmacyName;
    private String ownerName;
    private String licenseNumber;
    private String address;
    private String city;
    private String pincode;
    private boolean subscriptionActive;
    private List<String> recentActivity;

    public DashboardStatsDTO() {}

    public long getTotalMedicines() {
        return totalMedicines;
    }

    public void setTotalMedicines(long totalMedicines) {
        this.totalMedicines = totalMedicines;
    }

    public long getAvailableMedicines() {
        return availableMedicines;
    }

    public void setAvailableMedicines(long availableMedicines) {
        this.availableMedicines = availableMedicines;
    }

    public long getLowStockMedicines() {
        return lowStockMedicines;
    }

    public void setLowStockMedicines(long lowStockMedicines) {
        this.lowStockMedicines = lowStockMedicines;
    }

    public long getOutOfStockMedicines() {
        return outOfStockMedicines;
    }

    public void setOutOfStockMedicines(long outOfStockMedicines) {
        this.outOfStockMedicines = outOfStockMedicines;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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

    public boolean isSubscriptionActive() {
        return subscriptionActive;
    }

    public void setSubscriptionActive(boolean subscriptionActive) {
        this.subscriptionActive = subscriptionActive;
    }

    public List<String> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<String> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
