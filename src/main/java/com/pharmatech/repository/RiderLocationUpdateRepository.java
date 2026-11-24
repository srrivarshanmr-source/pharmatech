package com.pharmatech.repository;

import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Order;
import com.pharmatech.model.RiderLocationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiderLocationUpdateRepository extends JpaRepository<RiderLocationUpdate, Long> {
    List<RiderLocationUpdate> findByOrderOrderByRecordedAtDesc(Order order);
    Optional<RiderLocationUpdate> findFirstByOrderOrderByRecordedAtDesc(Order order);
    Optional<RiderLocationUpdate> findFirstByDeliveryPartnerOrderByRecordedAtDesc(DeliveryPartner deliveryPartner);
}
