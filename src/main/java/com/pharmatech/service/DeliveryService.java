package com.pharmatech.service;

import com.pharmatech.dto.AuthResponse;
import com.pharmatech.dto.LoginRequest;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.model.DeliveryPartner;

import java.util.List;
import java.util.Map;

public interface DeliveryService {
    AuthResponse login(LoginRequest request);
    Map<String, Object> getDeliveryDashboard(Long partnerId);
    List<OrderResponseDTO> getAssignedOrders(Long partnerId);
    List<OrderResponseDTO> getPendingDeliveryRequests(Long partnerId);
    OrderResponseDTO acceptDelivery(Long orderId, Long partnerId);
    OrderResponseDTO rejectDelivery(Long orderId, Long partnerId);
    OrderResponseDTO updateOrderStatus(Long orderId, String newStatus, Long partnerId);
    OrderResponseDTO recordPayment(Long orderId, Long partnerId, String paymentMode);
    Map<String, Object> generateOrderPaymentQr(Long orderId, Long partnerId);
    Map<String, Object> checkOrderQrPaymentStatus(Long orderId);
    OrderResponseDTO confirmCashCollected(Long orderId, Long partnerId, java.math.BigDecimal amount);
    OrderResponseDTO completeDelivery(Long orderId, Long partnerId);
    Map<String, Object> getCashRemittanceSummary(Long partnerId);
    DeliveryPartner toggleOnlineStatus(Long partnerId, boolean online);
    Map<String, Object> getEarningsSummary(Long partnerId);
    Map<String, Object> recordLocationUpdate(Long partnerId, Long orderId, Double latitude, Double longitude, Double heading, Double speed);
}
