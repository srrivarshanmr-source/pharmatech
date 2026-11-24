package com.pharmatech.service;

import com.pharmatech.dto.InventoryItemDTO;
import com.pharmatech.dto.MedicineRequest;
import com.pharmatech.model.Inventory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Inventory service interface demonstrating ABSTRACTION and METHOD OVERLOADING.
 */
public interface InventoryService {

    List<InventoryItemDTO> getPharmacyInventory(Long pharmacyId);

    List<InventoryItemDTO> searchPharmacyInventory(Long pharmacyId, String query);

    InventoryItemDTO getInventoryItemById(Long inventoryId);

    /**
     * Add inventory method 1 (Overloaded)
     * Adds an item using the full MedicineRequest payload.
     */
    InventoryItemDTO addMedicineToInventory(Long pharmacyId, MedicineRequest request);

    /**
     * Add inventory method 2 (Overloaded)
     * Adds an item with direct parameters.
     */
    InventoryItemDTO addMedicineToInventory(Long pharmacyId, Long medicineId, Integer quantity, BigDecimal price, LocalDate expiryDate, String batchNumber);

    InventoryItemDTO updateInventoryItem(Long inventoryId, MedicineRequest request);

    void deleteInventoryItem(Long inventoryId);

    List<InventoryItemDTO> getAvailableProductsForCustomer(String query, String category);

    List<InventoryItemDTO> getAvailableProductsForCustomer(String query, String category, String city);
}
