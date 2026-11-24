package com.pharmatech.repository;

import com.pharmatech.model.Pharmacy;
import com.pharmatech.model.PharmacyOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByOwner(PharmacyOwner owner);
    Optional<Pharmacy> findByOwnerId(Long ownerId);
    List<Pharmacy> findByCityIgnoreCase(String city);
    List<Pharmacy> findByPincode(String pincode);
}
