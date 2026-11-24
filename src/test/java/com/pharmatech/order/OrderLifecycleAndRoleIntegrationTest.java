package com.pharmatech.order;

import com.pharmatech.dto.CreateOrderRequestDTO;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.exception.UnauthorizedException;
import com.pharmatech.model.Customer;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Inventory;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.repository.CustomerRepository;
import com.pharmatech.repository.DeliveryPartnerRepository;
import com.pharmatech.repository.InventoryRepository;
import com.pharmatech.repository.PharmacyRepository;
import com.pharmatech.service.CustomerService;
import com.pharmatech.service.DeliveryService;
import com.pharmatech.service.PharmacyOwnerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OrderLifecycleAndRoleIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PharmacyOwnerService pharmacyOwnerService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Test
    @DisplayName("End-to-End Multi-Role Manual Order, Acceptance, Dispatch, and GPS Tracking Workflow")
    void testFullManualRoleOrderLifecycle() {
        Customer customer = customerRepository.findAll().get(0);
        Pharmacy pharmacy = pharmacyRepository.findAll().get(0);
        DeliveryPartner partner = deliveryPartnerRepository.findAll().get(0);
        partner.setOnline(true);
        partner.setAvailable(true);
        deliveryPartnerRepository.save(partner);

        Inventory inv = inventoryRepository.findByPharmacy(pharmacy).get(0);
        int initialStock = inv.getQuantity();

        // 1. Mandatory customer login check: Unauthenticated customer cannot place order
        CreateOrderRequestDTO unauthReq = new CreateOrderRequestDTO();
        unauthReq.setCustomerId(null);
        unauthReq.setPharmacyId(pharmacy.getId());
        unauthReq.setCustomerName("Guest");
        unauthReq.setCustomerPhone("9999999999");
        unauthReq.setDeliveryAddress("123 Street");
        unauthReq.setItems(Collections.singletonList(new CreateOrderRequestDTO.OrderItemRequest(inv.getId(), 1)));

        assertThrows(UnauthorizedException.class, () -> customerService.createOrder(unauthReq),
                "Unauthenticated order placement must be rejected");

        // 2. Authenticated Customer places order
        CreateOrderRequestDTO orderReq = new CreateOrderRequestDTO();
        orderReq.setCustomerId(customer.getId());
        orderReq.setPharmacyId(pharmacy.getId());
        orderReq.setCustomerName(customer.getFullName());
        orderReq.setCustomerPhone(customer.getPhoneNumber());
        orderReq.setDeliveryAddress("45 Metro Tower, Bangalore");
        orderReq.setCustomerLatitude(12.9716);
        orderReq.setCustomerLongitude(77.5946);
        orderReq.setPaymentMethod("COD");
        orderReq.setPriority("NORMAL");
        orderReq.setItems(Collections.singletonList(new CreateOrderRequestDTO.OrderItemRequest(inv.getId(), 2)));

        OrderResponseDTO createdOrder = customerService.createOrder(orderReq);
        assertNotNull(createdOrder.getId());
        assertEquals("ORDER_PLACED", createdOrder.getStatus());

        // CRITICAL: Delivery partner must NOT be automatically assigned!
        assertNull(createdOrder.getDeliveryPartnerId(), "Rider must NOT be automatically assigned on order creation");
        assertNull(createdOrder.getDeliveryPartnerName(), "Rider details must be hidden before partner assignment");

        // 3. Delivery partner checks pending requests: ORDER_PLACED order must NOT appear
        List<OrderResponseDTO> pendingForRider = deliveryService.getPendingDeliveryRequests(partner.getId());
        boolean orderFoundBeforeReady = pendingForRider.stream().anyMatch(o -> o.getId().equals(createdOrder.getId()));
        assertFalse(orderFoundBeforeReady, "Order must NOT appear in rider delivery requests before READY_FOR_PICKUP");

        // 4. Pharmacy owner manually reviews and accepts the order
        OrderResponseDTO acceptedByPharm = pharmacyOwnerService.updateOrderStatus(createdOrder.getId(), pharmacy.getId(), "PHARMACY_ACCEPTED");
        assertEquals("PHARMACY_ACCEPTED", acceptedByPharm.getStatus());

        // 5. Pharmacy owner prepares medicine and marks READY_FOR_PICKUP
        OrderResponseDTO readyForPickup = pharmacyOwnerService.updateOrderStatus(createdOrder.getId(), pharmacy.getId(), "READY_FOR_PICKUP");
        assertEquals("READY_FOR_PICKUP", readyForPickup.getStatus());
        assertNotNull(readyForPickup.getReadyAt());

        // 6. NOW order appears in available requests for online delivery partners
        List<OrderResponseDTO> readyRequests = deliveryService.getPendingDeliveryRequests(partner.getId());
        boolean orderAvailableNow = readyRequests.stream().anyMatch(o -> o.getId().equals(createdOrder.getId()));
        assertTrue(orderAvailableNow, "Order must appear in rider requests once READY_FOR_PICKUP");

        // 7. Delivery Partner manually accepts delivery
        OrderResponseDTO acceptedByRider = deliveryService.acceptDelivery(createdOrder.getId(), partner.getId());
        assertEquals("PARTNER_ASSIGNED", acceptedByRider.getStatus());
        assertEquals(partner.getId(), acceptedByRider.getDeliveryPartnerId());

        // Concurrency safety: second partner trying to accept the same order must fail
        if (deliveryPartnerRepository.findAll().size() > 1) {
            DeliveryPartner partner2 = deliveryPartnerRepository.findAll().get(1);
            assertThrows(BadRequestException.class, () -> deliveryService.acceptDelivery(createdOrder.getId(), partner2.getId()),
                    "Second partner must be prevented from accepting an already claimed delivery");
        }

        // 8. Customer tracking now displays assigned delivery partner details
        OrderResponseDTO tracked = customerService.trackOrder(createdOrder.getId());
        assertEquals(partner.getFullName(), tracked.getDeliveryPartnerName());
        assertEquals(partner.getVehicleNumber(), tracked.getVehicleNumber());

        // 9. Delivery partner manually marks PICKED_UP
        OrderResponseDTO pickedUp = deliveryService.updateOrderStatus(createdOrder.getId(), "PICKED_UP", partner.getId());
        assertEquals("PICKED_UP", pickedUp.getStatus());
        assertNotNull(pickedUp.getPickedUpAt());

        // 10. Rider sends real live GPS location update
        Map<String, Object> gpsUpdate = deliveryService.recordLocationUpdate(partner.getId(), createdOrder.getId(), 12.9750, 77.6000, 45.0, 25.0);
        assertNotNull(gpsUpdate);
        assertEquals(12.9750, gpsUpdate.get("latitude"));

        // Tracking returns recorded coordinates
        OrderResponseDTO trackedWithGps = customerService.trackOrder(createdOrder.getId());
        assertEquals(12.9750, trackedWithGps.getRiderLatitude());
        assertEquals(77.6000, trackedWithGps.getRiderLongitude());

        // 11. Rider transitions to OUT_FOR_DELIVERY and DELIVERED
        deliveryService.updateOrderStatus(createdOrder.getId(), "OUT_FOR_DELIVERY", partner.getId());
        OrderResponseDTO delivered = deliveryService.updateOrderStatus(createdOrder.getId(), "DELIVERED", partner.getId());
        assertEquals("DELIVERED", delivered.getStatus());
        assertNotNull(delivered.getDeliveredAt());

        // Rider is freed
        DeliveryPartner updatedPartner = deliveryPartnerRepository.findById(partner.getId()).orElseThrow();
        assertTrue(updatedPartner.isAvailable(), "Rider must be available again after delivery completion");
    }

    @Test
    @DisplayName("Pharmacy Rejection Workflow: Rejection sets status to CANCELLED and restores stock")
    void testPharmacyRejectionAndStockRestoration() {
        Customer customer = customerRepository.findAll().get(0);
        Pharmacy pharmacy = pharmacyRepository.findAll().get(0);
        Inventory inv = inventoryRepository.findByPharmacy(pharmacy).get(0);

        int initialStock = inv.getQuantity();

        CreateOrderRequestDTO orderReq = new CreateOrderRequestDTO();
        orderReq.setCustomerId(customer.getId());
        orderReq.setPharmacyId(pharmacy.getId());
        orderReq.setCustomerName(customer.getFullName());
        orderReq.setCustomerPhone(customer.getPhoneNumber());
        orderReq.setDeliveryAddress("77 Green Road, Bangalore");
        orderReq.setItems(Collections.singletonList(new CreateOrderRequestDTO.OrderItemRequest(inv.getId(), 5)));

        OrderResponseDTO created = customerService.createOrder(orderReq);

        // Check stock was deducted
        Inventory invAfterOrder = inventoryRepository.findById(inv.getId()).orElseThrow();
        assertEquals(initialStock - 5, invAfterOrder.getQuantity(), "Stock should be reduced by 5 upon ordering");

        // Pharmacy rejects order
        OrderResponseDTO rejected = pharmacyOwnerService.updateOrderStatus(created.getId(), pharmacy.getId(), "REJECTED");
        assertEquals("CANCELLED", rejected.getStatus());

        // Check stock is restored
        Inventory invAfterReject = inventoryRepository.findById(inv.getId()).orElseThrow();
        assertEquals(initialStock, invAfterReject.getQuantity(), "Stock should be restored to initial quantity on rejection");
    }
}
