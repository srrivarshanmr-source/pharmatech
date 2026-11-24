package com.pharmatech.repository;

import com.pharmatech.model.Order;
import com.pharmatech.model.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByRefundId(String refundId);

    List<PaymentRefund> findByOrder(Order order);

    List<PaymentRefund> findAllByOrderByCreatedAtDesc();
}
