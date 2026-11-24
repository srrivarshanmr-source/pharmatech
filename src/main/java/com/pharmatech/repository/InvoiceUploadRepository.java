package com.pharmatech.repository;

import com.pharmatech.model.InvoiceUploadRecord;
import com.pharmatech.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceUploadRepository extends JpaRepository<InvoiceUploadRecord, Long> {
    List<InvoiceUploadRecord> findByPharmacyOrderByUploadedAtDesc(Pharmacy pharmacy);
    boolean existsByPharmacyAndInvoiceNumber(Pharmacy pharmacy, String invoiceNumber);
    Optional<InvoiceUploadRecord> findByPharmacyAndInvoiceNumber(Pharmacy pharmacy, String invoiceNumber);
}
