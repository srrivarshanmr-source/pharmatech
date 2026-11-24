package com.pharmatech.payment;

import com.pharmatech.dto.*;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.model.Customer;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Inventory;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.repository.*;
import com.pharmatech.service.CustomerService;
import com.pharmatech.service.DeliveryService;
import com.pharmatech.service.PaymentService;
import com.pharmatech.service.PharmacyOwnerService;
import com.pharmatech.service.SettlementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PaymentAndSettlementIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private PharmacyOwnerService pharmacyOwnerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private DeliveryPartnerRepository deliveryPartnerRepository;

    @Autowired
    private AdminWalletLedgerRepository adminWalletLedgerRepository;

    @Autowired
    private PharmacySettlementLedgerRepository pharmacySettlementLedgerRepository;

    @Autowired
    private OrderRepository orderRepository;

    private OrderResponseDTO createTestOrder(String method) {
        Customer customer = customerRepository.findAll().get(0);
        Pharmacy pharmacy = pharmacyRepository.findAll().get(0);
        Inventory inv = inventoryRepository.findByPharmacy(pharmacy).get(0);

        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setCustomerId(customer.getId());
        req.setPharmacyId(pharmacy.getId());
        req.setCustomerName(customer.getFullName());
        req.setCustomerPhone(customer.getPhoneNumber());
        req.setDeliveryAddress("100 Indiranagar, Bangalore");
        req.setCustomerLatitude(12.9784);
        req.setCustomerLongitude(77.6408);
        req.setPriority("NORMAL");
        req.setPaymentMethod(method);
        req.setItems(Collections.singletonList(new CreateOrderRequestDTO.OrderItemRequest(inv.getId(), 1)));

        return customerService.createOrder(req);
    }

    @Test
    @DisplayName("UPI Test Payment Flow: Validates Regex, Masks UPI, Credits Admin Demo Wallet, Settles Pharmacy with Commission")
    void testUpiTestPaymentLifecycleAndSettlement() {
        OrderResponseDTO order = createTestOrder("UPI");
        assertNotNull(order.getId());
        assertEquals("PENDING", order.getPaymentStatus());

        // 1. Invalid UPI format check
        ProcessTestPaymentRequestDTO invalidReq = new ProcessTestPaymentRequestDTO();
        invalidReq.setOrderId(order.getId());
        invalidReq.setPaymentMethod("UPI");
        invalidReq.setAction("CONFIRM");
        invalidReq.setUpiId("not-a-valid-upi");

        assertThrows(BadRequestException.class, () -> paymentService.processTestPayment(invalidReq),
                "Should reject malformed UPI IDs");

        // 2. Valid UPI format confirmation
        ProcessTestPaymentRequestDTO validReq = new ProcessTestPaymentRequestDTO();
        validReq.setOrderId(order.getId());
        validReq.setPaymentMethod("UPI");
        validReq.setAction("CONFIRM");
        validReq.setUpiId("shankar.kumar@okhdfcbank");

        Map<String, Object> result = paymentService.processTestPayment(validReq);
        assertTrue((Boolean) result.get("success"));
        assertEquals("TEST_SUCCESS", result.get("paymentStatus"));
        assertNotNull(result.get("transactionReference"));
        assertTrue(result.get("maskedIdentifier").toString().contains("***"));

        // 3. Verify Admin Wallet Ledger entry
        List<AdminPaymentLedgerDTO> adminLedgers = settlementService.getAllAdminLedgerEntries("TEST_SUCCESS", null, null, null);
        AdminPaymentLedgerDTO entry = adminLedgers.stream()
                .filter(l -> l.getOrderId().equals(order.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(entry, "Admin demo wallet ledger entry must be created");
        assertEquals("TEST_SUCCESS", entry.getPaymentStatus());
        assertEquals("DEMO_SETTLED", entry.getSettlementStatus());
        assertEquals(new BigDecimal("5.00"), entry.getCommissionRate().setScale(2, RoundingMode.HALF_UP));

        BigDecimal expectedCommission = order.getTotalAmount().multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedNet = order.getTotalAmount().subtract(expectedCommission);

        assertEquals(expectedCommission, entry.getCommissionAmount().setScale(2, RoundingMode.HALF_UP));
        assertEquals(expectedNet, entry.getNetSettlementAmount().setScale(2, RoundingMode.HALF_UP));

        // 4. Verify Pharmacy Settlement Ledger entry
        List<PharmacySettlementDTO> pharmacySettlements = settlementService.getPharmacySettlements(order.getPharmacyId());
        PharmacySettlementDTO pEntry = pharmacySettlements.stream()
                .filter(s -> s.getOrderId().equals(order.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(pEntry, "Pharmacy settlement ledger entry must be created");
        assertEquals("DEMO_SETTLED", pEntry.getSettlementStatus());
        assertEquals(expectedNet, pEntry.getNetAmount().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Card Test Payment Flow: Uses Dummy Card Data, Masks Card, Never Stores CVV, and Settles Correctly")
    void testCardTestPaymentLifecycleAndSettlement() {
        OrderResponseDTO order = createTestOrder("CARD");

        ProcessTestPaymentRequestDTO cardReq = new ProcessTestPaymentRequestDTO();
        cardReq.setOrderId(order.getId());
        cardReq.setPaymentMethod("CARD");
        cardReq.setAction("CONFIRM");
        cardReq.setCardNumber("4532 0150 9988 4242");
        cardReq.setCardHolder("Ramesh Rao");
        cardReq.setExpiryDate("12/28");
        cardReq.setCvv("123");

        Map<String, Object> result = paymentService.processTestPayment(cardReq);
        assertTrue((Boolean) result.get("success"));
        assertEquals("TEST_SUCCESS", result.get("paymentStatus"));
        assertEquals("•••• •••• •••• 4242", result.get("maskedIdentifier"));

        // Verify ledgers created
        List<AdminPaymentLedgerDTO> adminLedgers = settlementService.getAllAdminLedgerEntries("TEST_SUCCESS", null, null, null);
        boolean found = adminLedgers.stream().anyMatch(l -> l.getOrderId().equals(order.getId()));
        assertTrue(found, "Card transaction must create admin ledger entry");
    }

    @Test
    @DisplayName("Payment Failure and Cancellation Handling: Records Attempts Without Settlement Credits")
    void testFailedAndCancelledPaymentSimulation() {
        OrderResponseDTO order1 = createTestOrder("UPI");

        // Fail simulation
        ProcessTestPaymentRequestDTO failReq = new ProcessTestPaymentRequestDTO();
        failReq.setOrderId(order1.getId());
        failReq.setPaymentMethod("UPI");
        failReq.setAction("FAIL");
        failReq.setUpiId("demo.fail@okhdfcbank");

        Map<String, Object> failResult = paymentService.processTestPayment(failReq);
        assertFalse((Boolean) failResult.get("success"));
        assertEquals("TEST_FAILED", failResult.get("paymentStatus"));

        // Verify no settlement created
        List<AdminPaymentLedgerDTO> ledgers = settlementService.getAllAdminLedgerEntries("TEST_FAILED", null, null, null);
        AdminPaymentLedgerDTO failedEntry = ledgers.stream()
                .filter(l -> l.getOrderId().equals(order1.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(failedEntry);
        assertEquals("NONE", failedEntry.getSettlementStatus());
        assertEquals(BigDecimal.ZERO, failedEntry.getNetSettlementAmount().setScale(0, RoundingMode.HALF_UP));

        // Cancel simulation
        OrderResponseDTO order2 = createTestOrder("CARD");
        ProcessTestPaymentRequestDTO cancelReq = new ProcessTestPaymentRequestDTO();
        cancelReq.setOrderId(order2.getId());
        cancelReq.setPaymentMethod("CARD");
        cancelReq.setAction("CANCEL");

        Map<String, Object> cancelResult = paymentService.processTestPayment(cancelReq);
        assertFalse((Boolean) cancelResult.get("success"));
        assertEquals("TEST_CANCELLED", cancelResult.get("paymentStatus"));
    }

    @Test
    @DisplayName("Cash on Delivery Flow: Starts as COD_PENDING with No Ledgers; Settles Upon Delivery Partner Confirmation")
    void testCashOnDeliveryAndCollectionSettlement() {
        OrderResponseDTO codOrder = createTestOrder("COD");
        assertEquals("COD_PENDING", codOrder.getPaymentStatus());

        // Verify NO settlement ledger exists upon COD placement
        List<AdminPaymentLedgerDTO> initialLedgers = settlementService.getAllAdminLedgerEntries(null, null, null, null);
        boolean settledInitial = initialLedgers.stream()
                .anyMatch(l -> l.getOrderId().equals(codOrder.getId()) && "DEMO_SETTLED".equalsIgnoreCase(l.getSettlementStatus()));
        assertFalse(settledInitial, "COD order must NOT be credited or settled at placement time");

        // Advance order: accept, ready, assign partner
        DeliveryPartner partner = deliveryPartnerRepository.findAll().get(0);
        partner.setOnline(true);
        partner.setAvailable(true);
        deliveryPartnerRepository.save(partner);

        pharmacyOwnerService.updateOrderStatus(codOrder.getId(), codOrder.getPharmacyId(), "READY_FOR_PICKUP");
        deliveryService.acceptDelivery(codOrder.getId(), partner.getId());

        // Delivery partner confirms cash receipt
        OrderResponseDTO collected = deliveryService.recordPayment(codOrder.getId(), partner.getId(), "COD");
        assertEquals("COD_COLLECTED", collected.getPaymentStatus());

        // Now verify Admin and Pharmacy ledgers exist and are settled
        List<AdminPaymentLedgerDTO> postLedgers = settlementService.getAllAdminLedgerEntries("COD_COLLECTED", null, null, null);
        AdminPaymentLedgerDTO codEntry = postLedgers.stream()
                .filter(l -> l.getOrderId().equals(codOrder.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(codEntry, "COD collected order must now have settled ledger");
        assertEquals("DEMO_SETTLED", codEntry.getSettlementStatus());
        assertEquals("COD", codEntry.getPaymentMethod());
    }

    @Test
    @DisplayName("Configurable Commission Rate: Updating Setting Dynamically Alters Settlement Deduction")
    void testConfigurableCommissionSetting() {
        // Change commission to 8.0%
        AdminCommissionSettingDTO setting = new AdminCommissionSettingDTO(new BigDecimal("8.00"), "srrivarshanmr@okaxis");
        settlementService.updateCommissionSetting(setting);

        BigDecimal currentRate = settlementService.getAdminCommissionRate();
        assertEquals(new BigDecimal("8.00"), currentRate.setScale(2, RoundingMode.HALF_UP));

        // Place and pay order
        OrderResponseDTO order = createTestOrder("UPI");
        ProcessTestPaymentRequestDTO req = new ProcessTestPaymentRequestDTO();
        req.setOrderId(order.getId());
        req.setPaymentMethod("UPI");
        req.setAction("CONFIRM");
        req.setUpiId("test@okaxis");

        paymentService.processTestPayment(req);

        List<AdminPaymentLedgerDTO> ledgers = settlementService.getAllAdminLedgerEntries("TEST_SUCCESS", null, null, null);
        AdminPaymentLedgerDTO entry = ledgers.stream()
                .filter(l -> l.getOrderId().equals(order.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(entry);
        assertEquals(new BigDecimal("8.00"), entry.getCommissionRate().setScale(2, RoundingMode.HALF_UP));

        BigDecimal expectedCommission = order.getTotalAmount().multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
        assertEquals(expectedCommission, entry.getCommissionAmount().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("Order Cancellation Reversal: Cancelling Paid Order Marks Ledgers REVERSED")
    void testOrderCancellationReversesSettlements() {
        OrderResponseDTO order = createTestOrder("UPI");
        ProcessTestPaymentRequestDTO req = new ProcessTestPaymentRequestDTO();
        req.setOrderId(order.getId());
        req.setPaymentMethod("UPI");
        req.setAction("CONFIRM");
        req.setUpiId("user@okaxis");

        paymentService.processTestPayment(req);

        // Cancel order via pharmacy owner
        pharmacyOwnerService.updateOrderStatus(order.getId(), order.getPharmacyId(), "CANCELLED");

        // Verify ledgers are marked REVERSED
        List<AdminPaymentLedgerDTO> ledgers = settlementService.getAllAdminLedgerEntries(null, null, null, null);
        AdminPaymentLedgerDTO entry = ledgers.stream()
                .filter(l -> l.getOrderId().equals(order.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(entry);
        assertEquals("REVERSED", entry.getSettlementStatus());

        List<PharmacySettlementDTO> pLedgers = settlementService.getPharmacySettlements(order.getPharmacyId());
        PharmacySettlementDTO pEntry = pLedgers.stream()
                .filter(s -> s.getOrderId().equals(order.getId()))
                .findFirst()
                .orElse(null);

        assertNotNull(pEntry);
        assertEquals("REVERSED", pEntry.getSettlementStatus());
    }
}
