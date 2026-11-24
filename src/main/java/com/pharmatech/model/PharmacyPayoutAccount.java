package com.pharmatech.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Verified bank / gateway linked payout account for pharmacy owners (Razorpay Route pattern).
 */
@Entity
@Table(name = "pharmacy_payout_accounts")
public class PharmacyPayoutAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacy_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"owner", "inventories", "hibernateLazyInitializer", "handler"})
    private Pharmacy pharmacy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"pharmacies", "user", "hibernateLazyInitializer", "handler"})
    private PharmacyOwner owner;

    @Column(name = "gateway_linked_account_id", nullable = false, length = 100)
    private String gatewayLinkedAccountId; // e.g. acc_apollo_route_01

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "account_number_masked", nullable = false, length = 50)
    private String accountNumberMasked; // e.g. •••• 9821

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "verification_status", nullable = false, length = 30)
    private String verificationStatus = "VERIFIED"; // PENDING, VERIFIED, REJECTED

    @Column(name = "settlement_status", nullable = false, length = 30)
    private String settlementStatus = "ACTIVE"; // ACTIVE, SUSPENDED, BLOCKED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PharmacyPayoutAccount() {}

    public PharmacyPayoutAccount(Pharmacy pharmacy, PharmacyOwner owner, String gatewayLinkedAccountId,
                                 String bankName, String accountNumberMasked, String ifscCode,
                                 String verificationStatus, String settlementStatus) {
        this.pharmacy = pharmacy;
        this.owner = owner;
        this.gatewayLinkedAccountId = gatewayLinkedAccountId;
        this.bankName = bankName;
        this.accountNumberMasked = accountNumberMasked;
        this.ifscCode = ifscCode;
        this.verificationStatus = verificationStatus;
        this.settlementStatus = settlementStatus;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public PharmacyOwner getOwner() {
        return owner;
    }

    public void setOwner(PharmacyOwner owner) {
        this.owner = owner;
    }

    public String getGatewayLinkedAccountId() {
        return gatewayLinkedAccountId;
    }

    public void setGatewayLinkedAccountId(String gatewayLinkedAccountId) {
        this.gatewayLinkedAccountId = gatewayLinkedAccountId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumberMasked() {
        return accountNumberMasked;
    }

    public void setAccountNumberMasked(String accountNumberMasked) {
        this.accountNumberMasked = accountNumberMasked;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getSettlementStatus() {
        return settlementStatus;
    }

    public void setSettlementStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
