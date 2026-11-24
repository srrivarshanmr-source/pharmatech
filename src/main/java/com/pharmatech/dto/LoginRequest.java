package com.pharmatech.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email or mobile number is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setEmail(String email) {
        if (this.identifier == null || this.identifier.trim().isEmpty()) {
            this.identifier = email;
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        if (this.identifier == null || this.identifier.trim().isEmpty()) {
            this.identifier = phoneNumber;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
