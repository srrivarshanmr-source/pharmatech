package com.pharmatech.controller;

import com.pharmatech.dto.ApiResponse;
import com.pharmatech.dto.AuthResponse;
import com.pharmatech.dto.CustomerRegisterRequest;
import com.pharmatech.dto.LoginRequest;
import com.pharmatech.dto.PharmacyOwnerRegisterRequest;
import com.pharmatech.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling Customer and Pharmacy Owner authentication and registration.
 * Clean separation between interfaces sharing the same backend.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Customer Registration
     */
    @PostMapping("/customer/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        AuthResponse response = authService.registerCustomer(request);
        return new ResponseEntity<>(ApiResponse.ok("Customer registered successfully", response), HttpStatus.CREATED);
    }

    /**
     * Customer Login
     */
    @PostMapping("/customer/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginCustomer(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * Pharmacy Owner Registration
     */
    @PostMapping("/pharmacy/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerPharmacyOwner(@Valid @RequestBody PharmacyOwnerRegisterRequest request) {
        AuthResponse response = authService.registerPharmacyOwner(request);
        return new ResponseEntity<>(ApiResponse.ok("Pharmacy registered successfully. Please proceed to activate your subscription.", response), HttpStatus.CREATED);
    }

    /**
     * Pharmacy Owner Login
     */
    @PostMapping("/pharmacy/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginPharmacyOwner(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * Admin Login
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (response.getRole() != com.pharmatech.model.Role.ROLE_ADMIN) {
            throw new com.pharmatech.exception.UnauthorizedException("Access Denied: This account does not have Administrator privileges");
        }
        return ResponseEntity.ok(ApiResponse.ok("Administrator authenticated successfully", response));
    }

    /**
     * Unified Login Endpoint (handles all 4 roles)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> unifiedLogin(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Authentication successful", response));
    }
}
