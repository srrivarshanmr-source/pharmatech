package com.pharmatech.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvoiceParseResultDTO {

    private String invoiceNumber;
    private String supplierName;
    private String fileName;
    private Integer itemsParsed = 0;
    private Integer totalStockAdded = 0;
    private BigDecimal totalInvoiceValue = BigDecimal.ZERO;
    private BigDecimal invoiceTotal = BigDecimal.ZERO;
    private BigDecimal calculatedTotal = BigDecimal.ZERO;
    private boolean totalsMatch = true;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String message;
    private boolean duplicateDetected = false;
    private String duplicateMessage;
    private boolean confirmed = false;
    private List<InvoiceItemDTO> items = new ArrayList<>();

    public InvoiceParseResultDTO() {}

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getItemsParsed() {
        return itemsParsed;
    }

    public void setItemsParsed(Integer itemsParsed) {
        this.itemsParsed = itemsParsed;
    }

    public Integer getTotalStockAdded() {
        return totalStockAdded;
    }

    public void setTotalStockAdded(Integer totalStockAdded) {
        this.totalStockAdded = totalStockAdded;
    }

    public BigDecimal getTotalInvoiceValue() {
        return totalInvoiceValue;
    }

    public void setTotalInvoiceValue(BigDecimal totalInvoiceValue) {
        this.totalInvoiceValue = totalInvoiceValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDuplicateDetected() {
        return duplicateDetected;
    }

    public void setDuplicateDetected(boolean duplicateDetected) {
        this.duplicateDetected = duplicateDetected;
    }

    public String getDuplicateMessage() {
        return duplicateMessage;
    }

    public void setDuplicateMessage(String duplicateMessage) {
        this.duplicateMessage = duplicateMessage;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public List<InvoiceItemDTO> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getInvoiceTotal() {
        return invoiceTotal;
    }

    public void setInvoiceTotal(BigDecimal invoiceTotal) {
        this.invoiceTotal = invoiceTotal;
    }

    public BigDecimal getCalculatedTotal() {
        return calculatedTotal;
    }

    public void setCalculatedTotal(BigDecimal calculatedTotal) {
        this.calculatedTotal = calculatedTotal;
    }

    public boolean isTotalsMatch() {
        return totalsMatch;
    }

    public void setTotalsMatch(boolean totalsMatch) {
        this.totalsMatch = totalsMatch;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
}
