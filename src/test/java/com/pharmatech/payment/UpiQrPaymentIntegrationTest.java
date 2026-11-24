package com.pharmatech.payment;

import com.pharmatech.dto.CreateOrderRequestDTO;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.dto.PaymentOrderRequestDTO;
import com.pharmatech.dto.PaymentVerifyRequestDTO;
import com.pharmatech.dto.UpiQrResponseDTO;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.model.Order;
import com.pharmatech.repository.OrderRepository;
import com.pharmatech.service.CustomerService;
import com.pharmatech.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UpiQrPaymentIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.pharmatech.repository.CustomerRepository customerRepository;

    @Test
    @DisplayName("End-to-End Demo UPI QR Generation, Simulation, Security Verification, and Rejection of Real Payment")
    void testDemoUpiQrPaymentLifecycle() {
        // 1. Place a customer order
        CreateOrderRequestDTO req = new CreateOrderRequestDTO();
        req.setCustomerId(customerRepository.findAll().get(0).getId());
        req.setPharmacyId(1L);
        req.setCustomerName("Anita Desai");
        req.setCustomerPhone("9123456789");
        req.setDeliveryAddress("45 Anna Salai, Coimbatore, 641018");
        req.setCustomerLatitude(11.0168);
        req.setCustomerLongitude(76.9558);
        req.setPriority("NORMAL");
        req.setPaymentMethod("RAZORPAY");

        CreateOrderRequestDTO.OrderItemRequest item = new CreateOrderRequestDTO.OrderItemRequest();
        item.setInventoryId(1L);
        item.setQuantity(2);
        req.setItems(Collections.singletonList(item));

        OrderResponseDTO orderDto = customerService.createOrder(req);
        assertNotNull(orderDto.getId());
        assertEquals("PENDING", orderDto.getPaymentStatus());
        assertEquals("ORDER_PLACED", orderDto.getStatus());
        assertTrue(orderDto.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);

        // 2. Generate dynamic Demo UPI QR Code (Requirements 2, 3, 4, 5)
        PaymentOrderRequestDTO payReq = new PaymentOrderRequestDTO();
        payReq.setOrderId(orderDto.getId());
        payReq.setOrderNumber(orderDto.getOrderNumber());
        payReq.setAmount(orderDto.getTotalAmount());

        UpiQrResponseDTO qrDto = paymentService.generateUpiQrCode(payReq);
        assertNotNull(qrDto);
        assertEquals(orderDto.getId(), qrDto.getOrderId());
        assertEquals(orderDto.getOrderNumber(), qrDto.getOrderNumber());
        assertEquals(orderDto.getTotalAmount(), qrDto.getAmount());
        assertEquals("PENDING", qrDto.getStatus());
        assertTrue(qrDto.isDemo(), "Must be marked as demo");
        assertEquals("DEMO", qrDto.getPaymentMode(), "Payment mode must be DEMO");
        assertEquals("DEMO ONLY – NOT FOR REAL PAYMENT", qrDto.getDemoLabel());

        // Verify non-payment reference (DO NOT use real upi://pay URI)
        assertNotNull(qrDto.getQrData());
        assertTrue(qrDto.getQrData().startsWith("pharmatech://demo-payment?"), "Must use non-payment reference scheme");
        assertTrue(qrDto.getQrData().contains("notice=DEMO_ONLY_NOT_FOR_REAL_PAYMENT"));
        assertFalse(qrDto.getQrData().startsWith("upi://pay"), "Must not generate a real UPI payment link");
        assertEquals(Integer.valueOf(600), qrDto.getExpiresInSeconds());

        // 3. Status polling before customer simulation
        Map<String, Object> initialStatus = paymentService.checkPaymentStatus(orderDto.getId());
        assertEquals("PENDING", initialStatus.get("status"));
        assertEquals(Boolean.FALSE, initialStatus.get("paid"));
        assertEquals(Boolean.TRUE, initialStatus.get("isDemo"));
        assertEquals("DEMO", initialStatus.get("paymentMode"));

        // 4. Simulate payment in backend (Requirements 6, 7, 8, 9)
        Map<String, Object> simulationResult = paymentService.simulateDemoPayment(orderDto.getId());
        assertEquals("DEMO_PAID", simulationResult.get("status"));
        assertEquals(Boolean.TRUE, simulationResult.get("paid"));
        assertEquals(Boolean.TRUE, simulationResult.get("isDemo"));
        assertEquals("DEMO", simulationResult.get("paymentMode"));
        assertEquals("Demo Payment Successful", simulationResult.get("message"));
        assertNotNull(simulationResult.get("paymentId"));

        // 5. Poll payment status after simulation
        Map<String, Object> confirmedStatus = paymentService.checkPaymentStatus(orderDto.getId());
        assertEquals("DEMO_PAID", confirmedStatus.get("status"));
        assertEquals(Boolean.TRUE, confirmedStatus.get("paid"));
        assertEquals(Boolean.TRUE, confirmedStatus.get("isDemo"));
        assertEquals("DEMO", confirmedStatus.get("paymentMode"));

        // 6. Verify database order state (Requirements 8, 10, 11)
        Order savedOrder = orderRepository.findById(orderDto.getId()).orElseThrow();
        assertEquals("DEMO_PAID", savedOrder.getPaymentStatus(), "Order must be marked as DEMO_PAID, never as real PAID");
        assertEquals("DEMO", savedOrder.getPaymentMode(), "Payment mode must be separate DEMO field");
        assertEquals("DEMO_UPI", savedOrder.getPaymentMethod());
        assertEquals("ORDER_PLACED", savedOrder.getStatus(), "Order must be queued for pharmacy/delivery workflow");

        // 7. Security: Prevent demo mode from accepting or confirming actual customer payments (Requirement 12)
        assertThrows(BadRequestException.class, () -> {
            paymentService.confirmUpiPayment(orderDto.getId(), "real_utr_12345", "real_pay_12345");
        }, "Should reject real gateway confirmation on demo mode orders");

        PaymentVerifyRequestDTO verifyReq = new PaymentVerifyRequestDTO();
        verifyReq.setOrderId(orderDto.getId());
        verifyReq.setRazorpayOrderId("rzp_demo_order");
        verifyReq.setRazorpayPaymentId("rzp_demo_pay");
        verifyReq.setRazorpaySignature("any_signature");
        assertThrows(BadRequestException.class, () -> {
            paymentService.verifyPaymentSignature(verifyReq);
        }, "Should reject real HMAC signature verification on demo mode orders");

        // 8. Prevent duplicate payments on already paid order
        assertThrows(BadRequestException.class, () -> {
            paymentService.generateUpiQrCode(payReq);
        }, "Should reject generating new QR code for an already paid order");
    }
}
