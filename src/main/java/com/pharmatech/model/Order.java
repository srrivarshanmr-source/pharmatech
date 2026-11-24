package com.pharmatech.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing customer medicine orders fulfilling delivery dispatch.
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 60)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_partner_id")
    private DeliveryPartner deliveryPartner;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "delivery_address", length = 255)
    private String deliveryAddress;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "ORDER_PLACED"; // ORDER_PLACED, PREPARING, READY_FOR_PICKUP, PARTNER_ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED

    @Column(name = "priority", nullable = false, length = 20)
    private String priority = "NORMAL"; // NORMAL, EMERGENCY

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "COD"; // COD, ONLINE, UPI

    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "PENDING"; // PENDING, PAID, DEMO_PAID, REFUNDED

    @Column(name = "payment_mode", length = 30)
    private String paymentMode = "DEMO"; // DEMO, REAL

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Lob
    @Column(name = "payment_proof_data", columnDefinition = "CLOB")
    private String paymentProofData;

    @Column(name = "payment_proof_file_name", length = 255)
    private String paymentProofFileName;

    @Column(name = "payment_proof_uploaded_at")
    private LocalDateTime paymentProofUploadedAt;

    @Column(name = "payment_proof_expires_at")
    private LocalDateTime paymentProofExpiresAt;

    @Column(name = "payment_utr", length = 100)
    private String paymentUtr;

    @Column(name = "payment_approval_status", length = 50)
    private String paymentApprovalStatus = "NONE"; // NONE, PENDING_PROOF, PENDING_VERIFICATION, APPROVED, REJECTED, EXPIRED

    @Column(name = "payment_approved_at")
    private LocalDateTime paymentApprovedAt;

    @Column(name = "payment_reject_reason", length = 255)
    private String paymentRejectReason;

    @Column(name = "customer_latitude")
    private Double customerLatitude;

    @Column(name = "customer_longitude")
    private Double customerLongitude;

    @Column(name = "delivery_distance_km")
    private Double deliveryDistanceKm = 2.5;

    @Column(name = "estimated_delivery_minutes")
    private Integer estimatedDeliveryMinutes = 20;

    @Column(name = "delivery_earnings", precision = 10, scale = 2)
    private BigDecimal deliveryEarnings = new BigDecimal("45.00");

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(String orderNumber, Customer customer, Pharmacy pharmacy, DeliveryPartner deliveryPartner,
                 String customerName, String customerPhone, String deliveryAddress,
                 BigDecimal totalAmount, String status, String paymentMethod) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.pharmacy = pharmacy;
        this.deliveryPartner = deliveryPartner;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.priority = "NORMAL";
        this.paymentStatus = "COD".equalsIgnoreCase(paymentMethod) ? "PENDING" : "PAID";
        this.createdAt = LocalDateTime.now();
    }

    public Order(String orderNumber, Customer customer, Pharmacy pharmacy, DeliveryPartner deliveryPartner,
                 String customerName, String customerPhone, String deliveryAddress,
                 BigDecimal totalAmount, String status, String priority, String paymentMethod, String paymentStatus,
                 Double customerLatitude, Double customerLongitude, Double deliveryDistanceKm,
                 Integer estimatedDeliveryMinutes, BigDecimal deliveryEarnings) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.pharmacy = pharmacy;
        this.deliveryPartner = deliveryPartner;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
        this.status = status;
        this.priority = priority;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.customerLatitude = customerLatitude;
        this.customerLongitude = customerLongitude;
        this.deliveryDistanceKm = deliveryDistanceKm;
        this.estimatedDeliveryMinutes = estimatedDeliveryMinutes;
        this.deliveryEarnings = deliveryEarnings;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public DeliveryPartner getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
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

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Double getCustomerLatitude() {
        return customerLatitude;
    }

    public void setCustomerLatitude(Double customerLatitude) {
        this.customerLatitude = customerLatitude;
    }

    public Double getCustomerLongitude() {
        return customerLongitude;
    }

    public void setCustomerLongitude(Double customerLongitude) {
        this.customerLongitude = customerLongitude;
    }

    public Double getDeliveryDistanceKm() {
        return deliveryDistanceKm;
    }

    public void setDeliveryDistanceKm(Double deliveryDistanceKm) {
        this.deliveryDistanceKm = deliveryDistanceKm;
    }

    public Integer getEstimatedDeliveryMinutes() {
        return estimatedDeliveryMinutes;
    }

    public void setEstimatedDeliveryMinutes(Integer estimatedDeliveryMinutes) {
        this.estimatedDeliveryMinutes = estimatedDeliveryMinutes;
    }

    public BigDecimal getDeliveryEarnings() {
        return deliveryEarnings;
    }

    public void setDeliveryEarnings(BigDecimal deliveryEarnings) {
        this.deliveryEarnings = deliveryEarnings;
    }

    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public void setReadyAt(LocalDateTime readyAt) {
        this.readyAt = readyAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public void setPickedUpAt(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getPaymentProofData() {
        return paymentProofData;
    }

    public void setPaymentProofData(String paymentProofData) {
        this.paymentProofData = paymentProofData;
    }

    public String getPaymentProofFileName() {
        return paymentProofFileName;
    }

    public void setPaymentProofFileName(String paymentProofFileName) {
        this.paymentProofFileName = paymentProofFileName;
    }

    public LocalDateTime getPaymentProofUploadedAt() {
        return paymentProofUploadedAt;
    }

    public void setPaymentProofUploadedAt(LocalDateTime paymentProofUploadedAt) {
        this.paymentProofUploadedAt = paymentProofUploadedAt;
    }

    public LocalDateTime getPaymentProofExpiresAt() {
        return paymentProofExpiresAt;
    }

    public void setPaymentProofExpiresAt(LocalDateTime paymentProofExpiresAt) {
        this.paymentProofExpiresAt = paymentProofExpiresAt;
    }

    public String getPaymentUtr() {
        return paymentUtr;
    }

    public void setPaymentUtr(String paymentUtr) {
        this.paymentUtr = paymentUtr;
    }

    public String getPaymentApprovalStatus() {
        return paymentApprovalStatus;
    }

    public void setPaymentApprovalStatus(String paymentApprovalStatus) {
        this.paymentApprovalStatus = paymentApprovalStatus;
    }

    public LocalDateTime getPaymentApprovedAt() {
        return paymentApprovedAt;
    }

    public void setPaymentApprovedAt(LocalDateTime paymentApprovedAt) {
        this.paymentApprovedAt = paymentApprovedAt;
    }

    public String getPaymentRejectReason() {
        return paymentRejectReason;
    }

    public void setPaymentRejectReason(String paymentRejectReason) {
        this.paymentRejectReason = paymentRejectReason;
    }
}
