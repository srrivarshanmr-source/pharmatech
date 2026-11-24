package com.pharmatech.repository;

import com.pharmatech.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByNameIgnoreCaseAndStrengthIgnoreCase(String name, String strength);

    List<Medicine> findByCategoryIgnoreCase(String category);

    @Query("SELECT m FROM Medicine m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.brand) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Medicine> searchByNameOrBrand(@Param("query") String query);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.brand) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND LOWER(m.category) = LOWER(:category)")
    List<Medicine> searchByNameOrBrandAndCategory(@Param("query") String query, @Param("category") String category);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.brand) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND LOWER(m.category) = LOWER(:category) " +
           "AND m.basePrice <= :maxPrice")
    List<Medicine> searchWithPriceFilter(@Param("query") String query,
                                         @Param("category") String category,
                                         @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT DISTINCT m.category FROM Medicine m")
    List<String> findDistinctCategories();
}
