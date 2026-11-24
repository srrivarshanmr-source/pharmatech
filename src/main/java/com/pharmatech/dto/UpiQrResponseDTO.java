package com.pharmatech.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpiQrResponseDTO {

    private Long orderId;
    private String orderNumber;
    private Long pharmacyId;
    private String pharmacyName;
    private String customerName;
    private BigDecimal amount;
    private Long amountInSubunits;
    private String currency = "INR";
    private String upiUri;
    private String qrImageUrl;
    private String razorpayQrId;
    private String status; // PENDING, PAID, DEMO_PAID, EXPIRED
    private Integer expiresInSeconds = 600;
    private LocalDateTime expiresAt;
    private boolean isDemo = true;
    private String paymentMode = "DEMO";
    private String demoLabel = "DEMO ONLY – NOT FOR REAL PAYMENT";
    private String qrData;
    private String instructions = "DEMO MODE: Non-payment reference generated for academic demonstration. Click 'Simulate Payment' to approve.";

    public UpiQrResponseDTO() {}

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getAmountInSubunits() {
        return amountInSubunits;
    }

    public void setAmountInSubunits(Long amountInSubunits) {
        this.amountInSubunits = amountInSubunits;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUpiUri() {
        return upiUri;
    }

    public void setUpiUri(String upiUri) {
        this.upiUri = upiUri;
    }

    public String getQrImageUrl() {
        return qrImageUrl;
    }

    public void setQrImageUrl(String qrImageUrl) {
        this.qrImageUrl = qrImageUrl;
    }

    public String getRazorpayQrId() {
        return razorpayQrId;
    }

    public void setRazorpayQrId(String razorpayQrId) {
        this.razorpayQrId = razorpayQrId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(Integer expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getDemoLabel() {
        return demoLabel;
    }

    public void setDemoLabel(String demoLabel) {
        this.demoLabel = demoLabel;
    }

    public String getQrData() {
        return qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }
}
