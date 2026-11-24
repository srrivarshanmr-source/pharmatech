package com.pharmatech.service.impl;

import com.pharmatech.dto.SubscriptionRequest;
import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.model.PharmacyOwner;
import com.pharmatech.model.Subscription;
import com.pharmatech.repository.PharmacyOwnerRepository;
import com.pharmatech.repository.SubscriptionRepository;
import com.pharmatech.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Pharmacy Owner Partner Subscription.
 */
@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PharmacyOwnerRepository pharmacyOwnerRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
                                   PharmacyOwnerRepository pharmacyOwnerRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.pharmacyOwnerRepository = pharmacyOwnerRepository;
    }

    @Override
    public Subscription activateSubscription(SubscriptionRequest request) {
        PharmacyOwner owner = pharmacyOwnerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy Owner", "id", request.getOwnerId()));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(30);

        String txRef = "SIM-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Subscription subscription = new Subscription(
                owner,
                request.getPlanName() != null ? request.getPlanName() : "PharmaTech Pharmacy Partner Plan",
                request.getMonthlyPrice() != null ? request.getMonthlyPrice() : new BigDecimal("200.00"),
                "ACTIVE",
                now,
                expiresAt,
                txRef
        );

        Subscription saved = subscriptionRepository.save(subscription);

        // Update owner subscription state
        owner.setSubscriptionActive(true);
        owner.setSubscriptionExpiresAt(expiresAt);
        pharmacyOwnerRepository.save(owner);

        return saved;
    }

    @Override
    public boolean isSubscriptionActive(Long ownerId) {
        PharmacyOwner owner = pharmacyOwnerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy Owner", "id", ownerId));
        return owner.isSubscriptionActive();
    }

    @Override
    public List<Subscription> getSubscriptionsByOwner(Long ownerId) {
        return subscriptionRepository.findByOwnerId(ownerId);
    }
}
