package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.AuthResponse;
import com.pharmatech.dto.LoginRequest;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller handling Delivery Partner Authentication, Dispatch,
 * Accept/Reject Popups, Status Transitions, and Pay on Delivery Collection.
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Delivery Partner Login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = deliveryService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Delivery partner login successful", response));
    }

    /**
     * Delivery Dashboard metrics & assigned deliveries
     */
    @GetMapping("/dashboard/{partnerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@PathVariable Long partnerId) {
        Map<String, Object> data = deliveryService.getDeliveryDashboard(partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Delivery dashboard data loaded", data));
    }

    /**
     * Get all assigned orders
     */
    @GetMapping("/orders/{partnerId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getOrders(@PathVariable Long partnerId) {
        List<OrderResponseDTO> orders = deliveryService.getAssignedOrders(partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Assigned orders fetched", orders));
    }

    /**
     * Get pending delivery requests for popup notifications (EMERGENCY prioritized at top).
     */
    @GetMapping("/pending-requests/{partnerId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getPendingRequests(@PathVariable Long partnerId) {
        List<OrderResponseDTO> pending = deliveryService.getPendingDeliveryRequests(partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Pending delivery requests fetched", pending));
    }

    /**
     * Accept a delivery request.
     */
    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> acceptDelivery(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body) {
        Long partnerId = Long.valueOf(body.get("partnerId").toString());
        OrderResponseDTO accepted = deliveryService.acceptDelivery(orderId, partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Delivery request accepted successfully", accepted));
    }

    /**
     * Reject a delivery request (offers to next available partner).
     */
    @PostMapping("/orders/{orderId}/reject")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> rejectDelivery(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body) {
        Long partnerId = Long.valueOf(body.get("partnerId").toString());
        OrderResponseDTO rejected = deliveryService.rejectDelivery(orderId, partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Delivery request rejected", rejected));
    }

    /**
     * Update order delivery status (e.g. PICKED_UP, OUT_FOR_DELIVERY, DELIVERED)
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Long partnerId = body.get("partnerId") != null ? Long.valueOf(body.get("partnerId").toString()) : null;
        OrderResponseDTO updated = deliveryService.updateOrderStatus(orderId, status, partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Order delivery status updated to " + status, updated));
    }

    /**
     * Toggle Delivery Partner online/offline availability.
     */
    @PutMapping("/{partnerId}/online")
    public ResponseEntity<ApiResponse<DeliveryPartner>> toggleOnline(
            @PathVariable Long partnerId,
            @RequestBody Map<String, Object> body) {
        boolean online = Boolean.parseBoolean(body.get("online").toString());
        DeliveryPartner updated = deliveryService.toggleOnlineStatus(partnerId, online);
        return ResponseEntity.ok(ApiResponse.ok("Partner status updated to " + (online ? "Online" : "Offline"), updated));
    }

    /**
     * Record payment collection for Pay on Delivery orders.
     */
    @PostMapping("/orders/{orderId}/record-payment")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> recordPayment(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long partnerId) {
        if (body != null && body.get("partnerId") != null) {
            partnerId = Long.valueOf(body.get("partnerId").toString());
        }
        String paymentMode = "CASH";
        if (body != null) {
            if (body.get("paymentMode") != null) {
                paymentMode = body.get("paymentMode").toString();
            } else if (body.get("paymentMethod") != null) {
                paymentMode = body.get("paymentMethod").toString();
            }
        }
        OrderResponseDTO updated = deliveryService.recordPayment(orderId, partnerId, paymentMode);
        return ResponseEntity.ok(ApiResponse.ok("Payment recorded successfully as PAID", updated));
    }

    /**
     * Generate unique, single-use payment QR for delivery collection.
     */
    @PostMapping("/orders/{orderId}/generate-qr")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateOrderPaymentQr(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long partnerId) {
        if (body != null && body.get("partnerId") != null) {
            partnerId = Long.valueOf(body.get("partnerId").toString());
        }
        Map<String, Object> result = deliveryService.generateOrderPaymentQr(orderId, partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Payment QR generated", result));
    }

    /**
     * Check payment status of single-use delivery collection QR.
     */
    @GetMapping("/orders/{orderId}/check-qr-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkQrStatus(@PathVariable Long orderId) {
        Map<String, Object> status = deliveryService.checkOrderQrPaymentStatus(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Payment status retrieved", status));
    }

    /**
     * Delivery partner confirms cash collection from customer.
     */
    @PostMapping("/orders/{orderId}/confirm-cash")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> confirmCash(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long partnerId) {
        if (body != null && body.get("partnerId") != null) {
            partnerId = Long.valueOf(body.get("partnerId").toString());
        }
        java.math.BigDecimal amount = null;
        if (body != null && body.get("amount") != null) {
            amount = new java.math.BigDecimal(body.get("amount").toString());
        }
        OrderResponseDTO updated = deliveryService.confirmCashCollected(orderId, partnerId, amount);
        return ResponseEntity.ok(ApiResponse.ok("Cash collected successfully. Added to outstanding cash remittance.", updated));
    }

    /**
     * Complete order delivery handover after payment is confirmed/received.
     */
    @PostMapping("/orders/{orderId}/complete-delivery")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> completeDelivery(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Long partnerId) {
        if (body != null && body.get("partnerId") != null) {
            partnerId = Long.valueOf(body.get("partnerId").toString());
        }
        OrderResponseDTO updated = deliveryService.completeDelivery(orderId, partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Delivery completed successfully", updated));
    }

    /**
     * Get cash remittance summary and outstanding balance for delivery partner.
     */
    @GetMapping("/remittance/{partnerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRemittanceSummary(@PathVariable Long partnerId) {
        Map<String, Object> summary = deliveryService.getCashRemittanceSummary(partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Cash remittance summary retrieved", summary));
    }

    /**
     * Get Delivery Partner Earnings summary and history.
     */
    @GetMapping("/earnings/{partnerId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEarnings(@PathVariable Long partnerId) {
        Map<String, Object> earnings = deliveryService.getEarningsSummary(partnerId);
        return ResponseEntity.ok(ApiResponse.ok("Earnings summary loaded", earnings));
    }

    /**
     * Live GPS coordinates update from active delivery rider
     */
    @PostMapping({"/location-update", "/location/update"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateRiderLocation(
            @RequestBody Map<String, Object> body) {
        Long partnerId = Long.valueOf(body.get("partnerId").toString());
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        Double latitude = Double.valueOf(body.get("latitude").toString());
        Double longitude = Double.valueOf(body.get("longitude").toString());
        Double heading = body.get("heading") != null ? Double.valueOf(body.get("heading").toString()) : 0.0;
        Double speed = body.get("speed") != null ? Double.valueOf(body.get("speed").toString()) : 0.0;

        Map<String, Object> result = deliveryService.recordLocationUpdate(partnerId, orderId, latitude, longitude, heading, speed);
        return ResponseEntity.ok(ApiResponse.ok("Location updated successfully", result));
    }
}
