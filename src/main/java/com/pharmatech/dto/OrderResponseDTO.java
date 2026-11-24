package com.pharmatech.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private Long pharmacyId;
    private String pharmacyName;
    private String pharmacyAddress;
    private String pharmacyPhone;
    private String merchantUpiId;
    private Double pharmacyLatitude;
    private Double pharmacyLongitude;
    private Double customerLatitude;
    private Double customerLongitude;
    private String deliveryPartnerName;
    private String deliveryPartnerPhone;
    private String vehicleNumber;
    private Long deliveryPartnerId;
    private Double deliveryPartnerRating;
    private Double riderLatitude;
    private Double riderLongitude;
    private Double riderHeading;
    private Double riderSpeed;
    private BigDecimal totalAmount;
    private String status; // ORDER_PLACED, PREPARING, READY_FOR_PICKUP, PARTNER_ASSIGNED, PICKED_UP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    private String priority = "NORMAL"; // NORMAL, EMERGENCY
    private String paymentMethod;
    private String paymentStatus = "PENDING"; // PENDING, PAID, DEMO_PAID
    private String paymentMode = "DEMO"; // DEMO, REAL
    private boolean isDemo = true;
    private String paymentProofData;
    private String paymentProofFileName;
    private LocalDateTime paymentProofUploadedAt;
    private LocalDateTime paymentProofExpiresAt;
    private String paymentUtr;
    private String paymentApprovalStatus = "NONE";
    private LocalDateTime paymentApprovedAt;
    private String paymentRejectReason;
    private Long remainingSeconds;
    private Boolean isPaymentExpired;
    private Double deliveryDistanceKm;
    private Integer estimatedDeliveryMinutes;
    private BigDecimal deliveryEarnings;
    private LocalDateTime createdAt;
    private LocalDateTime readyAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemDetailDTO> items;

    public static class OrderItemDetailDTO {
        private String medicineName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public OrderItemDetailDTO() {}

        public OrderItemDetailDTO(String medicineName, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
            this.medicineName = medicineName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public OrderResponseDTO() {}

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

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        this.pharmacyName = pharmacyName;
    }

    public String getPharmacyAddress() {
        return pharmacyAddress;
    }

    public void setPharmacyAddress(String pharmacyAddress) {
        this.pharmacyAddress = pharmacyAddress;
    }

    public String getPharmacyPhone() {
        return pharmacyPhone;
    }

    public void setPharmacyPhone(String pharmacyPhone) {
        this.pharmacyPhone = pharmacyPhone;
    }

    public String getDeliveryPartnerName() {
        return deliveryPartnerName;
    }

    public void setDeliveryPartnerName(String deliveryPartnerName) {
        this.deliveryPartnerName = deliveryPartnerName;
    }

    public String getDeliveryPartnerPhone() {
        return deliveryPartnerPhone;
    }

    public void setDeliveryPartnerPhone(String deliveryPartnerPhone) {
        this.deliveryPartnerPhone = deliveryPartnerPhone;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
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

    public List<OrderItemDetailDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetailDTO> items) {
        this.items = items;
    }

    public Long getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(Long pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getMerchantUpiId() {
        return merchantUpiId;
    }

    public void setMerchantUpiId(String merchantUpiId) {
        this.merchantUpiId = merchantUpiId;
    }

    public Double getPharmacyLatitude() {
        return pharmacyLatitude;
    }

    public void setPharmacyLatitude(Double pharmacyLatitude) {
        this.pharmacyLatitude = pharmacyLatitude;
    }

    public Double getPharmacyLongitude() {
        return pharmacyLongitude;
    }

    public void setPharmacyLongitude(Double pharmacyLongitude) {
        this.pharmacyLongitude = pharmacyLongitude;
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

    public Long getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(Long deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public Double getDeliveryPartnerRating() {
        return deliveryPartnerRating;
    }

    public void setDeliveryPartnerRating(Double deliveryPartnerRating) {
        this.deliveryPartnerRating = deliveryPartnerRating;
    }

    public Double getRiderLatitude() {
        return riderLatitude;
    }

    public void setRiderLatitude(Double riderLatitude) {
        this.riderLatitude = riderLatitude;
    }

    public Double getRiderLongitude() {
        return riderLongitude;
    }

    public void setRiderLongitude(Double riderLongitude) {
        this.riderLongitude = riderLongitude;
    }

    public Double getRiderHeading() {
        return riderHeading;
    }

    public void setRiderHeading(Double riderHeading) {
        this.riderHeading = riderHeading;
    }

    public Double getRiderSpeed() {
        return riderSpeed;
    }

    public void setRiderSpeed(Double riderSpeed) {
        this.riderSpeed = riderSpeed;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public boolean isDemo() {
        return isDemo;
    }

    public void setDemo(boolean demo) {
        isDemo = demo;
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

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public Boolean getIsPaymentExpired() {
        return isPaymentExpired;
    }

    public void setIsPaymentExpired(Boolean isPaymentExpired) {
        this.isPaymentExpired = isPaymentExpired;
    }
}
