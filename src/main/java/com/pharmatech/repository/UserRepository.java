package com.pharmatech.repository;

import com.pharmatech.model.Role;
import com.pharmatech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
}
