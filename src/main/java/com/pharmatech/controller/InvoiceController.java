package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.InvoiceConfirmRequestDTO;
import com.pharmatech.dto.InvoiceParseResultDTO;
import com.pharmatech.model.InvoiceUploadRecord;
import com.pharmatech.service.InvoiceProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controller handling Supplier Purchase Invoice Upload, Extraction, Preview, and Stock Update.
 */
@RestController
@RequestMapping("/api/pharmacy/invoice")
public class InvoiceController {

    private final InvoiceProcessingService invoiceProcessingService;

    public InvoiceController(InvoiceProcessingService invoiceProcessingService) {
        this.invoiceProcessingService = invoiceProcessingService;
    }

    /**
     * Stage 1: Upload invoice file (PDF, JPG, PNG, TXT, CSV) to extract and preview data.
     * Does NOT modify database inventory.
     */
    @PostMapping("/extract/{pharmacyId}")
    public ResponseEntity<ApiResponse<InvoiceParseResultDTO>> extractInvoiceFile(
            @PathVariable Long pharmacyId,
            @RequestParam("file") MultipartFile file) {
        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoice(pharmacyId, file);
        return ResponseEntity.ok(ApiResponse.ok("Invoice extracted for preview", result));
    }

    /**
     * Stage 1 (Alternative): Extract from raw invoice text.
     */
    @PostMapping("/extract-raw/{pharmacyId}")
    public ResponseEntity<ApiResponse<InvoiceParseResultDTO>> extractInvoiceRaw(
            @PathVariable Long pharmacyId,
            @RequestBody Map<String, String> payload) {
        String rawText = payload.get("rawText");
        String fileName = payload.getOrDefault("fileName", "purchase_invoice.txt");
        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(pharmacyId, rawText, fileName);
        return ResponseEntity.ok(ApiResponse.ok("Invoice extracted for preview", result));
    }

    /**
     * Stage 2: Confirm extracted & edited invoice items, updating pharmacy stock in database.
     * Prevents duplicate additions.
     */
    @PostMapping("/confirm/{pharmacyId}")
    public ResponseEntity<ApiResponse<InvoiceParseResultDTO>> confirmInvoice(
            @PathVariable Long pharmacyId,
            @RequestBody InvoiceConfirmRequestDTO request) {
        InvoiceParseResultDTO result = invoiceProcessingService.confirmAndUpdateStock(pharmacyId, request);
        return ResponseEntity.ok(ApiResponse.ok("Stock updated successfully", result));
    }

    /**
     * Legacy / Direct upload fallback (Extracts and returns preview).
     */
    @PostMapping("/upload/{pharmacyId}")
    public ResponseEntity<ApiResponse<InvoiceParseResultDTO>> uploadInvoice(
            @PathVariable Long pharmacyId,
            @RequestParam("file") MultipartFile file) {
        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoice(pharmacyId, file);
        return ResponseEntity.ok(ApiResponse.ok("Invoice extracted for preview", result));
    }

    @PostMapping("/upload-raw/{pharmacyId}")
    public ResponseEntity<ApiResponse<InvoiceParseResultDTO>> uploadRawInvoice(
            @PathVariable Long pharmacyId,
            @RequestBody Map<String, String> payload) {
        String rawText = payload.get("rawText");
        String fileName = payload.getOrDefault("fileName", "invoice_text.txt");
        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(pharmacyId, rawText, fileName);
        return ResponseEntity.ok(ApiResponse.ok("Invoice extracted for preview", result));
    }

    /**
     * Retrieve invoice upload history for this pharmacy.
     */
    @GetMapping("/history/{pharmacyId}")
    public ResponseEntity<ApiResponse<List<InvoiceUploadRecord>>> getHistory(@PathVariable Long pharmacyId) {
        List<InvoiceUploadRecord> history = invoiceProcessingService.getInvoiceHistory(pharmacyId);
        return ResponseEntity.ok(ApiResponse.ok("Invoice history fetched", history));
    }

    /**
     * Get pre-formatted sample invoice template.
     */
    @GetMapping("/sample-template")
    public ResponseEntity<ApiResponse<String>> getSampleTemplate() {
        String template = invoiceProcessingService.getSampleInvoiceTemplate();
        return ResponseEntity.ok(ApiResponse.ok("Sample template fetched", template));
    }

    /**
     * Preview safe reversal of an imported invoice (such as INV-947875).
     */
    @GetMapping("/revert-preview/{pharmacyId}/{invoiceNumber}")
    public ResponseEntity<ApiResponse<com.pharmatech.dto.InvoiceReversalPreviewDTO>> previewReversal(
            @PathVariable Long pharmacyId,
            @PathVariable String invoiceNumber) {
        com.pharmatech.dto.InvoiceReversalPreviewDTO preview = invoiceProcessingService.previewReversal(pharmacyId, invoiceNumber);
        return ResponseEntity.ok(ApiResponse.ok("Reversal preview generated", preview));
    }

    /**
     * Confirm safe reversal of an imported invoice.
     */
    @PostMapping("/revert-confirm/{pharmacyId}/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceParseResultDTO>> confirmReversal(
            @PathVariable Long pharmacyId,
            @PathVariable String invoiceNumber) {
        InvoiceParseResultDTO result = invoiceProcessingService.confirmReversal(pharmacyId, invoiceNumber);
        return ResponseEntity.ok(ApiResponse.ok("Invoice stock safely reverted", result));
    }
}
