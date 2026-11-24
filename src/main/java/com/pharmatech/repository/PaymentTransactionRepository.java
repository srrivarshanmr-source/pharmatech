package com.pharmatech.repository;

import com.pharmatech.model.Order;
import com.pharmatech.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrderOrderByCreatedAtDesc(Order order);
    Optional<PaymentTransaction> findFirstByOrderOrderByCreatedAtDesc(Order order);
    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);
    Optional<PaymentTransaction> findByRazorpayPaymentId(String razorpayPaymentId);
    Optional<PaymentTransaction> findByRazorpayQrId(String razorpayQrId);
}
