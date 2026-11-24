package com.pharmatech.service;

import com.pharmatech.dto.PaymentOrderRequestDTO;
import com.pharmatech.dto.PaymentOrderResponseDTO;
import com.pharmatech.dto.PaymentVerifyRequestDTO;
import com.pharmatech.model.PaymentTransaction;

import com.pharmatech.dto.UpiQrResponseDTO;

import java.util.Map;

public interface PaymentService {

    /**
     * Creates a payment order (Razorpay order or COD confirmation record).
     */
    PaymentOrderResponseDTO createPaymentOrder(PaymentOrderRequestDTO request);

    /**
     * Verifies server-side HMAC-SHA256 signature for Razorpay UPI/Cards payments.
     */
    Map<String, Object> verifyPaymentSignature(PaymentVerifyRequestDTO request);

    /**
     * Handles webhook payload notifications from Razorpay.
     */
    void handleWebhook(String payload, String signature);

    /**
     * Gets public gateway keys for frontend checkout.
     */
    Map<String, Object> getGatewayConfig();

    /**
     * Generates a dynamic UPI QR Code for the specified order.
     */
    UpiQrResponseDTO generateUpiQrCode(PaymentOrderRequestDTO request);

    /**
     * Checks payment status of an order via Razorpay / local verification.
     */
    Map<String, Object> checkPaymentStatus(Long orderId);

    /**
     * Confirms UPI payment success and transitions order to PAID and ready for pharmacy dispatch.
     */
    Map<String, Object> confirmUpiPayment(Long orderId, String transactionReference, String paymentId);

    /**
     * Simulates UPI payment confirmation in test / development mode.
     */
    Map<String, Object> simulateUpiPaymentSuccess(Long orderId);

    /**
     * Simulates successful payment in DEMO mode for college mini-project.
     * Marks the order as DEMO_PAID and prevents real payments.
     */
    Map<String, Object> simulateDemoPayment(Long orderId);

    /**
     * Professional test-payment processing for UPI, Card, and COD modes.
     * Handles manual confirmation, simulated failure, cancellation, and triggers automatic admin & pharmacy settlement.
     */
    Map<String, Object> processTestPayment(com.pharmatech.dto.ProcessTestPaymentRequestDTO request);

    Map<String, Object> uploadPaymentProof(Long orderId, String screenshotBase64, String fileName, String utrNumber);

    Map<String, Object> getVerificationStatus(Long orderId);

    Map<String, Object> retryUpiPayment(Long orderId);

    java.util.List<com.pharmatech.dto.PendingUpiVerificationDTO> getPendingUpiVerifications();

    Map<String, Object> approveUpiPayment(Long orderId);

    Map<String, Object> rejectUpiPayment(Long orderId, String reason);
}
