package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.DashboardStatsDTO;
import com.pharmatech.dto.SubscriptionRequest;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.model.PharmacyOwner;
import com.pharmatech.model.Subscription;
import com.pharmatech.service.PharmacyOwnerService;
import com.pharmatech.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller serving the Pharmacy Owner Interface (Dashboard, Subscription, Profile).
 */
@RestController
@RequestMapping("/api/pharmacy")
public class PharmacyOwnerController {

    private final PharmacyOwnerService pharmacyOwnerService;
    private final SubscriptionService subscriptionService;
    private final com.pharmatech.service.SettlementService settlementService;

    public PharmacyOwnerController(PharmacyOwnerService pharmacyOwnerService,
                                   SubscriptionService subscriptionService,
                                   com.pharmatech.service.SettlementService settlementService) {
        this.pharmacyOwnerService = pharmacyOwnerService;
        this.subscriptionService = subscriptionService;
        this.settlementService = settlementService;
    }

    /**
     * Dashboard Summary Metrics:
     * - Total Medicines
     * - Available Medicines
     * - Low Stock Medicines
     * - Pending Orders
     */
    @GetMapping("/dashboard/{ownerId}")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboard(@PathVariable Long ownerId) {
        DashboardStatsDTO stats = pharmacyOwnerService.getDashboardStats(ownerId);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /**
     * View Pharmacy and Owner Profile.
     */
    @GetMapping("/profile/{ownerId}")
    public ResponseEntity<ApiResponse<PharmacyOwner>> getProfile(@PathVariable Long ownerId) {
        PharmacyOwner profile = pharmacyOwnerService.getOwnerProfile(ownerId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    /**
     * Update Pharmacy Store Details.
     */
    @PutMapping("/profile/{ownerId}")
    public ResponseEntity<ApiResponse<Pharmacy>> updatePharmacyProfile(@PathVariable Long ownerId,
                                                                       @RequestBody Pharmacy pharmacyDetails) {
        Pharmacy updated = pharmacyOwnerService.updatePharmacyProfile(ownerId, pharmacyDetails);
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy details updated successfully", updated));
    }

    /**
     * Simulate Subscription Activation (PharmaTech Pharmacy Partner Plan - ₹200/month).
     */
    @PostMapping("/subscription/activate")
    public ResponseEntity<ApiResponse<Subscription>> activateSubscription(@Valid @RequestBody SubscriptionRequest request) {
        Subscription subscription = subscriptionService.activateSubscription(request);
        return ResponseEntity.ok(ApiResponse.ok("Subscription activated successfully!", subscription));
    }

    /**
     * Check Owner Subscription Status.
     */
    @GetMapping("/subscription/status/{ownerId}")
    public ResponseEntity<ApiResponse<Boolean>> checkSubscriptionStatus(@PathVariable Long ownerId) {
        boolean active = subscriptionService.isSubscriptionActive(ownerId);
        return ResponseEntity.ok(ApiResponse.ok(active));
    }

    /**
     * Get all incoming & active orders for this pharmacy (EMERGENCY prioritized at top).
     */
    @GetMapping("/orders/{pharmacyId}")
    public ResponseEntity<ApiResponse<List<com.pharmatech.dto.OrderResponseDTO>>> getOrders(@PathVariable Long pharmacyId) {
        List<com.pharmatech.dto.OrderResponseDTO> orders = pharmacyOwnerService.getPharmacyOrders(pharmacyId);
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy orders loaded", orders));
    }

    /**
     * Update order preparation status (e.g. PREPARING, READY_FOR_PICKUP).
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<com.pharmatech.dto.OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody java.util.Map<String, Object> body) {
        Long pharmacyId = body.get("pharmacyId") != null ? Long.valueOf(body.get("pharmacyId").toString()) : null;
        String status = (String) body.get("status");
        com.pharmatech.dto.OrderResponseDTO updated = pharmacyOwnerService.updateOrderStatus(orderId, pharmacyId, status);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated to " + status, updated));
    }

    /**
     * Get real-time notification popup and unread count for Pharmacy Owner.
     */
    @GetMapping("/notifications/{pharmacyId}")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getNotifications(@PathVariable Long pharmacyId) {
        java.util.Map<String, Object> notifs = pharmacyOwnerService.getPharmacyNotifications(pharmacyId);
        return ResponseEntity.ok(ApiResponse.ok("Notifications loaded", notifs));
    }

    /**
     * Get Pharmacy Earnings & Settlement history.
     */
    @GetMapping("/earnings/{pharmacyId}")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getPharmacyEarnings(@PathVariable Long pharmacyId) {
        java.util.Map<String, Object> earnings = settlementService.getPharmacyEarningsSummary(pharmacyId);
        return ResponseEntity.ok(ApiResponse.ok("Pharmacy earnings & settlement summary loaded", earnings));
    }
}
