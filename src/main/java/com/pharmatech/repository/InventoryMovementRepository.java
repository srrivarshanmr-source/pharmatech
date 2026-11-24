package com.pharmatech.repository;

import com.pharmatech.model.InventoryMovement;
import com.pharmatech.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);
    List<InventoryMovement> findByPharmacyAndReferenceNumber(Pharmacy pharmacy, String referenceNumber);
    List<InventoryMovement> findByReferenceNumber(String referenceNumber);
}
