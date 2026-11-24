package com.pharmatech.invoice;

import com.pharmatech.dto.InvoiceConfirmRequestDTO;
import com.pharmatech.dto.InvoiceItemDTO;
import com.pharmatech.dto.InvoiceParseResultDTO;
import com.pharmatech.dto.InvoiceReversalPreviewDTO;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.model.Inventory;
import com.pharmatech.model.InvoiceUploadRecord;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.repository.InventoryRepository;
import com.pharmatech.repository.PharmacyRepository;
import com.pharmatech.service.InvoiceProcessingService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class InvoiceProcessingIntegrationTest {

    @Autowired
    private InvoiceProcessingService invoiceProcessingService;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    @DisplayName("Invoice Extraction, Row Parsing, Two-Stage Stock Update, Duplicate Prevention, and Safe Reversal")
    void testCompleteInvoiceProcessingWorkflow() {
        Pharmacy pharmacy = pharmacyRepository.findById(1L).orElseThrow();

        // 1. Raw Invoice Content with headers, footers, row numbers, and summary lines that must be rejected
        String rawInvoiceText = "Supplier: SunPharma Healthcare Wholesale Hub\n" +
                "Invoice No: INV-AUTO-TEST-2026-001\n" +
                "Date: 2026-11-20\n" +
                "GSTIN: 29AAAAA0000A1Z5\n" +
                "SL. | Item Name | Brand | Category | Strength | Quantity | Unit Price | Expiry Date | Batch\n" +
                "1 | Dolo 650 | Micro Labs | Tablets | 650mg | 50 | 30.50 | 2028-06-30 | BAT-TEST-D1\n" +
                "2 | Augmentin 625 Duo | GSK | Tablets | 625mg | 30 | 204.00 | 2027-12-31 | BAT-TEST-A2\n" +
                "Total for 2 priced items\n" +
                "Subtotal: 7645.00\n" +
                "Grand Total: 7645.00\n" +
                "Authorized Signatory";

        // Stage 1: Extraction & Preview (Must NOT modify database inventory yet)
        int initialInventoryCount = inventoryRepository.findByPharmacy(pharmacy).size();
        InvoiceParseResultDTO preview = invoiceProcessingService.extractInvoiceRawText(1L, rawInvoiceText, "invoice.txt");

        assertNotNull(preview);
        assertEquals("INV-AUTO-TEST-2026-001", preview.getInvoiceNumber());
        assertEquals(2, preview.getItemsParsed(), "Footers and headers must not be treated as medicines");
        assertEquals(80, preview.getTotalStockAdded(), "50 + 30 units");
        assertFalse(preview.isConfirmed(), "Preview stage must not be confirmed");
        assertFalse(preview.isDuplicateDetected());

        // Validate extracted rows
        InvoiceItemDTO item1 = preview.getItems().get(0);
        assertEquals("Dolo 650", item1.getMedicineName());
        assertEquals("BAT-TEST-D1", item1.getBatchNumber());
        assertEquals(Integer.valueOf(50), item1.getQuantity());
        assertEquals(new BigDecimal("30.50"), item1.getPrice());
        assertEquals(new BigDecimal("1525.00"), item1.getLineTotal());

        InvoiceItemDTO item2 = preview.getItems().get(1);
        assertEquals("Augmentin 625 Duo", item2.getMedicineName());
        assertEquals("BAT-TEST-A2", item2.getBatchNumber());
        assertEquals(Integer.valueOf(30), item2.getQuantity());
        assertEquals(new BigDecimal("204.00"), item2.getPrice());
        assertEquals(new BigDecimal("6120.00"), item2.getLineTotal());

        // Verify database inventory was NOT changed in Stage 1
        assertEquals(initialInventoryCount, inventoryRepository.findByPharmacy(pharmacy).size());

        // Stage 2: Confirm & Commit to Inventory
        InvoiceConfirmRequestDTO confirmReq = new InvoiceConfirmRequestDTO();
        confirmReq.setInvoiceNumber(preview.getInvoiceNumber());
        confirmReq.setSupplierName(preview.getSupplierName());
        confirmReq.setFileName(preview.getFileName());
        confirmReq.setTotalValue(preview.getCalculatedTotal());
        confirmReq.setItems(preview.getItems());

        InvoiceParseResultDTO confirmResult = invoiceProcessingService.confirmAndUpdateStock(1L, confirmReq);
        assertNotNull(confirmResult);
        assertTrue(confirmResult.isConfirmed());
        assertEquals(80, confirmResult.getTotalStockAdded());

        // Verify inventory records in database
        List<Inventory> updatedInventory = inventoryRepository.findByPharmacy(pharmacy);
        Inventory savedDoloBatch = updatedInventory.stream()
                .filter(i -> "BAT-TEST-D1".equalsIgnoreCase(i.getBatchNumber()))
                .findFirst().orElse(null);
        assertNotNull(savedDoloBatch, "Batch BAT-TEST-D1 must be updated in inventory");
        assertEquals(Integer.valueOf(170), savedDoloBatch.getQuantity(), "Dolo 650 stock must increment from 120 to 170");

        // Duplicate Invoice Prevention: Re-confirming same invoice must be rejected
        assertThrows(BadRequestException.class, () -> {
            invoiceProcessingService.confirmAndUpdateStock(1L, confirmReq);
        }, "Must reject duplicate invoice confirmation to prevent double stock addition");

        // Verify extraction warns about duplicate
        InvoiceParseResultDTO duplicatePreview = invoiceProcessingService.extractInvoiceRawText(1L, rawInvoiceText, "invoice.txt");
        assertTrue(duplicatePreview.isDuplicateDetected());
        assertNotNull(duplicatePreview.getDuplicateMessage());

        // Verify invoice history tracking
        List<InvoiceUploadRecord> history = invoiceProcessingService.getInvoiceHistory(1L);
        assertFalse(history.isEmpty());
        assertEquals("INV-AUTO-TEST-2026-001", history.get(0).getInvoiceNumber());
        assertEquals("CONFIRMED", history.get(0).getStatus());

        // Verify Invoice Reversal Preview
        InvoiceReversalPreviewDTO reversalPreview = invoiceProcessingService.previewReversal(1L, "INV-AUTO-TEST-2026-001");
        assertNotNull(reversalPreview);
        assertEquals(80, reversalPreview.getTotalStockToRevert());

        // Execute Reversal Rollback
        InvoiceParseResultDTO rollbackResult = invoiceProcessingService.confirmReversal(1L, "INV-AUTO-TEST-2026-001");
        assertNotNull(rollbackResult);
        assertTrue(rollbackResult.getMessage().toLowerCase().contains("reverted"));

        // Verify stock was safely decremented back
        Inventory revertedDolo = inventoryRepository.findById(savedDoloBatch.getId()).orElse(null);
        if (revertedDolo != null) {
            assertEquals(Integer.valueOf(120), revertedDolo.getQuantity(), "Stock must be rolled back to original 120 units");
        }
    }

    @Test
    @DisplayName("Task 1 Exact Verification: DEMO-INV-005 with 5 medicines, 293 total units, and Rs. 27,609 total amount")
    void testExactDemoInv005InvoiceExtraction() {
        String exactInvoiceText = "Invoice number: DEMO-INV-005\n" +
                "Supplier: Sample Medical Distributors\n" +
                "Buyer: Apollo Pharmacy - Metro Express\n\n" +
                "Medicine 1:\n" +
                "- Name: Dolo 650\n" +
                "- Brand: Micro Labs\n" +
                "- Strength: 650 mg\n" +
                "- Quantity: 150\n" +
                "- Price: ₹30.50\n" +
                "- Batch: BAT-DOLO-01\n" +
                "- Expiry: 19-May-2028\n\n" +
                "Medicine 2:\n" +
                "- Name: Augmentin 625 Duo\n" +
                "- Brand: GSK\n" +
                "- Strength: 625 mg\n" +
                "- Quantity: 45\n" +
                "- Price: ₹204\n" +
                "- Batch: BAT-AUG-04\n" +
                "- Expiry: 17-Nov-2027\n\n" +
                "Medicine 3:\n" +
                "- Name: Pan 40\n" +
                "- Brand: Alkem\n" +
                "- Strength: 40 mg\n" +
                "- Quantity: 60\n" +
                "- Price: ₹155\n" +
                "- Batch: BAT-PAN-09\n" +
                "- Expiry: 17-Nov-2028\n\n" +
                "Medicine 4:\n" +
                "- Name: Benadryl Cough Syrup\n" +
                "- Brand: Johnson & Johnson\n" +
                "- Form: Syrup / 150 ml\n" +
                "- Quantity: 8\n" +
                "- Price: ₹138\n" +
                "- Batch: BAT-BEN-03\n" +
                "- Expiry: 17-Feb-2028\n\n" +
                "Medicine 5:\n" +
                "- Name: Ascoril D Plus Syrup\n" +
                "- Brand: Glenmark\n" +
                "- Form: Syrup / 100 ml\n" +
                "- Quantity: 30\n" +
                "- Price: ₹115\n" +
                "- Batch: BAT-ASC-11\n" +
                "- Expiry: 17-Jul-2028\n\n" +
                "All medicine details must be verified before signing.\n" +
                "Terms and Conditions apply. Authorized Signatory.";

        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(1L, exactInvoiceText, "demo_inv_005.txt");

        assertNotNull(result);
        assertEquals("DEMO-INV-005", result.getInvoiceNumber());
        assertEquals("Sample Medical Distributors", result.getSupplierName(), "Supplier must be extracted cleanly without buyer name");
        assertEquals(5, result.getItemsParsed(), "Total medicine rows must be exactly 5");
        assertEquals(293, result.getTotalStockAdded(), "Total units must be exactly 293 (150+45+60+8+30)");
        assertEquals(new BigDecimal("27609.00"), result.getCalculatedTotal(), "Invoice amount must be ₹27,609");

        // Validate individual items
        InvoiceItemDTO item1 = result.getItems().get(0);
        assertEquals("Dolo 650", item1.getMedicineName());
        assertEquals("Micro Labs", item1.getBrand());
        assertEquals("650 mg", item1.getStrength());
        assertEquals(Integer.valueOf(150), item1.getQuantity());
        assertEquals(new BigDecimal("30.50"), item1.getPrice());
        assertEquals("BAT-DOLO-01", item1.getBatchNumber());
        assertEquals(new BigDecimal("4575.00"), item1.getLineTotal());

        InvoiceItemDTO item2 = result.getItems().get(1);
        assertEquals("Augmentin 625 Duo", item2.getMedicineName());
        assertEquals(Integer.valueOf(45), item2.getQuantity());
        assertEquals(new BigDecimal("204.00"), item2.getPrice());
        assertEquals("BAT-AUG-04", item2.getBatchNumber());
        assertEquals(new BigDecimal("9180.00"), item2.getLineTotal());

        InvoiceItemDTO item3 = result.getItems().get(2);
        assertEquals("Pan 40", item3.getMedicineName());
        assertEquals(Integer.valueOf(60), item3.getQuantity());
        assertEquals(new BigDecimal("155.00"), item3.getPrice());
        assertEquals("BAT-PAN-09", item3.getBatchNumber());
        assertEquals(new BigDecimal("9300.00"), item3.getLineTotal());

        InvoiceItemDTO item4 = result.getItems().get(3);
        assertEquals("Benadryl Cough Syrup", item4.getMedicineName());
        assertEquals("Johnson & Johnson", item4.getBrand());
        assertEquals(Integer.valueOf(8), item4.getQuantity());
        assertEquals(new BigDecimal("138.00"), item4.getPrice());
        assertEquals("BAT-BEN-03", item4.getBatchNumber());
        assertEquals(new BigDecimal("1104.00"), item4.getLineTotal());

        InvoiceItemDTO item5 = result.getItems().get(4);
        assertEquals("Ascoril D Plus Syrup", item5.getMedicineName());
        assertEquals("Glenmark", item5.getBrand());
        assertEquals(Integer.valueOf(30), item5.getQuantity());
        assertEquals(new BigDecimal("115.00"), item5.getPrice());
        assertEquals("BAT-ASC-11", item5.getBatchNumber());
        assertEquals(new BigDecimal("3450.00"), item5.getLineTotal());

        // Confirm & Update Stock
        InvoiceConfirmRequestDTO confirmReq = new InvoiceConfirmRequestDTO();
        confirmReq.setInvoiceNumber(result.getInvoiceNumber());
        confirmReq.setSupplierName(result.getSupplierName());
        confirmReq.setFileName(result.getFileName());
        confirmReq.setTotalValue(result.getCalculatedTotal());
        confirmReq.setItems(result.getItems());

        InvoiceParseResultDTO confirmed = invoiceProcessingService.confirmAndUpdateStock(1L, confirmReq);
        assertNotNull(confirmed);
        assertTrue(confirmed.isConfirmed());
        assertEquals(293, confirmed.getTotalStockAdded());

        // Duplicate rejection
        assertThrows(BadRequestException.class, () -> {
            invoiceProcessingService.confirmAndUpdateStock(1L, confirmReq);
        });
    }

    @Test
    @DisplayName("Task 1 Space-Separated Tabular PDF Extraction: 5 medicines, 293 units, Rs. 27,609, no strength-as-quantity confusion")
    void testTabularSpaceSeparatedPdfInvoiceExtraction() {
        String spaceSeparatedInvoice = "Invoice No: DEMO-INV-005\n" +
                "Supplier: Sample Medical Distributors\n" +
                "Date: 20-Nov-2026\n" +
                "Medicine Manufacturer Strength Batch Expiry Quantity Purchase Price Amount\n" +
                "Dolo 650 Micro Labs 650 mg BAT-DOLO-01 19-May-2028 150 30.50 4575.00\n" +
                "Augmentin 625 Duo GSK 625 mg BAT-AUG-04 17-Nov-2027 45 204.00 9180.00\n" +
                "Pan 40 Alkem 40 mg BAT-PAN-09 17-Nov-2028 60 155.00 9300.00\n" +
                "Benadryl Cough Syrup Johnson & Johnson 150 ml BAT-BEN-03 17-Feb-2028 8 138.00 1104.00\n" +
                "Ascoril D Plus Syrup Glenmark 100 ml BAT-ASC-11 17-Jul-2028 30 115.00 3450.00\n" +
                "Grand Total: 27609.00\n" +
                "Authorized Signatory";

        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(1L, spaceSeparatedInvoice, "demo_inv_005_table.txt");

        assertNotNull(result);
        assertEquals("DEMO-INV-005", result.getInvoiceNumber());
        assertEquals("Sample Medical Distributors", result.getSupplierName());
        assertEquals(5, result.getItemsParsed());
        assertEquals(293, result.getTotalStockAdded(), "Total quantity must be exactly 293 units (150+45+60+8+30)");
        assertEquals(new BigDecimal("27609.00"), result.getCalculatedTotal(), "Total invoice value must be exactly ₹27,609.00");

        // Validate Medicine 1: Dolo 650 (Quantity must be 150, NOT 650! Price must be 30.50, NOT 650!)
        InvoiceItemDTO dolo = result.getItems().get(0);
        assertEquals("Dolo 650", dolo.getMedicineName());
        assertEquals("Micro Labs", dolo.getBrand());
        assertEquals("650 mg", dolo.getStrength());
        assertEquals(Integer.valueOf(150), dolo.getQuantity(), "Quantity must be 150, NOT 650");
        assertEquals(new BigDecimal("30.50"), dolo.getPrice(), "Price must be 30.50, NOT 650");
        assertEquals("BAT-DOLO-01", dolo.getBatchNumber());
        assertEquals(new BigDecimal("4575.00"), dolo.getLineTotal());

        // Validate Medicine 2: Augmentin 625 Duo (Quantity must be 45, NOT 625!)
        InvoiceItemDTO aug = result.getItems().get(1);
        assertEquals("Augmentin 625 Duo", aug.getMedicineName());
        assertEquals("GSK", aug.getBrand());
        assertEquals("625 mg", aug.getStrength());
        assertEquals(Integer.valueOf(45), aug.getQuantity(), "Quantity must be 45, NOT 625");
        assertEquals(new BigDecimal("204.00"), aug.getPrice());
        assertEquals("BAT-AUG-04", aug.getBatchNumber());
        assertEquals(new BigDecimal("9180.00"), aug.getLineTotal());

        // Validate Medicine 3: Pan 40 (Quantity must be 60, NOT 40!)
        InvoiceItemDTO pan = result.getItems().get(2);
        assertEquals("Pan 40", pan.getMedicineName());
        assertEquals("Alkem", pan.getBrand());
        assertEquals("40 mg", pan.getStrength());
        assertEquals(Integer.valueOf(60), pan.getQuantity(), "Quantity must be 60, NOT 40");
        assertEquals(new BigDecimal("155.00"), pan.getPrice());
        assertEquals("BAT-PAN-09", pan.getBatchNumber());
        assertEquals(new BigDecimal("9300.00"), pan.getLineTotal());

        // Validate Medicine 4: Benadryl Cough Syrup (Quantity must be 8, NOT 150 ml!)
        InvoiceItemDTO ben = result.getItems().get(3);
        assertEquals("Benadryl Cough Syrup", ben.getMedicineName());
        assertEquals("Johnson & Johnson", ben.getBrand());
        assertEquals("150 ml", ben.getStrength());
        assertEquals("Syrup", ben.getDosageForm());
        assertEquals(Integer.valueOf(8), ben.getQuantity(), "Quantity must be 8, NOT 150");
        assertEquals(new BigDecimal("138.00"), ben.getPrice());
        assertEquals("BAT-BEN-03", ben.getBatchNumber());
        assertEquals(new BigDecimal("1104.00"), ben.getLineTotal());

        // Validate Medicine 5: Ascoril D Plus Syrup (Quantity must be 30, NOT 100 ml!)
        InvoiceItemDTO asc = result.getItems().get(4);
        assertEquals("Ascoril D Plus Syrup", asc.getMedicineName());
        assertEquals("Glenmark", asc.getBrand());
        assertEquals("100 ml", asc.getStrength());
        assertEquals("Syrup", asc.getDosageForm());
        assertEquals(Integer.valueOf(30), asc.getQuantity(), "Quantity must be 30, NOT 100");
        assertEquals(new BigDecimal("115.00"), asc.getPrice());
        assertEquals("BAT-ASC-11", asc.getBatchNumber());
        assertEquals(new BigDecimal("3450.00"), asc.getLineTotal());
    }

    @Test
    @DisplayName("Task 1 Markdown Table Invoice Extraction: 5 medicines, 293 units, Rs. 27,609")
    void testMarkdownTableInvoiceExtraction() {
        String markdownTable = "Invoice No: DEMO-INV-005\n" +
                "Supplier: Sample Medical Distributors\n" +
                "| Medicine | Manufacturer | Strength | Quantity | Purchase Price |\n" +
                "|---|---|---|---|---|\n" +
                "| Dolo 650 | Micro Labs | 650 mg | 150 | 30.50 |\n" +
                "| Augmentin 625 Duo | GSK | 625 mg | 45 | 204.00 |\n" +
                "| Pan 40 | Alkem | 40 mg | 60 | 155.00 |\n" +
                "| Benadryl Cough Syrup | Johnson & Johnson | 150 ml | 8 | 138.00 |\n" +
                "| Ascoril D Plus Syrup | Glenmark | 100 ml | 30 | 115.00 |\n" +
                "Grand Total: 27609.00";

        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(1L, markdownTable, "markdown_invoice.md");

        assertNotNull(result);
        assertEquals(5, result.getItemsParsed());
        assertEquals(293, result.getTotalStockAdded());
        assertEquals(new BigDecimal("27609.00"), result.getCalculatedTotal());

        assertEquals(Integer.valueOf(150), result.getItems().get(0).getQuantity());
        assertEquals(Integer.valueOf(45), result.getItems().get(1).getQuantity());
        assertEquals(Integer.valueOf(60), result.getItems().get(2).getQuantity());
        assertEquals(Integer.valueOf(8), result.getItems().get(3).getQuantity());
        assertEquals(Integer.valueOf(30), result.getItems().get(4).getQuantity());
    }

    @Test
    @DisplayName("Task 1 PDFBox In-Memory PDF Document Extraction: 5 medicines, 293 units, Rs. 27,609")
    void testPdfBoxBinaryInvoiceExtraction() throws Exception {
        byte[] pdfBytes;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                stream.newLineAtOffset(50, 750);
                stream.showText("Invoice No: DEMO-INV-005");
                stream.newLineAtOffset(0, -20);
                stream.showText("Supplier: Sample Medical Distributors");
                stream.newLineAtOffset(0, -25);
                stream.setFont(PDType1Font.HELVETICA, 10);
                stream.showText("Dolo 650 Micro Labs 650 mg BAT-DOLO-01 19-May-2028 150 30.50 4575.00");
                stream.newLineAtOffset(0, -18);
                stream.showText("Augmentin 625 Duo GSK 625 mg BAT-AUG-04 17-Nov-2027 45 204.00 9180.00");
                stream.newLineAtOffset(0, -18);
                stream.showText("Pan 40 Alkem 40 mg BAT-PAN-09 17-Nov-2028 60 155.00 9300.00");
                stream.newLineAtOffset(0, -18);
                stream.showText("Benadryl Cough Syrup Johnson & Johnson 150 ml BAT-BEN-03 17-Feb-2028 8 138.00 1104.00");
                stream.newLineAtOffset(0, -18);
                stream.showText("Ascoril D Plus Syrup Glenmark 100 ml BAT-ASC-11 17-Jul-2028 30 115.00 3450.00");
                stream.newLineAtOffset(0, -25);
                stream.showText("Grand Total: 27609.00");
                stream.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MockMultipartFile mockPdf = new MockMultipartFile("file", "DEMO-INV-005.pdf", "application/pdf", pdfBytes);
        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoice(1L, mockPdf);

        assertNotNull(result);
        assertEquals(5, result.getItemsParsed());
        assertEquals(293, result.getTotalStockAdded());
        assertEquals(new BigDecimal("27609.00"), result.getCalculatedTotal());
    }

    @Test
    @DisplayName("Task 1 Multiline Tabular PDF Row Stitching")
    void testMultilineTabularPdfInvoiceExtraction() {
        String multilineInvoice = "Invoice No: DEMO-INV-005\n" +
                "Supplier: Sample Medical Distributors\n" +
                "Dolo 650 Micro Labs\n" +
                "650 mg BAT-DOLO-01 19-May-2028 150 30.50 4575.00\n" +
                "Benadryl Cough Syrup Johnson & Johnson\n" +
                "150 ml BAT-BEN-03 17-Feb-2028 8 138.00 1104.00\n";

        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(1L, multilineInvoice, "multiline.txt");

        assertNotNull(result);
        assertEquals(2, result.getItemsParsed());
        assertEquals(158, result.getTotalStockAdded(), "150 + 8 units");

        assertEquals("Dolo 650", result.getItems().get(0).getMedicineName());
        assertEquals(Integer.valueOf(150), result.getItems().get(0).getQuantity());

        assertEquals("Benadryl Cough Syrup", result.getItems().get(1).getMedicineName());
        assertEquals(Integer.valueOf(8), result.getItems().get(1).getQuantity());
    }

    @Test
    @DisplayName("Task 1 Math Mismatch Detection: line total calculation differs from declared value")
    void testMathMismatchFlaggedForManualVerification() {
        String corruptedLine = "Invoice No: DEMO-INV-005\n" +
                "Supplier: Sample Medical Distributors\n" +
                "Dolo 650 Micro Labs 650 mg BAT-DOLO-01 19-May-2028 150 30.50 9999.00\n";

        InvoiceParseResultDTO result = invoiceProcessingService.extractInvoiceRawText(1L, corruptedLine, "corrupted.txt");

        assertNotNull(result);
        assertEquals(1, result.getItemsParsed());
        InvoiceItemDTO item = result.getItems().get(0);
        assertEquals("MISMATCHED_TOTAL", item.getValidationStatus());
        assertTrue(item.getNeedsVerification(), "Row with math mismatch must be flagged for manual review");
    }
}
