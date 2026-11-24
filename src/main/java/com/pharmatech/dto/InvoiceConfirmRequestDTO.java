package com.pharmatech.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request payload sent when pharmacy owner confirms the editable preview table
 * to actually commit stock addition to the database.
 */
public class InvoiceConfirmRequestDTO {

    private String invoiceNumber;
    private String supplierName;
    private String fileName;
    private BigDecimal totalValue;
    private List<InvoiceItemDTO> items;

    public InvoiceConfirmRequestDTO() {}

    public InvoiceConfirmRequestDTO(String invoiceNumber, String supplierName, String fileName,
                                   BigDecimal totalValue, List<InvoiceItemDTO> items) {
        this.invoiceNumber = invoiceNumber;
        this.supplierName = supplierName;
        this.fileName = fileName;
        this.totalValue = totalValue;
        this.items = items;
    }

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

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public List<InvoiceItemDTO> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDTO> items) {
        this.items = items;
    }
}
