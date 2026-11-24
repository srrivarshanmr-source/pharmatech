package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller providing Admin Dashboard metrics and platform management.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final InvoiceUploadRepository invoiceUploadRepository;
    private final com.pharmatech.service.SettlementService settlementService;
    private final com.pharmatech.service.PaymentService paymentService;

    public AdminController(UserRepository userRepository,
                           PharmacyRepository pharmacyRepository,
                           MedicineRepository medicineRepository,
                           DeliveryPartnerRepository deliveryPartnerRepository,
                           BillRepository billRepository,
                           OrderRepository orderRepository,
                           InvoiceUploadRepository invoiceUploadRepository,
                           com.pharmatech.service.SettlementService settlementService,
                           com.pharmatech.service.PaymentService paymentService) {
        this.userRepository = userRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
        this.invoiceUploadRepository = invoiceUploadRepository;
        this.settlementService = settlementService;
        this.paymentService = paymentService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPharmacies", pharmacyRepository.count());
        stats.put("totalMedicines", medicineRepository.count());
        stats.put("totalDeliveryPartners", deliveryPartnerRepository.count());
        stats.put("totalBillsGenerated", billRepository.count());
        stats.put("totalOrdersPlaced", orderRepository.count());
        stats.put("totalInvoicesProcessed", invoiceUploadRepository.count());
        long activeCount = pharmacyRepository.findAll().stream().filter(Pharmacy::isVerified).count();
        stats.put("activePharmacies", activeCount);
        try {
            stats.put("pendingUpiVerifications", paymentService.getPendingUpiVerifications().size());
        } catch (Exception e) {
            stats.put("pendingUpiVerifications", 0);
        }

        return ResponseEntity.ok(ApiResponse.ok("Admin platform statistics loaded", stats));
    }

    @GetMapping("/pharmacies")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllPharmacies() {
        List<Pharmacy> list = pharmacyRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Pharmacy p : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("licenseNumber", p.getLicenseNumber());
            map.put("city", p.getCity());
            map.put("address", p.getAddress());
            map.put("phoneNumber", p.getPhoneNumber());
            map.put("rating", p.getRating());
            map.put("verified", p.isVerified());
            map.put("active", p.isVerified());
            if (p.getOwner() != null) {
                map.put("ownerName", p.getOwner().getFullName());
                map.put("ownerEmail", p.getOwner().getEmail());
                map.put("subscriptionActive", p.getOwner().isSubscriptionActive());
            }
            result.add(map);
        }
        return ResponseEntity.ok(ApiResponse.ok("Pharmacies list fetched", result));
    }

    @GetMapping("/delivery-fleet")
    public ResponseEntity<ApiResponse<Object>> getDeliveryFleet() {
        return ResponseEntity.ok(ApiResponse.ok("Delivery fleet fetched", deliveryPartnerRepository.findAll()));
    }

    /**
     * Get configurable admin commission percentage and display UPI ID.
     */
    @GetMapping("/commission-setting")
    public ResponseEntity<ApiResponse<com.pharmatech.dto.AdminCommissionSettingDTO>> getCommissionSetting() {
        com.pharmatech.dto.AdminCommissionSettingDTO dto = settlementService.getCommissionSetting();
        return ResponseEntity.ok(ApiResponse.ok("Commission configuration loaded", dto));
    }

    /**
     * Update admin commission rate or admin UPI ID.
     */
    @PutMapping("/commission-setting")
    public ResponseEntity<ApiResponse<com.pharmatech.dto.AdminCommissionSettingDTO>> updateCommissionSetting(
            @RequestBody com.pharmatech.dto.AdminCommissionSettingDTO dto) {
        settlementService.updateCommissionSetting(dto);
        return ResponseEntity.ok(ApiResponse.ok("Commission configuration updated successfully", settlementService.getCommissionSetting()));
    }

    /**
     * Get Admin Payment Ledger entries with filters (status, pharmacyId, date, search).
     */
    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<com.pharmatech.dto.AdminPaymentLedgerDTO>>> getAdminPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long pharmacyId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String search) {
        List<com.pharmatech.dto.AdminPaymentLedgerDTO> list = settlementService.getAllAdminLedgerEntries(status, pharmacyId, date, search);
        return ResponseEntity.ok(ApiResponse.ok("Admin payments ledger retrieved", list));
    }

    /**
     * Get Admin Payment Summary KPIs.
     */
    @GetMapping("/payment-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminPaymentSummary() {
        Map<String, Object> summary = settlementService.getAdminPaymentSummary();
        return ResponseEntity.ok(ApiResponse.ok("Admin payment summary retrieved", summary));
    }

    /**
     * Get pending UPI screenshot verification requests.
     */
    @GetMapping("/payments/pending-verifications")
    public ResponseEntity<ApiResponse<List<com.pharmatech.dto.PendingUpiVerificationDTO>>> getPendingUpiVerifications() {
        List<com.pharmatech.dto.PendingUpiVerificationDTO> list = paymentService.getPendingUpiVerifications();
        return ResponseEntity.ok(ApiResponse.ok("Pending UPI verifications loaded", list));
    }

    /**
     * Admin approves payment after verifying against actual received bank transaction.
     * Transitions order to PAID and ORDER_PLACED, sending it to the pharmacy.
     */
    @PostMapping("/payments/{orderId}/approve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approvePayment(@PathVariable Long orderId) {
        Map<String, Object> result = paymentService.approveUpiPayment(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and order approved. Order sent to pharmacy.", result));
    }

    /**
     * Admin rejects payment if transaction is not verified. Customer is notified and can retry.
     */
    @PostMapping("/payments/{orderId}/reject")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectPayment(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> payload) {
        String reason = payload != null && payload.get("reason") != null ? payload.get("reason").toString() : null;
        Map<String, Object> result = paymentService.rejectUpiPayment(orderId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Payment verification rejected. Customer notified to retry.", result));
    }

    /**
     * Automatic Money Transfer from Admin to Respective Pharmacy Owners.
     * Settles all eligible paid and delivered orders, calculating exact owner shares
     * and recording transactions in both Admin and Pharmacy histories.
     */
    @PostMapping("/payments/auto-settle-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> autoSettleAllPending() {
        Map<String, Object> res = settlementService.autoSettleAllPendingOrders();
        return ResponseEntity.ok(ApiResponse.ok("Automatic settlements processed for all eligible orders", res));
    }
}
