# PharmaTech – Medicine Availability and Pharmacy Management Platform
### College Project Milestone: Initial 25% Working Version

PharmaTech is a modern, startup-grade medicine availability and pharmacy platform built using **Java**, **Spring Boot**, **Spring Data JPA**, **MySQL**, and a responsive frontend (**HTML, CSS, JavaScript**).

---

## 1. Key Project Highlights

- **Blinkit / Instamart-Inspired Healthcare UI**: Emerald Green (`#059669`), soft mint, clean card layouts, instant medicine search, category filtering, and shopping cart slide-over.
- **Two Separate Interfaces**:
  1. **Customer Interface**: Explore medicines, check live pharmacy stock, filter categories, view nearby verified pharmacies, customer registration and login.
  2. **Pharmacy Owner Interface**: Accessed via the clearly visible **"Partner with Us"** button, includes dedicated registration, login, ₹200/month partner plan subscription activation, real-time dashboard metrics, and full medicine inventory management (CRUD).
- **Proper Layered Spring Boot Architecture**: Controller ➔ Service Interface ➔ Service Implementation ➔ Repository ➔ Entity Model.
- **Strict OOP Implementation for College Viva**: Encapsulation, Joined Inheritance (`User` ➔ `Customer`, `PharmacyOwner`), Compile-time Polymorphism (overloaded searches), Runtime Polymorphism (method overriding), and Abstraction. Detailed documentation in `JAVA_CONCEPT_MAPPING.md`.

---

## 2. Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Data JPA, Hibernate, Maven, REST APIs
- **Database**: MySQL (Production profile `application-mysql.properties`) with zero-config H2 MySQL-compatibility mode (`application-dev.properties`) for instant running out-of-the-box
- **Frontend**: HTML5, Modern CSS3 (CSS Variables, Flexbox, Grid), Vanilla JavaScript ES6+ (Fetch API, LocalStorage session handling)

---

## 3. How to Run the Project Locally

### Option A: Quick One-Click Launch (Windows)
Double-click `run.bat` in the project root directory. It automatically configures the local Java 17 and Maven paths and launches the server.

### Option B: Terminal Command
Run the following in PowerShell / Command Prompt:
```powershell
$env:JAVA_HOME = "$PWD\.tools\jdk17.0.21_0"
$env:Path = "$env:JAVA_HOME\bin;$PWD\.tools\apache-maven-3.9.6\bin;$env:Path"
mvn spring-boot:run
```

Once started, open your web browser at:
- **Customer Homepage**: [http://localhost:8080/index.html](http://localhost:8080/index.html)
- **Customer Login / Register**: [http://localhost:8080/customer-login.html](http://localhost:8080/customer-login.html)
- **Pharmacy Partner Portal**: [http://localhost:8080/pharmacy-login.html](http://localhost:8080/pharmacy-login.html)
- **Pharmacy Subscription Plan**: [http://localhost:8080/pharmacy-subscription.html](http://localhost:8080/pharmacy-subscription.html)
- **Pharmacy Owner Dashboard**: [http://localhost:8080/pharmacy-dashboard.html](http://localhost:8080/pharmacy-dashboard.html)
- **Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:pharmatech_db`, User: `sa`, Password: empty)

---

## 4. Connecting to Live MySQL Server

PharmaTech is built 100% for MySQL. When you want to connect to your local MySQL installation:
1. Ensure your local MySQL server is running on port `3306`.
2. Open MySQL Workbench or MySQL CLI and run `src/main/resources/schema.sql` (or let Hibernate auto-create tables).
3. In `src/main/resources/application.properties`, activate the `mysql` profile:
   ```properties
   spring.profiles.active=mysql
   ```
4. Adjust username/password in `src/main/resources/application-mysql.properties` if your MySQL root password is not `root`.

---

## 5. Seed Demonstration Accounts (College Viva)

The system automatically initializes realistic demonstration data on boot:

### Customer Account:
- **Email**: `customer@pharmatech.com`
- **Password**: `password123`
- **Name**: Rahul Sharma
- **City**: Bangalore

### Pharmacy Owner Account (Active Partner):
- **Email**: `owner@apollo.com`
- **Password**: `password123`
- **Store Name**: Apollo Pharmacy - Metro Express
- **License**: `DL-KA-2024-984321`
- **Subscription**: Active (₹200/month)

### Pharmacy Owner Account (MedPlus):
- **Email**: `owner@medplus.com`
- **Password**: `password123`
- **Store Name**: MedPlus Wellness - Koramangala

*(You can also register brand-new customer and pharmacy owner accounts from the web forms!)*

---

## 6. Project Directory Structure

```
c:\Users\kanna\OneDrive\Desktop\pharmatech
├── pom.xml
├── run.bat
├── README.md
├── JAVA_CONCEPT_MAPPING.md
├── src/
│   ├── main/
│   │   ├── java/com/pharmatech/
│   │   │   ├── PharmaTechApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CustomerController.java
│   │   │   │   ├── PharmacyOwnerController.java
│   │   │   │   └── MedicineController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java (Superclass - Inheritance)
│   │   │   │   ├── Customer.java (Subclass)
│   │   │   │   ├── PharmacyOwner.java (Subclass)
│   │   │   │   ├── Pharmacy.java
│   │   │   │   ├── Medicine.java
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── Subscription.java
│   │   │   │   └── Role.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── CustomerRegisterRequest.java
│   │   │   │   ├── PharmacyOwnerRegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── MedicineRequest.java
│   │   │   │   ├── InventoryItemDTO.java
│   │   │   │   ├── DashboardStatsDTO.java
│   │   │   │   └── SubscriptionRequest.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── CustomerRepository.java
│   │   │   │   ├── PharmacyOwnerRepository.java
│   │   │   │   ├── PharmacyRepository.java
│   │   │   │   ├── MedicineRepository.java
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   └── SubscriptionRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── MedicineService.java
│   │   │   │   ├── InventoryService.java
│   │   │   │   ├── PharmacyOwnerService.java
│   │   │   │   ├── CustomerService.java
│   │   │   │   └── SubscriptionService.java
│   │   │   ├── service/impl/
│   │   │   │   ├── AuthServiceImpl.java
│   │   │   │   ├── MedicineServiceImpl.java
│   │   │   │   ├── InventoryServiceImpl.java
│   │   │   │   ├── PharmacyOwnerServiceImpl.java
│   │   │   │   ├── CustomerServiceImpl.java
│   │   │   │   └── SubscriptionServiceImpl.java
│   │   │   ├── config/
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── DataInitializer.java
│   │   │   └── exception/
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── UserAlreadyExistsException.java
│   │   │       ├── UnauthorizedException.java
│   │   │       ├── BadRequestException.java
│   │   │       ├── ErrorDetails.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-mysql.properties
│   │       ├── schema.sql
│   │       └── static/
│   │           ├── index.html (Customer Homepage)
│   │           ├── customer-login.html (Customer Auth)
│   │           ├── pharmacy-login.html (Pharmacy Auth)
│   │           ├── pharmacy-subscription.html (Partner Plan)
│   │           ├── pharmacy-dashboard.html (Pharmacy Manager)
│   │           ├── css/
│   │           │   ├── style.css (Design System)
│   │           │   └── dashboard.css (Portal Styles)
│   │           └── js/
│   │               ├── api.js (REST Client)
│   │               ├── customer.js (Search & Cart)
│   │               ├── auth.js (Auth Forms)
│   │               ├── subscription.js (Plan Activation)
│   │               └── dashboard.js (Inventory CRUD)
```

---

## 7. Future Milestones (Milestones 2 & 3)

The following advanced features are intentionally reserved for subsequent milestones and clearly labeled as **"Coming Soon"** in the pharmacy owner dashboard:
- Invoice upload with OCR extraction
- Automatic medicine stock updates from invoices
- Billing system
- Real payment gateway integration (Razorpay / Stripe)
- Multi-pharmacy order fulfillment and delivery tracking
- SMS / WhatsApp notifications
