package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tracks payment transactions initiated via Razorpay or Pay on Delivery.
 */
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"items", "customer", "pharmacy", "deliveryPartner", "hibernateLazyInitializer", "handler"})
    private Order order;

    @Column(name = "gateway", nullable = false, length = 40)
    private String gateway; // RAZORPAY, COD

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 150)
    private String razorpaySignature;

    @Column(name = "razorpay_qr_id", length = 100)
    private String razorpayQrId;

    @Column(name = "upi_string", length = 500)
    private String upiString;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "status", nullable = false, length = 40)
    private String status = "CREATED"; // CREATED, PENDING, SUCCESS, DEMO_PAID, FAILED, EXPIRED, REFUNDED

    @Column(name = "payment_mode", length = 30)
    private String paymentMode = "DEMO"; // DEMO, REAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"orders", "hibernateLazyInitializer", "handler"})
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id")
    @JsonIgnoreProperties({"owner", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @Column(name = "payment_method", length = 40)
    private String paymentMethod; // UPI, CARD, COD

    @Column(name = "masked_upi_id", length = 100)
    private String maskedUpiId;

    @Column(name = "card_last4", length = 10)
    private String cardLast4;

    @Column(name = "settlement_status", length = 40)
    private String settlementStatus = "NONE"; // DEMO_SETTLED, PENDING_SETTLEMENT, REVERSED, NONE

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentTransaction() {}

    public PaymentTransaction(Order order, String gateway, String razorpayOrderId, BigDecimal amount, String status) {
        this.order = order;
        this.gateway = gateway;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
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

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRazorpayQrId() {
        return razorpayQrId;
    }

    public void setRazorpayQrId(String razorpayQrId) {
        this.razorpayQrId = razorpayQrId;
    }

    public String getUpiString() {
        return upiString;
    }

    public void setUpiString(String upiString) {
        this.upiString = upiString;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMaskedUpiId() {
        return maskedUpiId;
    }

    public void setMaskedUpiId(String maskedUpiId) {
        this.maskedUpiId = maskedUpiId;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public void setCardLast4(String cardLast4) {
        this.cardLast4 = cardLast4;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }
}
