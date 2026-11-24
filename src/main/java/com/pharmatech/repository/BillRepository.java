package com.pharmatech.repository;

import com.pharmatech.model.Bill;
import com.pharmatech.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    Optional<Bill> findByBillNumber(String billNumber);
    long countByPharmacy(Pharmacy pharmacy);
}
