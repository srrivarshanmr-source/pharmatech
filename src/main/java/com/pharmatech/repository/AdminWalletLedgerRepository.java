package com.pharmatech.repository;

import com.pharmatech.model.AdminWalletLedger;
import com.pharmatech.model.Order;
import com.pharmatech.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminWalletLedgerRepository extends JpaRepository<AdminWalletLedger, Long> {
    List<AdminWalletLedger> findAllByOrderByCreatedAtDesc();
    List<AdminWalletLedger> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    List<AdminWalletLedger> findByOrderOrderByCreatedAtDesc(Order order);
    Optional<AdminWalletLedger> findFirstByOrderAndSettlementStatus(Order order, String settlementStatus);
    Optional<AdminWalletLedger> findFirstByOrderAndPaymentStatus(Order order, String paymentStatus);
}
