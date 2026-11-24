package com.pharmatech.repository;

import com.pharmatech.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByOwnerId(Long ownerId);
    Optional<Subscription> findTopByOwnerIdOrderByStartDateDesc(Long ownerId);
    boolean existsByOwnerIdAndStatus(Long ownerId, String status);
}
