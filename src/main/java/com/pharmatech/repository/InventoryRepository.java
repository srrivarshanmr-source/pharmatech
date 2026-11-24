package com.pharmatech.repository;

import com.pharmatech.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByPharmacy(com.pharmatech.model.Pharmacy pharmacy);

    List<Inventory> findByPharmacyId(Long pharmacyId);

    Optional<Inventory> findFirstByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);

    Optional<Inventory> findByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);

    Optional<Inventory> findByPharmacyIdAndMedicineIdAndBatchNumber(Long pharmacyId, Long medicineId, String batchNumber);

    List<Inventory> findAllByPharmacyIdAndMedicineId(Long pharmacyId, Long medicineId);

    long countByPharmacyId(Long pharmacyId);

    long countByPharmacyIdAndQuantityGreaterThan(Long pharmacyId, Integer quantity);

    long countByPharmacyIdAndQuantityLessThanEqualAndQuantityGreaterThan(Long pharmacyId, Integer max, Integer min);

    long countByPharmacyIdAndQuantityEquals(Long pharmacyId, Integer quantity);

    @Query("SELECT i FROM Inventory i WHERE i.pharmacy.id = :pharmacyId AND " +
           "(LOWER(i.medicine.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(i.medicine.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(i.batchNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Inventory> searchPharmacyInventory(@Param("pharmacyId") Long pharmacyId, @Param("query") String query);

    @Query("SELECT i FROM Inventory i WHERE i.medicine.id = :medicineId AND i.quantity > 0 AND i.available = true")
    List<Inventory> findAvailableStockForMedicine(@Param("medicineId") Long medicineId);
}
