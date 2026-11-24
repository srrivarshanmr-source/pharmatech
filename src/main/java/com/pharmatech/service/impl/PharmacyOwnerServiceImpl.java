package com.pharmatech.service.impl;

import com.pharmatech.dto.DashboardStatsDTO;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.model.Order;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.model.PharmacyOwner;
import com.pharmatech.repository.InventoryRepository;
import com.pharmatech.repository.OrderRepository;
import com.pharmatech.repository.PharmacyOwnerRepository;
import com.pharmatech.repository.PharmacyRepository;
import com.pharmatech.service.PharmacyOwnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Pharmacy Owner operations, inventory metrics,
 * and order notification / status processing.
 */
@Service
@Transactional
public class PharmacyOwnerServiceImpl implements PharmacyOwnerService {

    private final PharmacyOwnerRepository pharmacyOwnerRepository;
    private final PharmacyRepository pharmacyRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final com.pharmatech.service.SettlementService settlementService;

    public PharmacyOwnerServiceImpl(PharmacyOwnerRepository pharmacyOwnerRepository,
                                    PharmacyRepository pharmacyRepository,
                                    InventoryRepository inventoryRepository,
                                    OrderRepository orderRepository,
                                    com.pharmatech.service.SettlementService settlementService) {
        this.pharmacyOwnerRepository = pharmacyOwnerRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.settlementService = settlementService;
    }

    @Override
    public DashboardStatsDTO getDashboardStats(Long ownerId) {
        PharmacyOwner owner = pharmacyOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy Owner", "id", ownerId));

        Pharmacy pharmacy = pharmacyRepository.findByOwner(owner)
                .orElse(null);

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setOwnerName(owner.getFullName());
        stats.setSubscriptionActive(owner.isSubscriptionActive());

        if (pharmacy != null) {
            Long pharmacyId = pharmacy.getId();
            long total = inventoryRepository.countByPharmacyId(pharmacyId);
            long available = inventoryRepository.countByPharmacyIdAndQuantityGreaterThan(pharmacyId, 0);
            long lowStock = inventoryRepository.countByPharmacyIdAndQuantityLessThanEqualAndQuantityGreaterThan(pharmacyId, 15, 1);
            long outOfStock = inventoryRepository.countByPharmacyIdAndQuantityEquals(pharmacyId, 0);

            long placed = orderRepository.countByPharmacyAndStatus(pharmacy, "ORDER_PLACED");
            long prep = orderRepository.countByPharmacyAndStatus(pharmacy, "PREPARING");
            long ready = orderRepository.countByPharmacyAndStatus(pharmacy, "READY_FOR_PICKUP");
            long pendingOrders = placed + prep + ready;

            stats.setPharmacyName(pharmacy.getName());
            stats.setLicenseNumber(pharmacy.getLicenseNumber());
            stats.setAddress(pharmacy.getAddress());
            stats.setCity(pharmacy.getCity());
            stats.setPincode(pharmacy.getPincode());
            stats.setTotalMedicines(total);
            stats.setAvailableMedicines(available);
            stats.setLowStockMedicines(lowStock);
            stats.setOutOfStockMedicines(outOfStock);
            stats.setPendingOrders(pendingOrders);

            stats.setRecentActivity(Arrays.asList(
                    "Store catalog synced with PharmaTech network",
                    "Active live delivery dispatch integration enabled",
                    "Partner subscription active: ₹200/month plan"
            ));
        } else {
            stats.setPharmacyName("No Pharmacy Associated");
            stats.setLicenseNumber(owner.getLicenseNumber());
            stats.setTotalMedicines(0);
            stats.setAvailableMedicines(0);
            stats.setLowStockMedicines(0);
            stats.setOutOfStockMedicines(0);
            stats.setPendingOrders(0);
        }

        return stats;
    }

    @Override
    public Pharmacy getPharmacyByOwnerId(Long ownerId) {
        return pharmacyRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found for owner id: " + ownerId));
    }

    @Override
    public PharmacyOwner getOwnerProfile(Long ownerId) {
        return pharmacyOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy Owner", "id", ownerId));
    }

    @Override
    public Pharmacy updatePharmacyProfile(Long ownerId, Pharmacy details) {
        Pharmacy pharmacy = getPharmacyByOwnerId(ownerId);
        if (details.getName() != null && !details.getName().trim().isEmpty()) {
            pharmacy.setName(details.getName().trim());
        }
        if (details.getAddress() != null && !details.getAddress().trim().isEmpty()) {
            pharmacy.setAddress(details.getAddress().trim());
        }
        if (details.getCity() != null && !details.getCity().trim().isEmpty()) {
            pharmacy.setCity(details.getCity().trim());
        }
        if (details.getPincode() != null && !details.getPincode().trim().isEmpty()) {
            pharmacy.setPincode(details.getPincode().trim());
        }
        if (details.getPhoneNumber() != null && !details.getPhoneNumber().trim().isEmpty()) {
            pharmacy.setPhoneNumber(details.getPhoneNumber().trim());
        }
        if (details.getOperatingHours() != null && !details.getOperatingHours().trim().isEmpty()) {
            pharmacy.setOperatingHours(details.getOperatingHours().trim());
        }
        if (details.getMerchantUpiId() != null && !details.getMerchantUpiId().trim().isEmpty()) {
            pharmacy.setMerchantUpiId(details.getMerchantUpiId().trim());
        }
        return pharmacyRepository.save(pharmacy);
    }

    @Override
    public List<OrderResponseDTO> getPharmacyOrders(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id: " + pharmacyId));

        return orderRepository.findByPharmacyPrioritized(pharmacy).stream()
                .filter(o -> !"PAYMENT_PENDING".equalsIgnoreCase(o.getStatus()))
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, Long pharmacyId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (pharmacyId != null && !order.getPharmacy().getId().equals(pharmacyId)) {
            throw new BadRequestException("Order does not belong to this pharmacy");
        }

        String validStatus = newStatus.trim().toUpperCase();

        if ("CANCELLED".equalsIgnoreCase(validStatus) || "REJECTED".equalsIgnoreCase(validStatus)) {
            validStatus = "CANCELLED";
            // Restore reserved inventory stock upon cancellation
            if (!"CANCELLED".equalsIgnoreCase(order.getStatus()) && order.getItems() != null) {
                for (com.pharmatech.model.OrderItem item : order.getItems()) {
                    if (item.getMedicine() != null && order.getPharmacy() != null) {
                        inventoryRepository.findByPharmacyIdAndMedicineId(order.getPharmacy().getId(), item.getMedicine().getId())
                                .ifPresent(inv -> {
                                    inv.setQuantity(inv.getQuantity() + item.getQuantity());
                                    inv.setAvailable(true);
                                    inventoryRepository.save(inv);
                                });
                    }
                }
            }
            if (order.getDeliveryPartner() != null) {
                com.pharmatech.model.DeliveryPartner dp = order.getDeliveryPartner();
                dp.setAvailable(true);
            }
            // Reverse any prepaid or collected settlements
            settlementService.reverseSettlementForOrder(order, "Pharmacy owner cancelled/rejected order");
        }

        order.setStatus(validStatus);

        if ("READY_FOR_PICKUP".equalsIgnoreCase(validStatus)) {
            order.setReadyAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);
        return mapOrderToDTO(saved);
    }

    @Override
    public Map<String, Object> getPharmacyNotifications(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy not found with id: " + pharmacyId));

        List<Order> orders = orderRepository.findByPharmacyPrioritized(pharmacy);

        List<Order> newOrders = orders.stream()
                .filter(o -> "ORDER_PLACED".equalsIgnoreCase(o.getStatus()) || "PHARMACY_ACCEPTED".equalsIgnoreCase(o.getStatus()) || "PREPARING".equalsIgnoreCase(o.getStatus()) || "PARTNER_ASSIGNED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());

        long emergencyCount = newOrders.stream()
                .filter(o -> "EMERGENCY".equalsIgnoreCase(o.getPriority()))
                .count();

        Map<String, Object> map = new HashMap<>();
        map.put("unreadCount", newOrders.size());
        map.put("emergencyCount", emergencyCount);
        map.put("newOrders", newOrders.stream().map(this::mapOrderToDTO).collect(Collectors.toList()));
        return map;
    }

    private OrderResponseDTO mapOrderToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setCustomerLatitude(order.getCustomerLatitude());
        dto.setCustomerLongitude(order.getCustomerLongitude());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPriority(order.getPriority());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMode(order.getPaymentMode() != null ? order.getPaymentMode() : "DEMO");
        dto.setDemo("DEMO".equalsIgnoreCase(order.getPaymentMode()));
        dto.setDeliveryDistanceKm(order.getDeliveryDistanceKm());
        dto.setEstimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes());
        dto.setDeliveryEarnings(order.getDeliveryEarnings());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setReadyAt(order.getReadyAt());
        dto.setPickedUpAt(order.getPickedUpAt());
        dto.setDeliveredAt(order.getDeliveredAt());

        dto.setPaymentProofData(order.getPaymentProofData());
        dto.setPaymentProofFileName(order.getPaymentProofFileName());
        dto.setPaymentProofUploadedAt(order.getPaymentProofUploadedAt());
        dto.setPaymentProofExpiresAt(order.getPaymentProofExpiresAt());
        dto.setPaymentUtr(order.getPaymentUtr());
        dto.setPaymentApprovalStatus(order.getPaymentApprovalStatus() != null ? order.getPaymentApprovalStatus() : "NONE");
        dto.setPaymentApprovedAt(order.getPaymentApprovedAt());
        dto.setPaymentRejectReason(order.getPaymentRejectReason());

        if (order.getPharmacy() != null) {
            dto.setPharmacyId(order.getPharmacy().getId());
            dto.setPharmacyName(order.getPharmacy().getName());
            dto.setPharmacyAddress(order.getPharmacy().getAddress() + ", " + order.getPharmacy().getCity());
            dto.setPharmacyPhone(order.getPharmacy().getPhoneNumber());
            dto.setMerchantUpiId(order.getPharmacy().getMerchantUpiId());
        }

        if (order.getDeliveryPartner() != null) {
            dto.setDeliveryPartnerId(order.getDeliveryPartner().getId());
            dto.setDeliveryPartnerName(order.getDeliveryPartner().getFullName());
            dto.setDeliveryPartnerPhone(order.getDeliveryPartner().getPhoneNumber());
            dto.setVehicleNumber(order.getDeliveryPartner().getVehicleNumber());
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(it -> new OrderResponseDTO.OrderItemDetailDTO(
                            it.getMedicineName(),
                            it.getQuantity(),
                            it.getUnitPrice(),
                            it.getTotalPrice()
                    ))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
