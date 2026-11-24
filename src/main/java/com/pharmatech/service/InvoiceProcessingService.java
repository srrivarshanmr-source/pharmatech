package com.pharmatech.service;

import com.pharmatech.dto.InvoiceConfirmRequestDTO;
import com.pharmatech.dto.InvoiceParseResultDTO;
import com.pharmatech.model.InvoiceUploadRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InvoiceProcessingService {
    
    /**
     * Extracts medicine details from an uploaded file (PDF, JPG, JPEG, PNG, TXT, CSV)
     * without modifying the database, returning an editable preview.
     */
    InvoiceParseResultDTO extractInvoice(Long pharmacyId, MultipartFile file);

    /**
     * Extracts medicine details from raw invoice text without modifying the database.
     */
    InvoiceParseResultDTO extractInvoiceRawText(Long pharmacyId, String rawText, String fileName);

    /**
     * Confirms the extracted/edited invoice items and updates the pharmacy stock in database.
     * Prevents duplicate additions.
     */
    InvoiceParseResultDTO confirmAndUpdateStock(Long pharmacyId, InvoiceConfirmRequestDTO request);

    /**
     * Fetch upload invoice history for a pharmacy.
     */
    List<InvoiceUploadRecord> getInvoiceHistory(Long pharmacyId);

    /**
     * Sample invoice format template for quick demo.
     */
    String getSampleInvoiceTemplate();

    /**
     * Preview safe reversal of an imported invoice (such as INV-947875).
     */
    com.pharmatech.dto.InvoiceReversalPreviewDTO previewReversal(Long pharmacyId, String invoiceNumber);

    /**
     * Execute safe reversal of an imported invoice, updating stock and creating audit movement logs.
     */
    InvoiceParseResultDTO confirmReversal(Long pharmacyId, String invoiceNumber);
}
