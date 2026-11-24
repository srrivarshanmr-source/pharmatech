-- ===================================================================
-- PharmaTech Platform - MySQL Schema Definition
-- College Project Milestone: Initial 25% Architecture
-- Database Name: pharmatech_db
-- ===================================================================

CREATE DATABASE IF NOT EXISTS pharmatech_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pharmatech_db;

-- 1. Base Users Table (Inheritance Superclass: Joined Table Strategy)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. Customers Table (Inherits from users)
CREATE TABLE IF NOT EXISTS customers (
    user_id BIGINT PRIMARY KEY,
    delivery_address VARCHAR(255),
    city VARCHAR(100),
    pincode VARCHAR(10),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_customer_city (city)
) ENGINE=InnoDB;

-- 3. Pharmacy Owners Table (Inherits from users)
CREATE TABLE IF NOT EXISTS pharmacy_owners (
    user_id BIGINT PRIMARY KEY,
    license_number VARCHAR(100),
    subscription_active BOOLEAN DEFAULT FALSE,
    subscription_expires_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_owner_license (license_number)
) ENGINE=InnoDB;

-- 4. Pharmacies Table
CREATE TABLE IF NOT EXISTS pharmacies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    license_number VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    phone_number VARCHAR(20),
    rating DECIMAL(2,1) DEFAULT 4.8,
    is_verified BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    owner_id BIGINT UNIQUE,
    FOREIGN KEY (owner_id) REFERENCES pharmacy_owners(user_id) ON DELETE SET NULL,
    INDEX idx_pharmacy_city (city),
    INDEX idx_pharmacy_pin (pincode)
) ENGINE=InnoDB;

-- 5. Master Medicine Catalog Table
CREATE TABLE IF NOT EXISTS medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    strength VARCHAR(50) NOT NULL,
    category VARCHAR(80) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    prescription_required BOOLEAN DEFAULT FALSE,
    image_url VARCHAR(500),
    INDEX idx_med_name (name),
    INDEX idx_med_brand (brand),
    INDEX idx_med_category (category)
) ENGINE=InnoDB;

-- 6. Pharmacy Inventory Table (Stocks per store)
CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pharmacy_id BIGINT NOT NULL,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price DECIMAL(10,2) NOT NULL,
    expiry_date DATE NOT NULL,
    batch_number VARCHAR(50),
    is_available BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_pharmacy_medicine (pharmacy_id, medicine_id),
    FOREIGN KEY (pharmacy_id) REFERENCES pharmacies(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE CASCADE,
    INDEX idx_inv_pharmacy (pharmacy_id),
    INDEX idx_inv_medicine (medicine_id)
) ENGINE=InnoDB;

-- 7. Subscriptions Table (Partner membership tracking)
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    monthly_price DECIMAL(10,2) NOT NULL DEFAULT 200.00,
    status VARCHAR(30) NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date TIMESTAMP NOT NULL,
    transaction_reference VARCHAR(100),
    FOREIGN KEY (owner_id) REFERENCES pharmacy_owners(user_id) ON DELETE CASCADE,
    INDEX idx_sub_owner (owner_id),
    INDEX idx_sub_status (status)
) ENGINE=InnoDB;
