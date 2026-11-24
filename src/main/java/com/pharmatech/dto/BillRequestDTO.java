package com.pharmatech.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class BillRequestDTO {

    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;

    private String customerName;
    private String customerPhone;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String paymentMode = "CASH"; // CASH, UPI, CARD

    @NotEmpty(message = "At least one item is required to generate bill")
    @Valid
    private List<BillItemRequestDTO> items;

    public BillRequestDTO() {}

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<BillItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<BillItemRequestDTO> items) {
        this.items = items;
    }
}
