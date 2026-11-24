package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.PaymentOrderRequestDTO;
import com.pharmatech.dto.PaymentOrderResponseDTO;
import com.pharmatech.dto.PaymentVerifyRequestDTO;
import com.pharmatech.dto.UpiQrResponseDTO;
import com.pharmatech.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/customer/payment", "/api/payment"})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGatewayConfig() {
        Map<String, Object> config = paymentService.getGatewayConfig();
        return ResponseEntity.ok(ApiResponse.ok("Payment gateway configuration loaded", config));
    }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponseDTO>> createPaymentOrder(
            @Valid @RequestBody PaymentOrderRequestDTO request) {
        PaymentOrderResponseDTO response = paymentService.createPaymentOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment order created successfully", response));
    }

    @PostMapping("/generate-upi-qr")
    public ResponseEntity<ApiResponse<UpiQrResponseDTO>> generateUpiQrCode(
            @Valid @RequestBody PaymentOrderRequestDTO request) {
        UpiQrResponseDTO response = paymentService.generateUpiQrCode(request);
        return ResponseEntity.ok(ApiResponse.ok("Dynamic UPI QR code generated successfully", response));
    }

    @GetMapping("/check-status/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkPaymentStatus(
            @PathVariable Long orderId) {
        Map<String, Object> status = paymentService.checkPaymentStatus(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Payment status retrieved", status));
    }

    @PostMapping("/confirm-upi-payment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmUpiPayment(
            @RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String transactionRef = payload.get("transactionReference") != null ? payload.get("transactionReference").toString() : null;
        String paymentId = payload.get("paymentId") != null ? payload.get("paymentId").toString() : null;
        Map<String, Object> result = paymentService.confirmUpiPayment(orderId, transactionRef, paymentId);
        return ResponseEntity.ok(ApiResponse.ok("UPI payment confirmed successfully", result));
    }

    @PostMapping("/process-test-payment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processTestPayment(
            @Valid @RequestBody com.pharmatech.dto.ProcessTestPaymentRequestDTO request) {
        Map<String, Object> result = paymentService.processTestPayment(request);
        return ResponseEntity.ok(ApiResponse.ok("Test payment processed successfully", result));
    }

    @PostMapping("/simulate-success/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateUpiPaymentSuccess(
            @PathVariable Long orderId) {
        Map<String, Object> result = paymentService.simulateUpiPaymentSuccess(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Test UPI payment simulation approved successfully", result));
    }

    @PostMapping("/demo/simulate-payment/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulateDemoPayment(
            @PathVariable Long orderId) {
        Map<String, Object> result = paymentService.simulateDemoPayment(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Demo Payment Successful", result));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequestDTO request) {
        Map<String, Object> result = paymentService.verifyPaymentSignature(request);
        return ResponseEntity.ok(ApiResponse.ok("Payment successfully verified and updated", result));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<String>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.ok("Webhook processed successfully"));
    }

    @PostMapping(value = "/upload-proof", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadPaymentProofMultipart(
            @RequestParam("orderId") Long orderId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "utrNumber", required = false) String utrNumber) {
        try {
            String base64 = "data:" + file.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(file.getBytes());
            Map<String, Object> result = paymentService.uploadPaymentProof(orderId, base64, file.getOriginalFilename(), utrNumber);
            return ResponseEntity.ok(ApiResponse.ok("Payment Submitted — Waiting for Admin Verification", result));
        } catch (Exception e) {
            throw new com.pharmatech.exception.BadRequestException("Failed to upload screenshot: " + e.getMessage());
        }
    }

    @PostMapping(value = "/upload-proof", consumes = {"application/json"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadPaymentProofJson(
            @RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String screenshotBase64 = payload.get("screenshotBase64") != null ? payload.get("screenshotBase64").toString() : "";
        String fileName = payload.get("fileName") != null ? payload.get("fileName").toString() : "upi_payment_screenshot.png";
        String utrNumber = payload.get("utrNumber") != null ? payload.get("utrNumber").toString() : null;
        Map<String, Object> result = paymentService.uploadPaymentProof(orderId, screenshotBase64, fileName, utrNumber);
        return ResponseEntity.ok(ApiResponse.ok("Payment Submitted — Waiting for Admin Verification", result));
    }

    @GetMapping({"/verification-status/{orderId}", "/check-verification/{orderId}"})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVerificationStatus(
            @PathVariable Long orderId) {
        Map<String, Object> result = paymentService.getVerificationStatus(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Verification status retrieved", result));
    }

    @PostMapping("/retry-upi/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retryUpi(
            @PathVariable Long orderId) {
        Map<String, Object> result = paymentService.retryUpiPayment(orderId);
        return ResponseEntity.ok(ApiResponse.ok("UPI payment session renewed", result));
    }
}
