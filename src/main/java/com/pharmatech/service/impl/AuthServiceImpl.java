package com.pharmatech.service.impl;

import com.pharmatech.dto.AuthResponse;
import com.pharmatech.dto.CustomerRegisterRequest;
import com.pharmatech.dto.LoginRequest;
import com.pharmatech.dto.PharmacyOwnerRegisterRequest;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.exception.UnauthorizedException;
import com.pharmatech.exception.UserAlreadyExistsException;
import com.pharmatech.model.*;
import com.pharmatech.repository.*;
import com.pharmatech.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Service implementation demonstrating ABSTRACTION and POLYMORPHISM (@Override).
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PharmacyOwnerRepository pharmacyOwnerRepository;
    private final PharmacyRepository pharmacyRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PharmacyOwnerRepository pharmacyOwnerRepository,
                           PharmacyRepository pharmacyRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.pharmacyOwnerRepository = pharmacyOwnerRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Override
    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new UserAlreadyExistsException("A user with email " + request.getEmail() + " already exists");
        }

        Customer customer = new Customer();
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(request.getEmail().trim().toLowerCase());
        customer.setPhoneNumber(request.getPhoneNumber().trim());
        customer.setPassword(hashPassword(request.getPassword()));
        customer.setRole(Role.ROLE_CUSTOMER);
        customer.setDeliveryAddress(request.getDeliveryAddress());
        customer.setCity(request.getCity());
        customer.setPincode(request.getPincode());

        Customer savedCustomer = customerRepository.save(customer);
        String token = generateToken(savedCustomer.getEmail());

        return new AuthResponse(
                savedCustomer.getId(),
                savedCustomer.getFullName(),
                savedCustomer.getEmail(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getRole(),
                null,
                null,
                false,
                token,
                "Customer registered successfully"
        );
    }

    @Override
    public AuthResponse registerPharmacyOwner(PharmacyOwnerRegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new UserAlreadyExistsException("A user with email " + request.getEmail() + " already exists");
        }

        // Create PharmacyOwner entity (demonstrates Inheritance from User)
        PharmacyOwner owner = new PharmacyOwner();
        owner.setFullName(request.getOwnerName().trim());
        owner.setEmail(request.getEmail().trim().toLowerCase());
        owner.setPhoneNumber(request.getPhoneNumber().trim());
        owner.setPassword(hashPassword(request.getPassword()));
        owner.setLicenseNumber(request.getLicenseNumber().trim());
        owner.setRole(Role.ROLE_PHARMACY_OWNER);
        owner.setSubscriptionActive(false);

        PharmacyOwner savedOwner = pharmacyOwnerRepository.save(owner);

        // Create associated Pharmacy store
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName(request.getPharmacyName().trim());
        pharmacy.setLicenseNumber(request.getLicenseNumber().trim());
        pharmacy.setAddress(request.getPharmacyAddress().trim());
        pharmacy.setCity(request.getCity().trim());
        pharmacy.setPincode(request.getPincode().trim());
        pharmacy.setPhoneNumber(request.getPhoneNumber().trim());
        pharmacy.setOwner(savedOwner);
        pharmacy.setRating(4.9);
        pharmacy.setVerified(true);

        Pharmacy savedPharmacy = pharmacyRepository.save(pharmacy);
        savedOwner.setPharmacy(savedPharmacy);

        String token = generateToken(savedOwner.getEmail());

        return new AuthResponse(
                savedOwner.getId(),
                savedOwner.getFullName(),
                savedOwner.getEmail(),
                savedOwner.getPhoneNumber(),
                savedOwner.getRole(),
                savedPharmacy.getId(),
                savedPharmacy.getName(),
                savedOwner.isSubscriptionActive(),
                token,
                "Pharmacy owner registered successfully. Please activate your subscription."
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();

        // Convenience aliases for Shree Lakshmi Medicals (SITRA, Coimbatore)
        if ("shreelakshmi".equals(identifier) || "shree lakshmi".equals(identifier) ||
            "shreelakshmimedicals".equals(identifier) || "shreelakshmi@pharmatech.com".equals(identifier) ||
            "owner@shreelakshmimedicals.com".equals(identifier) || "04222571234".equals(identifier) ||
            "0422-2571234".equals(identifier)) {
            identifier = "owner@shreelakshmi.com";
        }

        String finalIdentifier = identifier;
        User user = userRepository.findByEmail(finalIdentifier)
                .or(() -> userRepository.findByEmailOrPhoneNumber(finalIdentifier, finalIdentifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid email/phone number or password"));

        if (!validatePassword(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/phone number or password");
        }

        Long pharmacyId = null;
        String pharmacyName = null;
        boolean subscriptionActive = false;

        if (user.getRole() == Role.ROLE_PHARMACY_OWNER && user instanceof PharmacyOwner) {
            PharmacyOwner owner = (PharmacyOwner) user;
            subscriptionActive = owner.isSubscriptionActive();
            if (owner.getPharmacy() != null) {
                pharmacyId = owner.getPharmacy().getId();
                pharmacyName = owner.getPharmacy().getName();
            } else {
                Pharmacy pharmacy = pharmacyRepository.findByOwner(owner).orElse(null);
                if (pharmacy != null) {
                    pharmacyId = pharmacy.getId();
                    pharmacyName = pharmacy.getName();
                }
            }
        }

        String token = generateToken(user.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                pharmacyId,
                pharmacyName,
                subscriptionActive,
                token,
                "Login successful"
        );
    }

    @Override
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return hashPassword(rawPassword).equals(encodedPassword);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return password; // Fallback
        }
    }

    private String generateToken(String email) {
        return UUID.randomUUID().toString() + "-" + Base64.getUrlEncoder().encodeToString(email.getBytes());
    }
}
