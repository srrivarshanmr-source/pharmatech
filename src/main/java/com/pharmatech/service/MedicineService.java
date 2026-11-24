package com.pharmatech.service;

import com.pharmatech.model.Medicine;
import java.math.BigDecimal;
import java.util.List;

/**
 * Medicine service interface demonstrating ABSTRACTION and METHOD OVERLOADING (Polymorphism).
 */
public interface MedicineService {

    List<Medicine> getAllMedicines();

    Medicine getMedicineById(Long id);

    List<Medicine> getMedicinesByCategory(String category);

    List<String> getAllCategories();

    /**
     * Search method signature 1 (Overloaded)
     * Searches by keyword across medicine name and brand.
     */
    List<Medicine> searchMedicines(String query);

    /**
     * Search method signature 2 (Overloaded)
     * Searches by keyword within a specific category.
     */
    List<Medicine> searchMedicines(String query, String category);

    /**
     * Search method signature 3 (Overloaded)
     * Searches by keyword and category with a maximum budget filter.
     */
    List<Medicine> searchMedicines(String query, String category, BigDecimal maxPrice);

    Medicine createOrUpdateMedicine(Medicine medicine);
}
