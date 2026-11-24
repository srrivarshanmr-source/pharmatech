package com.pharmatech.service.impl;

import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.model.Medicine;
import com.pharmatech.repository.MedicineRepository;
import com.pharmatech.service.MedicineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service implementation demonstrating METHOD OVERLOADING (Polymorphism) and ABSTRACTION.
 */
@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;

    public MedicineServiceImpl(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    @Override
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    @Override
    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", id));
    }

    @Override
    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategoryIgnoreCase(category);
    }

    @Override
    public List<String> getAllCategories() {
        return medicineRepository.findDistinctCategories();
    }

    /**
     * Polymorphic Overloaded Search 1: by query keyword only.
     */
    @Override
    public List<Medicine> searchMedicines(String query) {
        if (query == null || query.trim().isEmpty()) {
            return medicineRepository.findAll();
        }
        return medicineRepository.searchByNameOrBrand(query.trim());
    }

    /**
     * Polymorphic Overloaded Search 2: by query keyword AND category.
     */
    @Override
    public List<Medicine> searchMedicines(String query, String category) {
        if ((category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) &&
            (query == null || query.trim().isEmpty())) {
            return medicineRepository.findAll();
        }
        if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) {
            return searchMedicines(query);
        }
        if (query == null || query.trim().isEmpty()) {
            return medicineRepository.findByCategoryIgnoreCase(category.trim());
        }
        return medicineRepository.searchByNameOrBrandAndCategory(query.trim(), category.trim());
    }

    /**
     * Polymorphic Overloaded Search 3: by query keyword, category, and budget limit.
     */
    @Override
    public List<Medicine> searchMedicines(String query, String category, BigDecimal maxPrice) {
        if (maxPrice == null) {
            return searchMedicines(query, category);
        }
        return medicineRepository.searchWithPriceFilter(
                query != null ? query.trim() : "",
                category != null ? category.trim() : "",
                maxPrice
        );
    }

    @Override
    public Medicine createOrUpdateMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }
}
