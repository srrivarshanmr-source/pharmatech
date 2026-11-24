package com.pharmatech.service;

import com.pharmatech.dto.DashboardStatsDTO;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.model.PharmacyOwner;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Pharmacy Owner operations and dashboard statistics.
 */
public interface PharmacyOwnerService {

    DashboardStatsDTO getDashboardStats(Long ownerId);

    Pharmacy getPharmacyByOwnerId(Long ownerId);

    PharmacyOwner getOwnerProfile(Long ownerId);

    Pharmacy updatePharmacyProfile(Long ownerId, Pharmacy pharmacyDetails);

    List<OrderResponseDTO> getPharmacyOrders(Long pharmacyId);

    OrderResponseDTO updateOrderStatus(Long orderId, Long pharmacyId, String newStatus);

    Map<String, Object> getPharmacyNotifications(Long pharmacyId);
}
