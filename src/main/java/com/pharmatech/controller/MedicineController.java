package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.InventoryItemDTO;
import com.pharmatech.dto.MedicineRequest;
import com.pharmatech.model.Medicine;
import com.pharmatech.service.InventoryService;
import com.pharmatech.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller handling Medicine and Pharmacy Inventory CRUD operations.
 */
@RestController
@RequestMapping("/api")
public class MedicineController {

    private final InventoryService inventoryService;
    private final MedicineService medicineService;

    public MedicineController(InventoryService inventoryService, MedicineService medicineService) {
        this.inventoryService = inventoryService;
        this.medicineService = medicineService;
    }

    /**
     * View all medicines in a pharmacy's inventory.
     */
    @GetMapping("/inventory/pharmacy/{pharmacyId}")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> getPharmacyInventory(@PathVariable Long pharmacyId) {
        List<InventoryItemDTO> inventory = inventoryService.getPharmacyInventory(pharmacyId);
        return ResponseEntity.ok(ApiResponse.ok(inventory));
    }

    /**
     * Search medicines in a pharmacy's inventory.
     */
    @GetMapping("/inventory/pharmacy/{pharmacyId}/search")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> searchPharmacyInventory(
            @PathVariable Long pharmacyId,
            @RequestParam String query) {
        List<InventoryItemDTO> results = inventoryService.searchPharmacyInventory(pharmacyId, query);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * Get single inventory item by ID.
     */
    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> getInventoryItem(@PathVariable Long inventoryId) {
        InventoryItemDTO item = inventoryService.getInventoryItemById(inventoryId);
        return ResponseEntity.ok(ApiResponse.ok(item));
    }

    /**
     * Add medicine to pharmacy inventory.
     */
    @PostMapping("/inventory/pharmacy/{pharmacyId}")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> addMedicine(
            @PathVariable Long pharmacyId,
            @Valid @RequestBody MedicineRequest request) {
        InventoryItemDTO created = inventoryService.addMedicineToInventory(pharmacyId, request);
        return new ResponseEntity<>(ApiResponse.ok("Medicine added to inventory successfully", created), HttpStatus.CREATED);
    }

    /**
     * Edit medicine in inventory.
     */
    @PutMapping("/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<InventoryItemDTO>> updateMedicine(
            @PathVariable Long inventoryId,
            @Valid @RequestBody MedicineRequest request) {
        InventoryItemDTO updated = inventoryService.updateInventoryItem(inventoryId, request);
        return ResponseEntity.ok(ApiResponse.ok("Medicine updated successfully", updated));
    }

    /**
     * Delete medicine from inventory.
     */
    @DeleteMapping("/inventory/{inventoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long inventoryId) {
        inventoryService.deleteInventoryItem(inventoryId);
        return ResponseEntity.ok(ApiResponse.ok("Medicine deleted from inventory successfully", null));
    }

    /**
     * Master catalog of registered medicines.
     */
    @GetMapping("/medicines/catalog")
    public ResponseEntity<ApiResponse<List<Medicine>>> getCatalog() {
        List<Medicine> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(ApiResponse.ok(medicines));
    }
}
