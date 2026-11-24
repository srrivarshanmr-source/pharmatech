package com.pharmatech.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SubscriptionRequest {

    @NotNull(message = "Owner ID is required")
    private Long ownerId;

    private String planName = "PharmaTech Pharmacy Partner Plan";

    private BigDecimal monthlyPrice = new BigDecimal("200.00");

    private String paymentMethod = "SIMULATED_GATEWAY";

    public SubscriptionRequest() {}

    public SubscriptionRequest(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
