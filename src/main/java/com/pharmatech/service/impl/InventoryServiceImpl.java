package com.pharmatech.service.impl;

import com.pharmatech.dto.InventoryItemDTO;
import com.pharmatech.dto.MedicineRequest;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.model.Inventory;
import com.pharmatech.model.Medicine;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.repository.InventoryRepository;
import com.pharmatech.repository.MedicineRepository;
import com.pharmatech.repository.PharmacyRepository;
import com.pharmatech.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service implementation for Pharmacy Inventory Management.
 * Demonstrates METHOD OVERLOADING, COLLECTIONS (List, Streams), and ABSTRACTION.
 */
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                PharmacyRepository pharmacyRepository,
                                MedicineRepository medicineRepository) {
        this.inventoryRepository = inventoryRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
    }

    @Override
    public List<InventoryItemDTO> getPharmacyInventory(Long pharmacyId) {
        validatePharmacyExists(pharmacyId);
        return inventoryRepository.findByPharmacyId(pharmacyId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItemDTO> searchPharmacyInventory(Long pharmacyId, String query) {
        validatePharmacyExists(pharmacyId);
        if (query == null || query.trim().isEmpty()) {
            return getPharmacyInventory(pharmacyId);
        }
        return inventoryRepository.searchPharmacyInventory(pharmacyId, query.trim())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InventoryItemDTO getInventoryItemById(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", inventoryId));
        return convertToDTO(inventory);
    }

    /**
     * Method Overloading Signature 1: Adds medicine to inventory from MedicineRequest DTO.
     */
    @Override
    public InventoryItemDTO addMedicineToInventory(Long pharmacyId, MedicineRequest request) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", "id", pharmacyId));

        Medicine medicine;
        if (request.getMedicineId() != null) {
            medicine = medicineRepository.findById(request.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", request.getMedicineId()));
        } else {
            // Find existing matching medicine or create new catalog entry
            Optional<Medicine> existing = medicineRepository.findByNameIgnoreCaseAndStrengthIgnoreCase(
                    request.getName().trim(), request.getStrength().trim()
            );

            if (existing.isPresent()) {
                medicine = existing.get();
            } else {
                medicine = new Medicine(
                        request.getName().trim(),
                        request.getBrand().trim(),
                        request.getStrength().trim(),
                        request.getCategory().trim(),
                        request.getDescription() != null ? request.getDescription().trim() : "",
                        request.getPrice(),
                        request.isPrescriptionRequired(),
                        request.getImageUrl()
                );
                medicine = medicineRepository.save(medicine);
            }
        }

        // Check if inventory record already exists for this pharmacy and medicine
        Optional<Inventory> existingInv = inventoryRepository.findByPharmacyIdAndMedicineId(pharmacyId, medicine.getId());
        Inventory inventory;
        if (existingInv.isPresent()) {
            inventory = existingInv.get();
            inventory.addStock(request.getQuantity());
            inventory.setPrice(request.getPrice());
            if (request.getExpiryDate() != null) {
                inventory.setExpiryDate(request.getExpiryDate());
            }
            if (request.getBatchNumber() != null) {
                inventory.setBatchNumber(request.getBatchNumber());
            }
        } else {
            LocalDate expDate = request.getExpiryDate() != null ? request.getExpiryDate() : LocalDate.now().plusMonths(18);
            String batchNum = request.getBatchNumber() != null && !request.getBatchNumber().isEmpty()
                    ? request.getBatchNumber()
                    : "BAT-" + System.currentTimeMillis() % 100000;

            inventory = new Inventory(pharmacy, medicine, request.getQuantity(), request.getPrice(), expDate, batchNum);
        }

        Inventory saved = inventoryRepository.save(inventory);
        return convertToDTO(saved);
    }

    /**
     * Method Overloading Signature 2: Adds medicine to inventory with discrete primitive parameters.
     */
    @Override
    public InventoryItemDTO addMedicineToInventory(Long pharmacyId, Long medicineId, Integer quantity,
                                                   BigDecimal price, LocalDate expiryDate, String batchNumber) {
        MedicineRequest request = new MedicineRequest();
        request.setMedicineId(medicineId);
        request.setQuantity(quantity);
        request.setPrice(price);
        request.setExpiryDate(expiryDate);
        request.setBatchNumber(batchNumber);
        return addMedicineToInventory(pharmacyId, request);
    }

    @Override
    public InventoryItemDTO updateInventoryItem(Long inventoryId, MedicineRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item", "id", inventoryId));

        if (request.getPrice() != null && request.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            inventory.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            inventory.setQuantity(request.getQuantity());
        }
        if (request.getExpiryDate() != null) {
            inventory.setExpiryDate(request.getExpiryDate());
        }
        if (request.getBatchNumber() != null && !request.getBatchNumber().trim().isEmpty()) {
            inventory.setBatchNumber(request.getBatchNumber().trim());
        }

        // Also update medicine catalog details if modified
        Medicine medicine = inventory.getMedicine();
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            medicine.setName(request.getName().trim());
        }
        if (request.getBrand() != null && !request.getBrand().trim().isEmpty()) {
            medicine.setBrand(request.getBrand().trim());
        }
        if (request.getStrength() != null && !request.getStrength().trim().isEmpty()) {
            medicine.setStrength(request.getStrength().trim());
        }
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            medicine.setCategory(request.getCategory().trim());
        }
        medicineRepository.save(medicine);

        inventory.setLastUpdated(LocalDateTime.now());
        Inventory updated = inventoryRepository.save(inventory);
        return convertToDTO(updated);
    }

    @Override
    public void deleteInventoryItem(Long inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new ResourceNotFoundException("Inventory item", "id", inventoryId);
        }
        inventoryRepository.deleteById(inventoryId);
    }

    @Override
    public List<InventoryItemDTO> getAvailableProductsForCustomer(String query, String category) {
        return getAvailableProductsForCustomer(query, category, "Coimbatore");
    }

    @Override
    public List<InventoryItemDTO> getAvailableProductsForCustomer(String query, String category, String city) {
        String targetCity = (city != null && !city.trim().isEmpty()) ? city.trim() : "Coimbatore";
        return inventoryRepository.findAll().stream()
                .filter(i -> i.isAvailable() && i.getQuantity() > 0)
                .filter(i -> i.getPharmacy() == null || i.getPharmacy().getCity() == null ||
                        i.getPharmacy().getCity().equalsIgnoreCase(targetCity))
                .filter(i -> {
                    if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) {
                        return true;
                    }
                    return i.getMedicine().getCategory().equalsIgnoreCase(category.trim());
                })
                .filter(i -> {
                    if (query == null || query.trim().isEmpty()) {
                        return true;
                    }
                    String q = query.trim().toLowerCase();
                    return i.getMedicine().getName().toLowerCase().contains(q) ||
                           i.getMedicine().getBrand().toLowerCase().contains(q);
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validatePharmacyExists(Long pharmacyId) {
        if (!pharmacyRepository.existsById(pharmacyId)) {
            throw new ResourceNotFoundException("Pharmacy", "id", pharmacyId);
        }
    }

    private InventoryItemDTO convertToDTO(Inventory inventory) {
        InventoryItemDTO dto = new InventoryItemDTO();
        dto.setInventoryId(inventory.getId());
        dto.setQuantity(inventory.getQuantity());
        dto.setPrice(inventory.getPrice());
        dto.setExpiryDate(inventory.getExpiryDate());
        dto.setBatchNumber(inventory.getBatchNumber());
        dto.setAvailable(inventory.isAvailable());
        dto.setLowStock(inventory.isLowStock());
        dto.setLastUpdated(inventory.getLastUpdated());

        if (inventory.getMedicine() != null) {
            Medicine m = inventory.getMedicine();
            dto.setMedicineId(m.getId());
            dto.setMedicineName(m.getName());
            dto.setBrand(m.getBrand());
            dto.setStrength(m.getStrength());
            dto.setCategory(m.getCategory());
            dto.setDescription(m.getDescription());
            dto.setPrescriptionRequired(m.isPrescriptionRequired());
            dto.setImageUrl(m.getImageUrl());
        }

        if (inventory.getPharmacy() != null) {
            Pharmacy p = inventory.getPharmacy();
            dto.setPharmacyId(p.getId());
            dto.setPharmacyName(p.getName());
            dto.setPharmacyAddress(p.getAddress() + ", " + p.getCity());
            dto.setLatitude(p.getLatitude());
            dto.setLongitude(p.getLongitude());
            
            double dist = 5.0;
            if (p.getName() != null) {
                String n = p.getName().toLowerCase();
                if (n.contains("shree lakshmi")) dist = 7.1;
                else if (n.contains("kmch")) dist = 2.4;
                else if (n.contains("apollo")) dist = 3.5;
                else if (n.contains("medplus")) dist = 4.8;
                else if (n.contains("fortis")) dist = 6.2;
                else if (n.contains("carefirst")) dist = 8.5;
            }
            dto.setDistanceKm(dist);
        }

        return dto;
    }
}
