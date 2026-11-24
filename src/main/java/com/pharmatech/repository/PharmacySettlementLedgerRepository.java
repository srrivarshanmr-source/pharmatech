package com.pharmatech.repository;

import com.pharmatech.model.Order;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.model.PharmacySettlementLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacySettlementLedgerRepository extends JpaRepository<PharmacySettlementLedger, Long> {
    List<PharmacySettlementLedger> findAllByOrderByCreatedAtDesc();
    List<PharmacySettlementLedger> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    List<PharmacySettlementLedger> findByOrderOrderByCreatedAtDesc(Order order);
    Optional<PharmacySettlementLedger> findFirstByOrderAndSettlementStatus(Order order, String settlementStatus);
}
