package com.pharmatech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PharmaTech - Medicine Availability and Pharmacy Management Platform
 * Initial 25% Working Milestone
 */
@SpringBootApplication
public class PharmaTechApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaTechApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  PharmaTech Platform is running at: http://localhost:8080        ");
        System.out.println("  - Customer Portal:        http://localhost:8080/index.html      ");
        System.out.println("  - Customer Login:         http://localhost:8080/customer-login.html ");
        System.out.println("  - Pharmacy Portal:        http://localhost:8080/pharmacy-login.html ");
        System.out.println("  - Pharmacy Subscription:  http://localhost:8080/pharmacy-subscription.html ");
        System.out.println("  - Pharmacy Dashboard:     http://localhost:8080/pharmacy-dashboard.html ");
        System.out.println("  - H2 Database Console:    http://localhost:8080/h2-console      ");
        System.out.println("==================================================================");
    }
}
