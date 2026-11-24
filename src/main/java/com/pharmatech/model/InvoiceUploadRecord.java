package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record of supplier invoice files uploaded by pharmacy owners to update stock.
 */
@Entity
@Table(name = "invoice_uploads")
public class InvoiceUploadRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    @JsonIgnoreProperties({"inventories", "owner", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @Column(name = "invoice_number", length = 80)
    private String invoiceNumber;

    @Column(name = "file_name", nullable = false, length = 150)
    private String fileName;

    @Column(name = "supplier_name", length = 150)
    private String supplierName;

    @Column(name = "items_count")
    private Integer itemsCount = 0;

    @Column(name = "total_stock_added")
    private Integer totalStockAdded = 0;

    @Column(name = "total_value", precision = 12, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "status", length = 50)
    private String status = "PROCESSED"; // PROCESSED, FAILED

    public InvoiceUploadRecord() {}

    public InvoiceUploadRecord(Pharmacy pharmacy, String invoiceNumber, String fileName,
                               String supplierName, Integer itemsCount, Integer totalStockAdded,
                               BigDecimal totalValue, String status) {
        this.pharmacy = pharmacy;
        this.invoiceNumber = invoiceNumber;
        this.fileName = fileName;
        this.supplierName = supplierName;
        this.itemsCount = itemsCount;
        this.totalStockAdded = totalStockAdded;
        this.totalValue = totalValue != null ? totalValue : BigDecimal.ZERO;
        this.status = status;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Integer getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    public Integer getTotalStockAdded() {
        return totalStockAdded;
    }

    public void setTotalStockAdded(Integer totalStockAdded) {
        this.totalStockAdded = totalStockAdded;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
