package com.pharmatech.dto;

import jakarta.validation.constraints.NotNull;

public class ProcessTestPaymentRequestDTO {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private String paymentMethod; // UPI, CARD, COD

    private String action = "CONFIRM"; // CONFIRM, FAIL, CANCEL

    private String upiId;

    private String cardNumber;

    private String cardHolder;

    private String expiryDate;

    private String cvv;

    public ProcessTestPaymentRequestDTO() {}

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
