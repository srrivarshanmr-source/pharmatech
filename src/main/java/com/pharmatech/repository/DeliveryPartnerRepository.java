package com.pharmatech.repository;

import com.pharmatech.model.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByEmail(String email);
    List<DeliveryPartner> findByAvailableTrue();
    List<DeliveryPartner> findByOnlineTrueAndAvailableTrue();
    List<DeliveryPartner> findByAssignedCity(String city);
    List<DeliveryPartner> findByAssignedCityAndOnlineTrueAndAvailableTrue(String city);
}
