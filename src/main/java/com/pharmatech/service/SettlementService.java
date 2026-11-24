package com.pharmatech.service;

import com.pharmatech.dto.AdminCommissionSettingDTO;
import com.pharmatech.dto.AdminPaymentLedgerDTO;
import com.pharmatech.dto.PharmacySettlementDTO;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Order;
import com.pharmatech.model.PaymentTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SettlementService {

    BigDecimal getAdminCommissionRate();

    void updateAdminCommissionRate(BigDecimal rate);

    String getAdminUpiId();

    void updateAdminUpiId(String upiId);

    AdminCommissionSettingDTO getCommissionSetting();

    void updateCommissionSetting(AdminCommissionSettingDTO dto);

    void processPrepaidSettlement(Order order, PaymentTransaction txn);

    void processCodCollection(Order order, DeliveryPartner partner);

    void recordFailedOrCancelledTransaction(Order order, String method, String status, String maskedIdentifier, String reason);

    void reverseSettlementForOrder(Order order, String reason);

    List<AdminPaymentLedgerDTO> getAllAdminLedgerEntries(String status, Long pharmacyId, String date, String search);

    Map<String, Object> getAdminPaymentSummary();

    Map<String, Object> getPharmacyEarningsSummary(Long pharmacyId);

    List<PharmacySettlementDTO> getPharmacySettlements(Long pharmacyId);

    Map<String, Object> getPharmacyPayoutAccount(Long pharmacyId);

    void markTransferProcessed(Long transferId);

    Map<String, Object> autoSettleAllPendingOrders();
}
