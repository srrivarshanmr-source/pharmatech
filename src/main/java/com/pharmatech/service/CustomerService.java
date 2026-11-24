package com.pharmatech.service;

import com.pharmatech.dto.CreateOrderRequestDTO;
import com.pharmatech.dto.NearbyPharmacySearchResultDTO;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.model.Customer;
import com.pharmatech.model.Pharmacy;
import java.util.List;

/**
 * Service interface for customer-facing operations.
 */
public interface CustomerService {

    Customer getCustomerProfile(Long customerId);

    List<Pharmacy> getNearbyPharmacies(String city, String pincode);

    Customer updateLocation(Long customerId, String city, String pincode, String address);

    Customer updateLocationCoordinates(Long customerId, String locality, String city, String pincode, Double latitude, Double longitude);

    List<NearbyPharmacySearchResultDTO> searchNearbyPharmacies(String medicineQuery, Double latitude, Double longitude, Double radiusKm);

    OrderResponseDTO createOrder(CreateOrderRequestDTO request);

    OrderResponseDTO trackOrder(Long orderId);

    OrderResponseDTO trackOrderByNumber(String orderNumber);

    List<OrderResponseDTO> getCustomerOrders(Long customerId);
}

