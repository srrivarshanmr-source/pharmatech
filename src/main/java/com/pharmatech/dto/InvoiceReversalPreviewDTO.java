package com.pharmatech.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvoiceReversalPreviewDTO {

    private String invoiceNumber;
    private Long pharmacyId;
    private String pharmacyName;
    private Integer itemsCount = 0;
    private Integer totalStockToRevert = 0;
    private BigDecimal totalValue = BigDecimal.ZERO;
    private String status;
    private boolean reversible = true;
    private String message;
    private List<ReversalItemDetail> affectedItems = new ArrayList<>();

    public static class ReversalItemDetail {
        private String medicineName;
        private String batchNumber;
        private Integer currentStock;
        private Integer quantityToDeduct;
        private Integer projectedStock;
        private BigDecimal unitPrice;

        public ReversalItemDetail() {}

        public ReversalItemDetail(String medicineName, String batchNumber, Integer currentStock,
                                  Integer quantityToDeduct, Integer projectedStock, BigDecimal unitPrice) {
            this.medicineName = medicineName;
            this.batchNumber = batchNumber;
            this.currentStock = currentStock;
            this.quantityToDeduct = quantityToDeduct;
            this.projectedStock = projectedStock;
            this.unitPrice = unitPrice;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public String getBatchNumber() {
            return batchNumber;
        }

        public void setBatchNumber(String batchNumber) {
            this.batchNumber = batchNumber;
        }

        public Integer getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(Integer currentStock) {
            this.currentStock = currentStock;
        }

        public Integer getQuantityToDeduct() {
            return quantityToDeduct;
        }

        public void setQuantityToDeduct(Integer quantityToDeduct) {
            this.quantityToDeduct = quantityToDeduct;
        }

        public Integer getProjectedStock() {
            return projectedStock;
        }

        public void setProjectedStock(Integer projectedStock) {
            this.projectedStock = projectedStock;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    public InvoiceReversalPreviewDTO() {}

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public Integer getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    public Integer getTotalStockToRevert() {
        return totalStockToRevert;
    }

    public void setTotalStockToRevert(Integer totalStockToRevert) {
        this.totalStockToRevert = totalStockToRevert;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isReversible() {
        return reversible;
    }

    public void setReversible(boolean reversible) {
        this.reversible = reversible;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ReversalItemDetail> getAffectedItems() {
        return affectedItems;
    }

    public void setAffectedItems(List<ReversalItemDetail> affectedItems) {
        this.affectedItems = affectedItems;
    }
}
