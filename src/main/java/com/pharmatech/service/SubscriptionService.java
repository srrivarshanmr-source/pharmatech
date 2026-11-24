package com.pharmatech.service;

import com.pharmatech.dto.SubscriptionRequest;
import com.pharmatech.model.Subscription;
import java.util.List;

/**
 * Service interface for partner subscription management.
 */
public interface SubscriptionService {

    Subscription activateSubscription(SubscriptionRequest request);

    boolean isSubscriptionActive(Long ownerId);

    List<Subscription> getSubscriptionsByOwner(Long ownerId);
}
