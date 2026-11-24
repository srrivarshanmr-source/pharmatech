package com.pharmatech.service;

import com.pharmatech.dto.AuthResponse;
import com.pharmatech.dto.CustomerRegisterRequest;
import com.pharmatech.dto.LoginRequest;
import com.pharmatech.dto.PharmacyOwnerRegisterRequest;
import com.pharmatech.model.User;

/**
 * Service interface demonstrating ABSTRACTION.
 * Decouples controller layer from concrete business logic implementations.
 */
public interface AuthService {

    AuthResponse registerCustomer(CustomerRegisterRequest request);

    AuthResponse registerPharmacyOwner(PharmacyOwnerRegisterRequest request);

    AuthResponse login(LoginRequest request);

    User getCurrentUser(String email);

    boolean validatePassword(String rawPassword, String encodedPassword);
}
