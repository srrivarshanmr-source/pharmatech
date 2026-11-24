package com.pharmatech.dto;

import java.math.BigDecimal;

public class InvoiceItemDTO {

    private String medicineName;
    private String brand;
    private String category;
    private String dosageForm;
    private String strength;
    private String batchNumber;
    private Integer quantity;
    private BigDecimal price;
    private String expiryDate;
    private String actionTaken; // "STOCK_INCREMENTED" or "NEW_ITEM_ADDED"
    private Integer currentStock = 0;
    private Integer newStock = 0;
    private Boolean existingInInventory = false;
    private Boolean needsVerification = false;
    private BigDecimal lineTotal = BigDecimal.ZERO;
    private String validationStatus = "VALID"; // VALID, WARNING_HIGH_QTY, MISMATCHED_TOTAL, NEEDS_VERIFICATION
    private String statusMessage;

    public InvoiceItemDTO() {}

    public InvoiceItemDTO(String medicineName, String brand, String category, String strength,
                          String batchNumber, Integer quantity, BigDecimal price, String expiryDate,
                          String actionTaken) {
        this.medicineName = medicineName;
        this.brand = brand;
        this.category = category;
        this.strength = strength;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.price = price;
        this.expiryDate = expiryDate;
        this.actionTaken = actionTaken;
    }

    public InvoiceItemDTO(String medicineName, String brand, String category, String strength,
                          String batchNumber, Integer quantity, BigDecimal price, String expiryDate,
                          String actionTaken, Integer currentStock, Integer newStock,
                          Boolean existingInInventory, Boolean needsVerification) {
        this.medicineName = medicineName;
        this.brand = brand;
        this.category = category;
        this.strength = strength;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.price = price;
        this.expiryDate = expiryDate;
        this.actionTaken = actionTaken;
        this.currentStock = currentStock != null ? currentStock : 0;
        this.newStock = newStock != null ? newStock : 0;
        this.existingInInventory = existingInInventory != null && existingInInventory;
        this.needsVerification = needsVerification != null && needsVerification;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public Boolean getExistingInInventory() {
        return existingInInventory;
    }

    public void setExistingInInventory(Boolean existingInInventory) {
        this.existingInInventory = existingInInventory;
    }

    public Boolean getNeedsVerification() {
        return needsVerification;
    }

    public void setNeedsVerification(Boolean needsVerification) {
        this.needsVerification = needsVerification;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
