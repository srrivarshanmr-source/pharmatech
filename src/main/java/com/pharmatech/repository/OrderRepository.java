package com.pharmatech.repository;

import com.pharmatech.model.Customer;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Order;
import com.pharmatech.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(Customer customer);
    
    @Query("SELECT o FROM Order o WHERE o.pharmacy = :pharmacy ORDER BY CASE WHEN o.priority = 'EMERGENCY' THEN 0 ELSE 1 END, o.createdAt DESC")
    List<Order> findByPharmacyPrioritized(@Param("pharmacy") Pharmacy pharmacy);

    List<Order> findByPharmacyOrderByCreatedAtDesc(Pharmacy pharmacy);

    @Query("SELECT o FROM Order o WHERE o.deliveryPartner = :deliveryPartner ORDER BY CASE WHEN o.priority = 'EMERGENCY' THEN 0 ELSE 1 END, o.createdAt DESC")
    List<Order> findByDeliveryPartnerPrioritized(@Param("deliveryPartner") DeliveryPartner deliveryPartner);

    List<Order> findByDeliveryPartnerOrderByCreatedAtDesc(DeliveryPartner deliveryPartner);
    List<Order> findByDeliveryPartnerAndStatusIn(DeliveryPartner deliveryPartner, List<String> statuses);
    
    @Query("SELECT o FROM Order o WHERE (o.deliveryPartner = :deliveryPartner OR o.deliveryPartner IS NULL) AND o.status IN :statuses ORDER BY CASE WHEN o.priority = 'EMERGENCY' THEN 0 ELSE 1 END, o.createdAt DESC")
    List<Order> findPendingDeliveriesForPartner(@Param("deliveryPartner") DeliveryPartner deliveryPartner, @Param("statuses") List<String> statuses);

    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    long countByPharmacyAndStatus(Pharmacy pharmacy, String status);
    long countByDeliveryPartnerAndStatus(DeliveryPartner deliveryPartner, String status);
}
