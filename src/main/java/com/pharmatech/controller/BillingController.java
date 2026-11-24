package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.BillRequestDTO;
import com.pharmatech.dto.BillResponseDTO;
import com.pharmatech.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling Pharmacy Counter Billing & POS.
 * Automatically reduces inventory stock when a bill is generated and saved.
 */
@RestController
@RequestMapping("/api/pharmacy/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * Generate & Save a counter bill, reducing stock in database.
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BillResponseDTO>> createBill(@Valid @RequestBody BillRequestDTO request) {
        BillResponseDTO response = billingService.createBill(request);
        return new ResponseEntity<>(ApiResponse.ok("Bill created and medicine stock updated successfully", response), HttpStatus.CREATED);
    }

    /**
     * Get bill details by ID (for print/receipt viewing).
     */
    @GetMapping("/{billId}")
    public ResponseEntity<ApiResponse<BillResponseDTO>> getBillById(@PathVariable Long billId) {
        BillResponseDTO response = billingService.getBillById(billId);
        return ResponseEntity.ok(ApiResponse.ok("Bill fetched", response));
    }

    /**
     * Get recent billing history for a pharmacy.
     */
    @GetMapping("/history/{pharmacyId}")
    public ResponseEntity<ApiResponse<List<BillResponseDTO>>> getBillingHistory(@PathVariable Long pharmacyId) {
        List<BillResponseDTO> bills = billingService.getBillsByPharmacy(pharmacyId);
        return ResponseEntity.ok(ApiResponse.ok("Billing history fetched", bills));
    }

    /**
     * Cancel an existing bill and restore stock to inventory.
     */
    @PostMapping("/{billId}/cancel")
    public ResponseEntity<ApiResponse<BillResponseDTO>> cancelBill(@PathVariable Long billId) {
        BillResponseDTO response = billingService.cancelBill(billId);
        return ResponseEntity.ok(ApiResponse.ok("Bill #" + response.getBillNumber() + " cancelled and stock restored to inventory", response));
    }

    /**
     * Fetch bill by billNumber (for reprinting).
     */
    @GetMapping("/by-number/{billNumber}")
    public ResponseEntity<ApiResponse<BillResponseDTO>> getBillByNumber(@PathVariable String billNumber) {
        BillResponseDTO response = billingService.getBillByNumber(billNumber);
        return ResponseEntity.ok(ApiResponse.ok("Bill fetched", response));
    }
}
