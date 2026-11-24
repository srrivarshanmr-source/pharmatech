package com.pharmatech.service.impl;

import com.pharmatech.dto.BillItemRequestDTO;
import com.pharmatech.dto.BillRequestDTO;
import com.pharmatech.dto.BillResponseDTO;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.model.Bill;
import com.pharmatech.model.BillItem;
import com.pharmatech.model.Inventory;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.repository.BillRepository;
import com.pharmatech.repository.InventoryRepository;
import com.pharmatech.repository.PharmacyRepository;
import com.pharmatech.service.BillingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final PharmacyRepository pharmacyRepository;
    private final InventoryRepository inventoryRepository;

    public BillingServiceImpl(BillRepository billRepository,
                              PharmacyRepository pharmacyRepository,
                              InventoryRepository inventoryRepository) {
        this.billRepository = billRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public BillResponseDTO createBill(BillRequestDTO request) {
        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id: " + request.getPharmacyId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Bill must contain at least one medicine item");
        }

        String billNumber = "BILL-" + (System.currentTimeMillis() % 10000000);
        BigDecimal subtotal = BigDecimal.ZERO;
        List<BillItem> billItems = new ArrayList<>();

        // Validate stock availability and deduct atomically
        for (BillItemRequestDTO itemReq : request.getItems()) {
            Inventory inventory = null;
            if (itemReq.getInventoryId() != null) {
                inventory = inventoryRepository.findById(itemReq.getInventoryId())
                        .orElse(null);
            }

            if (inventory == null) {
                // Find by medicine name in this pharmacy
                inventory = inventoryRepository.findByPharmacy(pharmacy).stream()
                        .filter(inv -> inv.getMedicine() != null && inv.getMedicine().getName().equalsIgnoreCase(itemReq.getMedicineName()))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("Medicine '" + itemReq.getMedicineName() + "' is not present in pharmacy inventory"));
            }

            if (!inventory.getPharmacy().getId().equals(pharmacy.getId())) {
                throw new BadRequestException("Inventory item does not belong to this pharmacy");
            }

            int requestedQty = itemReq.getQuantity();
            int currentQty = inventory.getQuantity() != null ? inventory.getQuantity() : 0;

            // Validate Medicine Expiry
            if (inventory.getExpiryDate() != null && inventory.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                throw new BadRequestException("Cannot sell expired medicine: '" + inventory.getMedicine().getName()
                        + "' (Expired on " + inventory.getExpiryDate() + "). Please remove this expired batch from inventory.");
            }

            if (currentQty < requestedQty) {
                throw new BadRequestException("Insufficient stock for '" + inventory.getMedicine().getName()
                        + "'. Available stock: " + currentQty + ", Requested: " + requestedQty);
            }

            // ATOMIC STOCK DEDUCTION
            int updatedQty = currentQty - requestedQty;
            inventory.setQuantity(updatedQty);
            if (updatedQty <= 0) {
                inventory.setAvailable(false);
            }
            inventory.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inventory);

            BigDecimal unitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : inventory.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));
            subtotal = subtotal.add(lineTotal);

            BillItem billItem = new BillItem(
                    inventory.getMedicine(),
                    inventory.getMedicine().getName(),
                    inventory.getBatchNumber(),
                    unitPrice,
                    requestedQty,
                    lineTotal
            );
            billItems.add(billItem);
        }

        // GST Tax calculation (5% standard healthcare GST)
        BigDecimal taxRate = new BigDecimal("0.05");
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal grandTotal = subtotal.add(taxAmount).subtract(discount).setScale(2, RoundingMode.HALF_UP);

        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        Bill bill = new Bill(
                billNumber,
                pharmacy,
                request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty() ? request.getCustomerName().trim() : "Walk-in Customer",
                request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty() ? request.getCustomerPhone().trim() : "N/A",
                subtotal,
                taxAmount,
                discount,
                grandTotal,
                request.getPaymentMode() != null ? request.getPaymentMode() : "CASH"
        );

        for (BillItem item : billItems) {
            bill.addItem(item);
        }

        Bill savedBill = billRepository.save(bill);

        return mapToDTO(savedBill);
    }

    @Override
    public BillResponseDTO getBillById(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));
        return mapToDTO(bill);
    }

    @Override
    public BillResponseDTO getBillByNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with number: " + billNumber));
        return mapToDTO(bill);
    }

    @Override
    public List<BillResponseDTO> getBillsByPharmacy(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id: " + pharmacyId));
        return billRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BillResponseDTO cancelBill(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + billId));

        if ("CANCELLED".equalsIgnoreCase(bill.getStatus())) {
            throw new BadRequestException("Bill #" + bill.getBillNumber() + " is already cancelled / refunded.");
        }

        // Inventory Reversal: Restore stock for each item in the bill
        for (BillItem item : bill.getItems()) {
            if (item.getQuantity() != null && item.getQuantity() > 0) {
                Inventory inventory = null;
                if (item.getMedicine() != null) {
                    inventory = inventoryRepository.findByPharmacy(bill.getPharmacy()).stream()
                            .filter(inv -> inv.getMedicine() != null && inv.getMedicine().getId().equals(item.getMedicine().getId()))
                            .findFirst()
                            .orElse(null);
                }
                if (inventory == null) {
                    inventory = inventoryRepository.findByPharmacy(bill.getPharmacy()).stream()
                            .filter(inv -> inv.getMedicine() != null && inv.getMedicine().getName().equalsIgnoreCase(item.getMedicineName()))
                            .findFirst()
                            .orElse(null);
                }

                if (inventory != null) {
                    inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                    inventory.setAvailable(true);
                    inventory.setLastUpdated(LocalDateTime.now());
                    inventoryRepository.save(inventory);
                }
            }
        }

        bill.setStatus("CANCELLED");
        Bill saved = billRepository.save(bill);
        return mapToDTO(saved);
    }

    private BillResponseDTO mapToDTO(Bill bill) {
        BillResponseDTO dto = new BillResponseDTO();
        dto.setId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setPharmacyId(bill.getPharmacy().getId());
        dto.setPharmacyName(bill.getPharmacy().getName());
        dto.setPharmacyAddress(bill.getPharmacy().getAddress() + ", " + bill.getPharmacy().getCity() + " - " + bill.getPharmacy().getPincode());
        dto.setPharmacyLicense(bill.getPharmacy().getLicenseNumber());
        dto.setPharmacyPhone(bill.getPharmacy().getPhoneNumber());
        dto.setCustomerName(bill.getCustomerName());
        dto.setCustomerPhone(bill.getCustomerPhone());
        dto.setSubtotal(bill.getSubtotal());
        dto.setTaxAmount(bill.getTaxAmount());
        dto.setDiscountAmount(bill.getDiscountAmount());
        dto.setGrandTotal(bill.getGrandTotal());
        dto.setPaymentMode(bill.getPaymentMode());
        dto.setStatus(bill.getStatus() != null ? bill.getStatus() : "PAID");
        dto.setCreatedAt(bill.getCreatedAt());

        List<BillResponseDTO.BillItemDetailDTO> items = bill.getItems().stream()
                .map(item -> new BillResponseDTO.BillItemDetailDTO(
                        item.getMedicineName(),
                        item.getBatchNumber(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getTotalPrice()
                ))
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }
}
