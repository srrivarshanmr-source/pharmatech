package com.pharmatech.repository;

import com.pharmatech.model.CashRemittance;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashRemittanceRepository extends JpaRepository<CashRemittance, Long> {

    List<CashRemittance> findByDeliveryPartnerOrderByCollectedAtDesc(DeliveryPartner deliveryPartner);

    Optional<CashRemittance> findByOrder(Order order);

    List<CashRemittance> findByRemittanceStatus(String remittanceStatus);

    @Query("SELECT COALESCE(SUM(c.amountCollected), 0) FROM CashRemittance c WHERE c.deliveryPartner = :partner AND c.remittanceStatus = :status")
    BigDecimal sumAmountByDeliveryPartnerAndStatus(@Param("partner") DeliveryPartner partner, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(c.amountCollected), 0) FROM CashRemittance c WHERE c.remittanceStatus = :status")
    BigDecimal sumTotalByStatus(@Param("status") String status);
}
