package com.pharmatech.repository;

import com.pharmatech.model.Pharmacy;
import com.pharmatech.model.PharmacyOwner;
import com.pharmatech.model.PharmacyPayoutAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyPayoutAccountRepository extends JpaRepository<PharmacyPayoutAccount, Long> {

    Optional<PharmacyPayoutAccount> findByPharmacy(Pharmacy pharmacy);

    Optional<PharmacyPayoutAccount> findByPharmacyId(Long pharmacyId);

    List<PharmacyPayoutAccount> findByOwner(PharmacyOwner owner);
}
