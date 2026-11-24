package com.pharmatech.dto;

import java.math.BigDecimal;

public class AdminCommissionSettingDTO {

    private BigDecimal commissionPercentage;
    private String adminUpiId;

    public AdminCommissionSettingDTO() {}

    public AdminCommissionSettingDTO(BigDecimal commissionPercentage, String adminUpiId) {
        this.commissionPercentage = commissionPercentage;
        this.adminUpiId = adminUpiId;
    }

    public BigDecimal getCommissionPercentage() {
        return commissionPercentage;
    }

    public void setCommissionPercentage(BigDecimal commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }

    public String getAdminUpiId() {
        return adminUpiId;
    }

    public void setAdminUpiId(String adminUpiId) {
        this.adminUpiId = adminUpiId;
    }
}
