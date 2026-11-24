package com.pharmatech.service.impl;

import com.pharmatech.dto.AdminCommissionSettingDTO;
import com.pharmatech.dto.AdminPaymentLedgerDTO;
import com.pharmatech.dto.PharmacySettlementDTO;
import com.pharmatech.model.*;
import com.pharmatech.repository.*;
import com.pharmatech.service.SettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SettlementServiceImpl implements SettlementService {

    private final AdminSettingRepository adminSettingRepository;
    private final AdminWalletLedgerRepository adminWalletLedgerRepository;
    private final PharmacySettlementLedgerRepository pharmacySettlementLedgerRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PharmacyRepository pharmacyRepository;
    private final OrderRepository orderRepository;
    private final PharmacyPayoutAccountRepository pharmacyPayoutAccountRepository;
    private final PaymentTransferRepository paymentTransferRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final com.pharmatech.repository.CashRemittanceRepository cashRemittanceRepository;

    private static final String SETTING_COMMISSION = "commission_percentage";
    private static final String SETTING_ADMIN_UPI = "admin_upi_id";
    private static final BigDecimal DEFAULT_COMMISSION = new BigDecimal("5.0");
    private static final String DEFAULT_ADMIN_UPI = "srrivarshanmr@okaxis";

    public SettlementServiceImpl(AdminSettingRepository adminSettingRepository,
                                 AdminWalletLedgerRepository adminWalletLedgerRepository,
                                 PharmacySettlementLedgerRepository pharmacySettlementLedgerRepository,
                                 PaymentTransactionRepository paymentTransactionRepository,
                                 PharmacyRepository pharmacyRepository,
                                 OrderRepository orderRepository,
                                 PharmacyPayoutAccountRepository pharmacyPayoutAccountRepository,
                                 PaymentTransferRepository paymentTransferRepository,
                                 PaymentRefundRepository paymentRefundRepository,
                                 com.pharmatech.repository.CashRemittanceRepository cashRemittanceRepository) {
        this.adminSettingRepository = adminSettingRepository;
        this.adminWalletLedgerRepository = adminWalletLedgerRepository;
        this.pharmacySettlementLedgerRepository = pharmacySettlementLedgerRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.orderRepository = orderRepository;
        this.pharmacyPayoutAccountRepository = pharmacyPayoutAccountRepository;
        this.paymentTransferRepository = paymentTransferRepository;
        this.paymentRefundRepository = paymentRefundRepository;
        this.cashRemittanceRepository = cashRemittanceRepository;
    }

    @Override
    public BigDecimal getAdminCommissionRate() {
        return adminSettingRepository.findBySettingKey(SETTING_COMMISSION)
                .map(s -> {
                    try {
                        return new BigDecimal(s.getSettingValue());
                    } catch (Exception e) {
                        return DEFAULT_COMMISSION;
                    }
                })
                .orElse(DEFAULT_COMMISSION);
    }

    @Override
    public void updateAdminCommissionRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("100.0")) > 0) {
            rate = DEFAULT_COMMISSION;
        }
        AdminSetting setting = adminSettingRepository.findBySettingKey(SETTING_COMMISSION)
                .orElse(new AdminSetting(SETTING_COMMISSION, rate.toPlainString(), "Admin Platform Commission Percentage"));
        setting.setSettingValue(rate.setScale(2, RoundingMode.HALF_UP).toPlainString());
        setting.setUpdatedAt(LocalDateTime.now());
        adminSettingRepository.save(setting);
    }

    @Override
    public String getAdminUpiId() {
        return adminSettingRepository.findBySettingKey(SETTING_ADMIN_UPI)
                .map(AdminSetting::getSettingValue)
                .orElse(DEFAULT_ADMIN_UPI);
    }

    @Override
    public void updateAdminUpiId(String upiId) {
        if (upiId == null || upiId.trim().isEmpty()) {
            upiId = DEFAULT_ADMIN_UPI;
        }
        AdminSetting setting = adminSettingRepository.findBySettingKey(SETTING_ADMIN_UPI)
                .orElse(new AdminSetting(SETTING_ADMIN_UPI, upiId.trim(), "Configurable Admin Platform UPI ID for Display"));
        setting.setSettingValue(upiId.trim());
        setting.setUpdatedAt(LocalDateTime.now());
        adminSettingRepository.save(setting);
    }

    @Override
    public AdminCommissionSettingDTO getCommissionSetting() {
        return new AdminCommissionSettingDTO(getAdminCommissionRate(), getAdminUpiId());
    }

    @Override
    public void updateCommissionSetting(AdminCommissionSettingDTO dto) {
        if (dto.getCommissionPercentage() != null) {
            updateAdminCommissionRate(dto.getCommissionPercentage());
        }
        if (dto.getAdminUpiId() != null && !dto.getAdminUpiId().trim().isEmpty()) {
            updateAdminUpiId(dto.getAdminUpiId());
        }
    }

    @Override
    public void processPrepaidSettlement(Order order, PaymentTransaction txn) {
        if (order == null) return;

        String idempotencyKey = order.getId() + "_" + (order.getPharmacy() != null ? order.getPharmacy().getId() : 0);

        // Idempotency check: prevent duplicate settlement
        if (paymentTransferRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        BigDecimal grossAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal commissionRate = getAdminCommissionRate();
        BigDecimal commissionAmount = grossAmount.multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal netSettlement = grossAmount.subtract(commissionAmount);

        String txnRef = (txn != null && txn.getRazorpayPaymentId() != null)
                ? txn.getRazorpayPaymentId()
                : "TXN-DEMO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        String masked = null;
        if (txn != null) {
            if (txn.getMaskedUpiId() != null) {
                masked = txn.getMaskedUpiId();
            } else if (txn.getCardLast4() != null) {
                masked = "•••• " + txn.getCardLast4();
            }
        }

        String method = order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "ONLINE";

        // 1. Credit Admin Demo Wallet Ledger
        AdminWalletLedger adminLedger = new AdminWalletLedger(
                txnRef, order, order.getCustomer(), order.getPharmacy(),
                method, grossAmount, commissionRate, commissionAmount, netSettlement,
                "TEST_SUCCESS", "SETTLED", masked, "Prepaid order automatic settlement to " + (order.getPharmacy() != null ? order.getPharmacy().getName() : "Owner")
        );
        adminWalletLedgerRepository.save(adminLedger);

        // 2. Credit Pharmacy Settlement Ledger
        if (order.getPharmacy() != null) {
            PharmacySettlementLedger pharmacyLedger = new PharmacySettlementLedger(
                    order.getPharmacy(), order, txnRef, method,
                    grossAmount, commissionRate, commissionAmount, netSettlement, "SETTLED"
            );
            pharmacySettlementLedgerRepository.save(pharmacyLedger);
        }

        // 3. Create Direct PaymentTransfer record (Razorpay Route pattern)
        if (order.getPharmacy() != null) {
            PharmacyPayoutAccount payoutAccount = pharmacyPayoutAccountRepository.findByPharmacy(order.getPharmacy()).orElse(null);
            String transferId = "trf_route_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            PaymentTransfer transfer = new PaymentTransfer(
                    transferId, order, order.getPharmacy(), txn, payoutAccount,
                    grossAmount, commissionAmount, netSettlement, "PROCESSED", idempotencyKey,
                    "Automated Route transfer for Order #" + order.getOrderNumber()
            );
            paymentTransferRepository.save(transfer);
        }

        if (txn != null) {
            txn.setSettlementStatus("SETTLED");
            paymentTransactionRepository.save(txn);
        }
    }

    @Override
    public void processCodCollection(Order order, DeliveryPartner partner) {
        if (order == null) return;

        String idempotencyKey = "COD_" + order.getId() + "_" + (order.getPharmacy() != null ? order.getPharmacy().getId() : 0);

        // Idempotency check
        if (paymentTransferRepository.existsByIdempotencyKey(idempotencyKey)) {
            return;
        }

        BigDecimal grossAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal commissionRate = getAdminCommissionRate();
        BigDecimal commissionAmount = grossAmount.multiply(commissionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal netSettlement = grossAmount.subtract(commissionAmount);

        String txnRef = "COD-REC-" + order.getOrderNumber();
        String partnerName = partner != null ? partner.getFullName() : "Assigned Rider";

        // 1. Credit Admin Demo Wallet Ledger for COD collected cash
        AdminWalletLedger adminLedger = new AdminWalletLedger(
                txnRef, order, order.getCustomer(), order.getPharmacy(),
                "COD", grossAmount, commissionRate, commissionAmount, netSettlement,
                "COD_COLLECTED", "SETTLED", "CASH ON DELIVERY",
                "Cash collected upon delivery by " + partnerName
        );
        adminWalletLedgerRepository.save(adminLedger);

        // 2. Credit Pharmacy Settlement Ledger
        if (order.getPharmacy() != null) {
            PharmacySettlementLedger pharmacyLedger = new PharmacySettlementLedger(
                    order.getPharmacy(), order, txnRef, "COD",
                    grossAmount, commissionRate, commissionAmount, netSettlement, "SETTLED"
            );
            pharmacySettlementLedgerRepository.save(pharmacyLedger);
        }

        // Record or update PaymentTransaction
        PaymentTransaction txn = paymentTransactionRepository.findFirstByOrderOrderByCreatedAtDesc(order).orElse(null);
        if (txn == null) {
            txn = new PaymentTransaction(order, "COD", txnRef, grossAmount, "SUCCESS");
        }
        txn.setStatus("SUCCESS");
        txn.setPaymentMethod("COD");
        txn.setSettlementStatus("SETTLED");
        txn.setRazorpayPaymentId(txnRef);
        paymentTransactionRepository.save(txn);

        // 3. Create PaymentTransfer for COD settlement
        if (order.getPharmacy() != null) {
            PharmacyPayoutAccount payoutAccount = pharmacyPayoutAccountRepository.findByPharmacy(order.getPharmacy()).orElse(null);
            String transferId = "trf_cod_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            PaymentTransfer transfer = new PaymentTransfer(
                    transferId, order, order.getPharmacy(), txn, payoutAccount,
                    grossAmount, commissionAmount, netSettlement, "PROCESSED", idempotencyKey,
                    "COD collection settled by delivery partner " + partnerName
            );
            paymentTransferRepository.save(transfer);
        }
    }

    @Override
    public void recordFailedOrCancelledTransaction(Order order, String method, String status, String maskedIdentifier, String reason) {
        if (order == null) return;

        String txnRef = "TXN-ATTEMPT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        AdminWalletLedger failedLedger = new AdminWalletLedger(
                txnRef, order, order.getCustomer(), order.getPharmacy(),
                method != null ? method.toUpperCase() : "ONLINE",
                order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                status != null ? status.toUpperCase() : "TEST_FAILED",
                "NONE", maskedIdentifier, reason
        );
        adminWalletLedgerRepository.save(failedLedger);
    }

    @Override
    public void reverseSettlementForOrder(Order order, String reason) {
        if (order == null) return;

        List<AdminWalletLedger> adminLedgers = adminWalletLedgerRepository.findByOrderOrderByCreatedAtDesc(order);
        for (AdminWalletLedger al : adminLedgers) {
            if ("DEMO_SETTLED".equalsIgnoreCase(al.getSettlementStatus())) {
                al.setSettlementStatus("REVERSED");
                al.setNotes("REVERSED: " + reason);
                adminWalletLedgerRepository.save(al);
            }
        }

        List<PharmacySettlementLedger> pharmacyLedgers = pharmacySettlementLedgerRepository.findByOrderOrderByCreatedAtDesc(order);
        for (PharmacySettlementLedger pl : pharmacyLedgers) {
            if ("DEMO_SETTLED".equalsIgnoreCase(pl.getSettlementStatus())) {
                pl.setSettlementStatus("REVERSED");
                pharmacySettlementLedgerRepository.save(pl);
            }
        }

        List<PaymentTransfer> transfers = paymentTransferRepository.findByOrder(order);
        for (PaymentTransfer t : transfers) {
            t.setStatus("REVERSED");
            paymentTransferRepository.save(t);
        }

        PaymentTransaction txn = paymentTransactionRepository.findFirstByOrderOrderByCreatedAtDesc(order).orElse(null);
        if (txn != null) {
            txn.setSettlementStatus("REVERSED");
            paymentTransactionRepository.save(txn);

            // Record PaymentRefund
            String refundId = "ref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            PaymentRefund refund = new PaymentRefund(
                    refundId, txn, order, order.getTotalAmount(), reason, "PROCESSED"
            );
            paymentRefundRepository.save(refund);
        }
    }

    @Override
    public List<AdminPaymentLedgerDTO> getAllAdminLedgerEntries(String status, Long pharmacyId, String date, String search) {
        List<AdminWalletLedger> list = adminWalletLedgerRepository.findAllByOrderByCreatedAtDesc();

        return list.stream()
                .filter(l -> {
                    if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                        if (!status.equalsIgnoreCase(l.getPaymentStatus()) && !status.equalsIgnoreCase(l.getSettlementStatus())) {
                            return false;
                        }
                    }
                    if (pharmacyId != null && (l.getPharmacy() == null || !l.getPharmacy().getId().equals(pharmacyId))) {
                        return false;
                    }
                    if (date != null && !date.trim().isEmpty()) {
                        String createdStr = l.getCreatedAt().toString();
                        if (!createdStr.startsWith(date)) {
                            return false;
                        }
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.toLowerCase();
                        boolean matchRef = l.getTransactionReference() != null && l.getTransactionReference().toLowerCase().contains(s);
                        boolean matchOrder = l.getOrder() != null && l.getOrder().getOrderNumber() != null && l.getOrder().getOrderNumber().toLowerCase().contains(s);
                        boolean matchCust = l.getCustomer() != null && l.getCustomer().getFullName() != null && l.getCustomer().getFullName().toLowerCase().contains(s);
                        boolean matchPharm = l.getPharmacy() != null && l.getPharmacy().getName() != null && l.getPharmacy().getName().toLowerCase().contains(s);
                        if (!matchRef && !matchOrder && !matchCust && !matchPharm) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::mapToAdminLedgerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getAdminPaymentSummary() {
        List<AdminWalletLedger> all = adminWalletLedgerRepository.findAllByOrderByCreatedAtDesc();

        BigDecimal totalCustomerPayments = BigDecimal.ZERO;
        BigDecimal totalPlatformRevenue = BigDecimal.ZERO;
        BigDecimal totalPharmacyEarnings = BigDecimal.ZERO;
        BigDecimal pendingTransfers = BigDecimal.ZERO;
        BigDecimal completedTransfers = BigDecimal.ZERO;
        long failedCount = 0;
        BigDecimal failedAmount = BigDecimal.ZERO;
        long refundCount = 0;
        BigDecimal refundAmount = BigDecimal.ZERO;

        for (AdminWalletLedger l : all) {
            if ("DEMO_SETTLED".equalsIgnoreCase(l.getSettlementStatus())) {
                if (l.getGrossAmount() != null) totalCustomerPayments = totalCustomerPayments.add(l.getGrossAmount());
                if (l.getCommissionAmount() != null) totalPlatformRevenue = totalPlatformRevenue.add(l.getCommissionAmount());
                if (l.getNetSettlementAmount() != null) totalPharmacyEarnings = totalPharmacyEarnings.add(l.getNetSettlementAmount());
                completedTransfers = completedTransfers.add(l.getNetSettlementAmount() != null ? l.getNetSettlementAmount() : BigDecimal.ZERO);
            } else if ("REVERSED".equalsIgnoreCase(l.getSettlementStatus())) {
                refundCount++;
                if (l.getGrossAmount() != null) refundAmount = refundAmount.add(l.getGrossAmount());
            } else if ("TEST_FAILED".equalsIgnoreCase(l.getPaymentStatus())) {
                failedCount++;
                if (l.getGrossAmount() != null) failedAmount = failedAmount.add(l.getGrossAmount());
            }
        }

        // Sum pending transfers from payment transfers
        List<PaymentTransfer> transfers = paymentTransferRepository.findAll();
        for (PaymentTransfer t : transfers) {
            if ("SETTLEMENT_PENDING".equalsIgnoreCase(t.getStatus())) {
                if (t.getOwnerShareAmount() != null) pendingTransfers = pendingTransfers.add(t.getOwnerShareAmount());
            }
        }

        BigDecimal outstandingRemittance = cashRemittanceRepository.sumTotalByStatus("PENDING_REMITTANCE");
        if (outstandingRemittance == null) outstandingRemittance = BigDecimal.ZERO;

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomerPayments", totalCustomerPayments.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalPlatformRevenue", totalPlatformRevenue.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalPharmacyEarnings", totalPharmacyEarnings.setScale(2, RoundingMode.HALF_UP));
        summary.put("pendingTransfers", pendingTransfers.setScale(2, RoundingMode.HALF_UP));
        summary.put("completedTransfers", completedTransfers.setScale(2, RoundingMode.HALF_UP));
        summary.put("failedSettlementsCount", failedCount);
        summary.put("failedSettlementsAmount", failedAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("refundsCount", refundCount);
        summary.put("refundsAmount", refundAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("outstandingCashRemittance", outstandingRemittance.setScale(2, RoundingMode.HALF_UP));

        summary.put("totalDemoRevenue", totalCustomerPayments.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalPharmacySettlements", totalPharmacyEarnings.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalAdminCommission", totalPlatformRevenue.setScale(2, RoundingMode.HALF_UP));
        summary.put("successfulTransactionsCount", all.stream().filter(l -> "DEMO_SETTLED".equalsIgnoreCase(l.getSettlementStatus())).count());
        summary.put("totalTransactionsCount", all.size());
        summary.put("adminUpiId", getAdminUpiId());
        summary.put("commissionPercentage", getAdminCommissionRate());

        return summary;
    }

    @Override
    public Map<String, Object> getPharmacyEarningsSummary(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);
        if (pharmacy == null) {
            return Collections.emptyMap();
        }

        List<PharmacySettlementLedger> settlements = pharmacySettlementLedgerRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy);

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal pendingSettlements = BigDecimal.ZERO;
        BigDecimal totalCommissionDeducted = BigDecimal.ZERO;
        long completedCount = 0;

        for (PharmacySettlementLedger s : settlements) {
            if (s.getSettlementStatus() != null && (s.getSettlementStatus().toUpperCase().contains("SETTLED") || "PROCESSED".equalsIgnoreCase(s.getSettlementStatus()))) {
                if (s.getGrossAmount() != null) totalSales = totalSales.add(s.getGrossAmount());
                if (s.getNetAmount() != null) totalReceived = totalReceived.add(s.getNetAmount());
                if (s.getCommissionDeducted() != null) totalCommissionDeducted = totalCommissionDeducted.add(s.getCommissionDeducted());
                completedCount++;
            }
        }

        // Pending settlements from transfers or pending COD orders
        List<PaymentTransfer> transfers = paymentTransferRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy);
        for (PaymentTransfer t : transfers) {
            if ("SETTLEMENT_PENDING".equalsIgnoreCase(t.getStatus())) {
                if (t.getOwnerShareAmount() != null) pendingSettlements = pendingSettlements.add(t.getOwnerShareAmount());
            }
        }

        // Payout account info
        PharmacyPayoutAccount payoutAccount = pharmacyPayoutAccountRepository.findByPharmacy(pharmacy).orElse(null);
        Map<String, Object> accountInfo = new HashMap<>();
        if (payoutAccount != null) {
            accountInfo.put("gatewayLinkedAccountId", payoutAccount.getGatewayLinkedAccountId());
            accountInfo.put("bankName", payoutAccount.getBankName());
            accountInfo.put("accountNumberMasked", payoutAccount.getAccountNumberMasked());
            accountInfo.put("ifscCode", payoutAccount.getIfscCode());
            accountInfo.put("verificationStatus", payoutAccount.getVerificationStatus());
            accountInfo.put("settlementStatus", payoutAccount.getSettlementStatus());
        } else {
            accountInfo.put("gatewayLinkedAccountId", "acc_default_" + pharmacyId);
            accountInfo.put("bankName", "HDFC Bank");
            accountInfo.put("accountNumberMasked", "•••• 9821");
            accountInfo.put("ifscCode", "HDFC0001234");
            accountInfo.put("verificationStatus", "VERIFIED");
            accountInfo.put("settlementStatus", "ACTIVE");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("pharmacyId", pharmacy.getId());
        res.put("pharmacyName", pharmacy.getName());
        res.put("totalSales", totalSales.setScale(2, RoundingMode.HALF_UP));
        res.put("totalReceived", totalReceived.setScale(2, RoundingMode.HALF_UP));
        res.put("pendingSettlements", pendingSettlements.setScale(2, RoundingMode.HALF_UP));
        res.put("completedSettlementsCount", completedCount);
        res.put("completedSettlementsTotal", totalReceived.setScale(2, RoundingMode.HALF_UP));
        res.put("platformCommissionPaid", totalCommissionDeducted.setScale(2, RoundingMode.HALF_UP));
        res.put("availablePayoutBalance", totalReceived.setScale(2, RoundingMode.HALF_UP));

        // Legacy compatibility keys
        res.put("totalDemoRevenue", totalSales.setScale(2, RoundingMode.HALF_UP));
        res.put("totalSettlementsReceived", totalReceived.setScale(2, RoundingMode.HALF_UP));
        res.put("totalAdminCommissionDeducted", totalCommissionDeducted.setScale(2, RoundingMode.HALF_UP));
        res.put("payoutAccount", accountInfo);
        res.put("settlements", settlements.stream().map(this::mapToPharmacySettlementDTO).collect(Collectors.toList()));

        return res;
    }

    @Override
    public List<PharmacySettlementDTO> getPharmacySettlements(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);
        if (pharmacy == null) return Collections.emptyList();
        return pharmacySettlementLedgerRepository.findByPharmacyOrderByCreatedAtDesc(pharmacy).stream()
                .map(this::mapToPharmacySettlementDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPharmacyPayoutAccount(Long pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId).orElse(null);
        if (pharmacy == null) return Collections.emptyMap();

        PharmacyPayoutAccount account = pharmacyPayoutAccountRepository.findByPharmacy(pharmacy).orElse(null);
        Map<String, Object> res = new HashMap<>();
        if (account != null) {
            res.put("gatewayLinkedAccountId", account.getGatewayLinkedAccountId());
            res.put("bankName", account.getBankName());
            res.put("accountNumberMasked", account.getAccountNumberMasked());
            res.put("ifscCode", account.getIfscCode());
            res.put("verificationStatus", account.getVerificationStatus());
            res.put("settlementStatus", account.getSettlementStatus());
        }
        return res;
    }

    @Override
    public void markTransferProcessed(Long transferId) {
        PaymentTransfer transfer = paymentTransferRepository.findById(transferId).orElse(null);
        if (transfer != null) {
            transfer.setStatus("PROCESSED");
            transfer.setSettledAt(LocalDateTime.now());
            paymentTransferRepository.save(transfer);
        }
    }

    @Override
    public Map<String, Object> autoSettleAllPendingOrders() {
        List<Order> orders = orderRepository.findAll();
        int settledCount = 0;
        BigDecimal totalDisbursed = BigDecimal.ZERO;

        for (Order o : orders) {
            boolean isPaid = "PAID".equalsIgnoreCase(o.getPaymentStatus());
            boolean isCodCollected = "COD_COLLECTED".equalsIgnoreCase(o.getPaymentStatus()) || "DELIVERED".equalsIgnoreCase(o.getStatus());
            if (!isPaid && !isCodCollected) {
                continue;
            }

            // Check if already settled in pharmacy ledger
            if (o.getPharmacy() != null) {
                boolean alreadySettled = pharmacySettlementLedgerRepository.findByPharmacyOrderByCreatedAtDesc(o.getPharmacy())
                        .stream().anyMatch(l -> l.getOrder() != null && l.getOrder().getId().equals(o.getId()));
                if (alreadySettled) {
                    continue;
                }
            }

            if (isPaid) {
                processPrepaidSettlement(o, null);
                settledCount++;
                if (o.getTotalAmount() != null) {
                    BigDecimal comm = o.getTotalAmount().multiply(getAdminCommissionRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    totalDisbursed = totalDisbursed.add(o.getTotalAmount().subtract(comm));
                }
            } else if (isCodCollected) {
                processCodCollection(o, o.getDeliveryPartner());
                settledCount++;
                if (o.getTotalAmount() != null) {
                    BigDecimal comm = o.getTotalAmount().multiply(getAdminCommissionRate()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    totalDisbursed = totalDisbursed.add(o.getTotalAmount().subtract(comm));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("settledOrdersCount", settledCount);
        result.put("totalDisbursedToOwners", totalDisbursed.setScale(2, RoundingMode.HALF_UP));
        result.put("message", "Automatic money transfers executed successfully. Visible in Admin & Pharmacy Owner histories.");
        return result;
    }

    private AdminPaymentLedgerDTO mapToAdminLedgerDTO(AdminWalletLedger l) {
        AdminPaymentLedgerDTO dto = new AdminPaymentLedgerDTO();
        dto.setId(l.getId());
        dto.setTransactionReference(l.getTransactionReference());
        if (l.getOrder() != null) {
            dto.setOrderId(l.getOrder().getId());
            dto.setOrderNumber(l.getOrder().getOrderNumber());
        }
        if (l.getCustomer() != null) {
            dto.setCustomerId(l.getCustomer().getId());
            dto.setCustomerName(l.getCustomer().getFullName());
        }
        if (l.getPharmacy() != null) {
            dto.setPharmacyId(l.getPharmacy().getId());
            dto.setPharmacyName(l.getPharmacy().getName());
        }
        dto.setPaymentMethod(l.getPaymentMethod());
        dto.setGrossAmount(l.getGrossAmount());
        dto.setCommissionRate(l.getCommissionRate());
        dto.setCommissionAmount(l.getCommissionAmount());
        dto.setNetSettlementAmount(l.getNetSettlementAmount());
        dto.setPaymentStatus(l.getPaymentStatus());
        dto.setSettlementStatus(l.getSettlementStatus());
        dto.setMaskedIdentifier(l.getMaskedIdentifier());
        dto.setNotes(l.getNotes());
        dto.setCreatedAt(l.getCreatedAt());
        dto.setSettledAt(l.getSettledAt());
        return dto;
    }

    private PharmacySettlementDTO mapToPharmacySettlementDTO(PharmacySettlementLedger s) {
        PharmacySettlementDTO dto = new PharmacySettlementDTO();
        dto.setId(s.getId());
        if (s.getOrder() != null) {
            dto.setOrderId(s.getOrder().getId());
            dto.setOrderNumber(s.getOrder().getOrderNumber());
            if (s.getOrder().getCustomer() != null) {
                dto.setCustomerName(s.getOrder().getCustomer().getFullName());
            } else {
                dto.setCustomerName(s.getOrder().getCustomerName());
            }
        }
        dto.setPaymentMethod(s.getPaymentMethod());
        dto.setGrossAmount(s.getGrossAmount());
        dto.setCommissionRate(s.getCommissionRate());
        dto.setCommissionDeducted(s.getCommissionDeducted());
        dto.setNetAmount(s.getNetAmount());
        dto.setSettlementStatus(s.getSettlementStatus());
        dto.setCreatedAt(s.getCreatedAt());
        return dto;
    }
}
