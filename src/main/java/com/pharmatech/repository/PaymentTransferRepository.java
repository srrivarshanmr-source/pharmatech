package com.pharmatech.repository;

import com.pharmatech.model.Order;
import com.pharmatech.model.PaymentTransfer;
import com.pharmatech.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransferRepository extends JpaRepository<PaymentTransfer, Long> {

    Optional<PaymentTransfer> findByTransferId(String transferId);

    Optional<PaymentTransfer> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<PaymentTransfer> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    List<PaymentTransfer> findByPharmacyIdOrderByCreatedAtDesc(Long pharmacyId);

    List<PaymentTransfer> findByOrder(Order order);

    List<PaymentTransfer> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT COALESCE(SUM(t.grossAmount), 0) FROM PaymentTransfer t WHERE t.pharmacy.id = :pharmacyId")
    BigDecimal sumGrossByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT COALESCE(SUM(t.ownerShareAmount), 0) FROM PaymentTransfer t WHERE t.pharmacy.id = :pharmacyId AND t.status = 'PROCESSED'")
    BigDecimal sumSettledByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT COALESCE(SUM(t.ownerShareAmount), 0) FROM PaymentTransfer t WHERE t.pharmacy.id = :pharmacyId AND t.status = 'SETTLEMENT_PENDING'")
    BigDecimal sumPendingByPharmacyId(@Param("pharmacyId") Long pharmacyId);

    @Query("SELECT COALESCE(SUM(t.commissionAmount), 0) FROM PaymentTransfer t WHERE t.pharmacy.id = :pharmacyId")
    BigDecimal sumCommissionByPharmacyId(@Param("pharmacyId") Long pharmacyId);
}
