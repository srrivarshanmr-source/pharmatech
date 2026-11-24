package com.pharmatech.repository;

import com.pharmatech.model.PharmacyOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PharmacyOwnerRepository extends JpaRepository<PharmacyOwner, Long> {
    Optional<PharmacyOwner> findByEmail(String email);
    Optional<PharmacyOwner> findByLicenseNumber(String licenseNumber);
}
