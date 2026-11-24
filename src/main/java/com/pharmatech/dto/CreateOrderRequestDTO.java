package com.pharmatech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request payload for creating a customer medicine order.
 */
public class CreateOrderRequestDTO {

    @NotNull(message = "Customer ID is required. Please sign in to place an order.")
    private Long customerId;

    @NotNull(message = "Pharmacy ID is required")
    private Long pharmacyId;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer phone number is required")
    private String customerPhone;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private Double customerLatitude;
    private Double customerLongitude;

    private String priority = "NORMAL"; // NORMAL, EMERGENCY
    private String paymentMethod = "COD"; // COD, ONLINE

    @NotEmpty(message = "At least one medicine item is required")
    private List<OrderItemRequest> items;

    public static class OrderItemRequest {
        @NotNull(message = "Inventory ID is required")
        private Long inventoryId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        public OrderItemRequest() {}

        public OrderItemRequest(Long inventoryId, Integer quantity) {
            this.inventoryId = inventoryId;
            this.quantity = quantity;
        }

        public Long getInventoryId() {
            return inventoryId;
        }

        public void setInventoryId(Long inventoryId) {
            this.inventoryId = inventoryId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public CreateOrderRequestDTO() {}

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

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

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
