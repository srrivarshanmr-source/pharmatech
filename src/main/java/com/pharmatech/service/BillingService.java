package com.pharmatech.service;

import com.pharmatech.dto.BillRequestDTO;
import com.pharmatech.dto.BillResponseDTO;

import java.util.List;

public interface BillingService {
    BillResponseDTO createBill(BillRequestDTO request);
    BillResponseDTO getBillById(Long billId);
    BillResponseDTO getBillByNumber(String billNumber);
    List<BillResponseDTO> getBillsByPharmacy(Long pharmacyId);
    BillResponseDTO cancelBill(Long billId);
}
