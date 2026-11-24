package com.pharmatech.repository;

import com.pharmatech.model.DeliveryAssignment;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {
    List<DeliveryAssignment> findByDeliveryPartnerAndStatusOrderByAssignedAtDesc(DeliveryPartner partner, String status);
    List<DeliveryAssignment> findByOrderOrderByAssignedAtDesc(Order order);
    Optional<DeliveryAssignment> findByOrderAndDeliveryPartner(Order order, DeliveryPartner partner);
}
