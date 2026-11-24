# PharmaTech – Java Object-Oriented Programming (OOP) Concept Mapping
### College Viva & Technical Evaluation Guide

This document maps every core Java and Object-Oriented Programming (OOP) concept directly to the actual source code files, classes, and methods in the **PharmaTech** platform. You can use these explanations word-for-word during your college project presentation and viva examination.

---

## Quick Reference Summary Table

| OOP / Java Concept | Exact File Path | Class / Interface / Method | Viva One-Liner |
| :--- | :--- | :--- | :--- |
| **Encapsulation** | `model/User.java`<br>`model/Inventory.java` | Private attributes + Getters/Setters + `isLowStock()`, `updateStock()` | Bundles data and methods together while hiding internal state with private access. |
| **Inheritance** | `model/User.java`<br>`model/Customer.java`<br>`model/PharmacyOwner.java` | `Customer extends User`<br>`PharmacyOwner extends User` | `Customer` and `PharmacyOwner` inherit common user attributes (name, email, password) from `User`. |
| **Runtime Polymorphism (Method Overriding)** | `model/User.java`<br>`model/Customer.java`<br>`model/PharmacyOwner.java` | `@Override public String getUserSummary()` | Dynamic method dispatch calls subclass implementation at runtime based on object type. |
| **Compile-time Polymorphism (Method Overloading)** | `service/MedicineService.java`<br>`service/impl/MedicineServiceImpl.java` | `searchMedicines(query)`<br>`searchMedicines(query, category)`<br>`searchMedicines(query, category, maxPrice)` | Multiple methods with identical names but different parameter lists resolved at compile time. |
| **Abstraction** | `service/MedicineService.java`<br>`service/AuthService.java`<br>`service/InventoryService.java` | Service Interfaces & Implementations in `service.impl` | Hides implementation complexity behind interface contracts, achieving loose coupling. |
| **Exception Handling** | `exception/GlobalExceptionHandler.java`<br>`exception/ResourceNotFoundException.java` | `@RestControllerAdvice`, `@ExceptionHandler` | Graceful handling and centralized error translation without crashing the application. |
| **Constructor Overloading & Chaining** | `model/User.java`<br>`model/Customer.java` | `super(...)` inside `Customer` constructor | Initializing objects with default vs parameterized state and chaining to superclass. |
| **Collections & Generics** | `dto/ApiResponse.java`<br>`service/impl/InventoryServiceImpl.java` | `ApiResponse<T>`, `List<InventoryItemDTO>`, Streams | Type-safe generic response envelopes and functional Java Streams (`map`, `filter`, `collect`). |
| **Layered Architecture** | `controller`, `service`, `repository`, `model` | Spring Boot Controller-Service-Repository pattern | Separation of concerns: presentation, business rules, and database persistence. |

---

## Detailed Concepts Breakdown for College Viva

### 1. Encapsulation

#### Where it is implemented:
- **File**: `src/main/java/com/pharmatech/model/User.java`
- **File**: `src/main/java/com/pharmatech/model/Inventory.java`
- **File**: `src/main/java/com/pharmatech/model/Medicine.java`

#### Code Snippet:
```java
// In Inventory.java:
@Entity
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Encapsulated Business Logic Methods
    public boolean isLowStock() {
        return this.quantity != null && this.quantity > 0 && this.quantity <= 15;
    }

    public void updateStock(int newQuantity) {
        this.quantity = Math.max(0, newQuantity);
        this.available = (this.quantity > 0);
        this.lastUpdated = LocalDateTime.now();
    }
}
```

#### How it works in PharmaTech:
1. All entity properties (`quantity`, `price`, `password`, `email`) are declared `private` to prevent unauthorized outside tampering.
2. Controlled access is provided through public getters and setters.
3. Business rules (e.g., checking if stock is low or preventing negative quantities) are encapsulated directly inside the model instead of scattered in client code.

#### Viva Explanation:
> *"Sir/Madam, Encapsulation is data hiding. In PharmaTech, all entity variables like medicine price and quantity are marked `private`. External classes cannot change them directly. They must use getters and setters or business methods like `updateStock()`, which validates that quantity cannot drop below zero."*

---

### 2. Inheritance

#### Where it is implemented:
- **Base Class**: `src/main/java/com/pharmatech/model/User.java`
- **Subclasses**: 
  - `src/main/java/com/pharmatech/model/Customer.java` (`public class Customer extends User`)
  - `src/main/java/com/pharmatech/model/PharmacyOwner.java` (`public class PharmacyOwner extends User`)
  - `src/main/java/com/pharmatech/model/DeliveryPartner.java` (`public class DeliveryPartner extends User`)

#### Code Snippet:
```java
// User.java (Superclass)
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private Role role;
    ...
}

// DeliveryPartner.java (Subclass)
@Entity
@Table(name = "delivery_partners")
@PrimaryKeyJoinColumn(name = "user_id")
public class DeliveryPartner extends User {
    private String vehicleNumber;
    private String drivingLicense;
    private String assignedCity;
    private boolean available;
    private Integer totalDeliveries;
    ...
}

// Customer.java (Subclass)
@Entity
@Table(name = "customers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Customer extends User {
    private String deliveryAddress;
    private String city;
    private String pincode;
    ...
}

// PharmacyOwner.java (Subclass)
@Entity
@Table(name = "pharmacy_owners")
@PrimaryKeyJoinColumn(name = "user_id")
public class PharmacyOwner extends User {
    private String licenseNumber;
    private boolean subscriptionActive;
    ...
}
```

#### How it works in PharmaTech:
Both Customers and Pharmacy Owners need common credentials (name, email, phone, password, role). Instead of duplicating these columns in multiple tables, `User` defines the common identity. `Customer` and `PharmacyOwner` extend `User` using JPA's `InheritanceType.JOINED` strategy, creating normalized MySQL tables linked via foreign key (`user_id`).

#### Viva Explanation:
> *"We implemented an `is-a` relationship using the `extends` keyword. A `Customer` is a `User`, and a `PharmacyOwner` is a `User`. Both inherit common credentials from the `User` superclass, avoiding code duplication and reflecting real-world database normalization in MySQL."*

---

### 3. Polymorphism

PharmaTech implements **both forms** of Polymorphism:

#### A. Compile-Time Polymorphism (Method Overloading)
- **File**: `src/main/java/com/pharmatech/service/impl/MedicineServiceImpl.java`
- **File**: `src/main/java/com/pharmatech/service/MedicineService.java`

```java
// 1. Search by name/brand only
List<Medicine> searchMedicines(String query);

// 2. Search by name/brand within a specific category
List<Medicine> searchMedicines(String query, String category);

// 3. Search with query, category, AND maximum price limit
List<Medicine> searchMedicines(String query, String category, BigDecimal maxPrice);
```

**Viva Explanation**:
> *"Method Overloading allows multiple methods in the same class to share the same name with different parameters. For example, our `MedicineService` has three overloaded `searchMedicines` methods: one that searches by keyword, another that also filters by category, and a third that adds a price filter."*

#### B. Runtime Polymorphism (Method Overriding)
- **Superclass**: `User.java`
- **Subclasses**: `Customer.java` and `PharmacyOwner.java`

```java
// In User.java:
public String getUserSummary() {
    return "User: " + fullName + " (" + email + ") | Role: " + role;
}

// In Customer.java:
@Override
public String getUserSummary() {
    return "[Customer] " + getFullName() + " | City: " + city + " | Address: " + deliveryAddress;
}

// In PharmacyOwner.java:
@Override
public String getUserSummary() {
    return "[Pharmacy Owner] " + getFullName() + " | Pharmacy: " + (pharmacy != null ? pharmacy.getName() : "None") + " | License: " + licenseNumber;
}
```

**Viva Explanation**:
> *"Runtime polymorphism is achieved via method overriding (`@Override`). When `getUserSummary()` is invoked on a `User` reference variable pointing to a `Customer` or `PharmacyOwner` object, the JVM decides at runtime which overridden method to execute via dynamic method dispatch."*

---

### 4. Abstraction

#### Where it is implemented:
- **Interfaces**:
  - `src/main/java/com/pharmatech/service/AuthService.java`
  - `src/main/java/com/pharmatech/service/MedicineService.java`
  - `src/main/java/com/pharmatech/service/InventoryService.java`
  - `src/main/java/com/pharmatech/service/PharmacyOwnerService.java`
  - `src/main/java/com/pharmatech/service/CustomerService.java`
  - `src/main/java/com/pharmatech/service/SubscriptionService.java`
- **Implementations**:
  - `src/main/java/com/pharmatech/service/impl/AuthServiceImpl.java`
  - `src/main/java/com/pharmatech/service/impl/MedicineServiceImpl.java`
  - `src/main/java/com/pharmatech/service/impl/InventoryServiceImpl.java`
  - etc.

#### Code Snippet:
```java
// AuthService Interface (Contract / Abstraction)
public interface AuthService {
    AuthResponse registerCustomer(CustomerRegisterRequest request);
    AuthResponse registerPharmacyOwner(PharmacyOwnerRegisterRequest request);
    AuthResponse login(LoginRequest request);
}

// AuthServiceImpl (Concrete Implementation)
@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        // Concrete password hashing, entity instantiation, and repository persistence
    }
}
```

#### Viva Explanation:
> *"Abstraction hides implementation details and exposes only the interface. Our controllers depend strictly on service interfaces like `MedicineService` rather than concrete classes. This achieves loose coupling and allows replacing or mocking the implementation without altering controller code."*

---

### 5. Exception Handling

#### Where it is implemented:
- **Custom Exceptions**:
  - `src/main/java/com/pharmatech/exception/ResourceNotFoundException.java`
  - `src/main/java/com/pharmatech/exception/UserAlreadyExistsException.java`
  - `src/main/java/com/pharmatech/exception/UnauthorizedException.java`
  - `src/main/java/com/pharmatech/exception/BadRequestException.java`
- **Centralized Handler**:
  - `src/main/java/com/pharmatech/exception/GlobalExceptionHandler.java` (using `@RestControllerAdvice` and `@ExceptionHandler`)

#### Code Snippet:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest req) {
        ErrorDetails error = new ErrorDetails(LocalDateTime.now(), ex.getMessage(), req.getDescription(false), 404);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```

#### Viva Explanation:
> *"We built a robust exception handling framework using custom runtime exceptions and Spring's `@RestControllerAdvice`. Instead of letting unhandled errors return generic 500 stack traces, our `GlobalExceptionHandler` intercepts exceptions and formats them into clear, structured JSON error responses."*

---

### 6. Constructors and Constructor Chaining

#### Where it is implemented:
- `src/main/java/com/pharmatech/model/Customer.java` invokes `super(...)` to pass parameters to the parent `User` constructor:

```java
public Customer(String fullName, String email, String phoneNumber, String password,
                String deliveryAddress, String city, String pincode) {
    super(fullName, email, phoneNumber, password, Role.ROLE_CUSTOMER);
    this.deliveryAddress = deliveryAddress;
    this.city = city;
    this.pincode = pincode;
}
```

---

### 7. Java Generics and Collections

#### Where it is implemented:
- **Generics**: `src/main/java/com/pharmatech/dto/ApiResponse.java` (`public class ApiResponse<T>`) creates a type-safe envelope for any payload (`List<InventoryItemDTO>`, `AuthResponse`, etc.).
- **Collections & Streams API**: `src/main/java/com/pharmatech/service/impl/InventoryServiceImpl.java`:
  ```java
  return inventoryRepository.findByPharmacyId(pharmacyId)
          .stream()
          .map(this::convertToDTO)
          .collect(Collectors.toList());
  ```

---

### 8. Spring Framework Annotations Explained

| Annotation | Usage in PharmaTech |
| :--- | :--- |
| `@Entity` | Marks a Java class as a JPA entity mapped to a MySQL table. |
| `@Table` | Specifies the exact database table name (`users`, `medicines`, `inventories`). |
| `@Inheritance(strategy = InheritanceType.JOINED)` | Maps class inheritance hierarchy to joined relational tables. |
| `@Id`, `@GeneratedValue` | Specifies primary key and auto-increment identity strategy. |
| `@ManyToOne`, `@OneToMany`, `@OneToOne` | Configures relational cardinality between entities. |
| `@RestController`, `@RequestMapping` | Exposes RESTful HTTP endpoints returning JSON. |
| `@Service`, `@Transactional` | Marks business logic layer and guarantees ACID database transactions. |
| `@Repository` | Spring Data JPA interface generating SQL queries automatically. |
| `@CrossOrigin` | Enables Cross-Origin Resource Sharing for frontend communication. |
| `@RestControllerAdvice` | Centralizes exception handling across all REST controllers. |
| `@Valid`, `@NotBlank`, `@Email`, `@Min` | Enforces declarative validation rules before controller execution. |

---

### 9. ACID Transactions & Atomic Stock Synchronization

#### Where it is implemented:
- **File**: `src/main/java/com/pharmatech/service/impl/BillingServiceImpl.java` (`createBill`, `cancelBill`)
- **File**: `src/main/java/com/pharmatech/service/impl/InvoiceProcessingServiceImpl.java` (`confirmAndUpdateStock`)

#### Code Snippet:
```java
// BillingServiceImpl.java
@Override
@Transactional
public BillResponseDTO createBill(BillRequestDTO request) {
    // 1. Validate pharmacy exists
    Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())...;

    // 2. Atomically check and deduct stock for all billed items
    for (BillItemRequestDTO itemDTO : request.getItems()) {
        Inventory inventory = inventoryRepository.findById(itemDTO.getInventoryId())...;
        if (inventory.getQuantity() < itemDTO.getQuantity()) {
            throw new BadRequestException("Insufficient stock for '" + inventory.getMedicine().getName() + "'.");
        }
        // Deduct inventory atomically
        inventory.setQuantity(inventory.getQuantity() - itemDTO.getQuantity());
        inventory.setAvailable(inventory.getQuantity() > 0);
        inventoryRepository.save(inventory);
    }

    // 3. Persist Bill & BillItems inside the SAME database transaction
    Bill bill = new Bill(...);
    return convertToDTO(billRepository.save(bill));
}
```

#### How it works in PharmaTech:
Spring's `@Transactional` ensures **Atomicity, Consistency, Isolation, and Durability (ACID)**:
1. If any medicine has insufficient stock or a database constraint fails, the entire transaction **rolls back**. No bill is created, and no stock is deducted.
2. When the pharmacy owner confirms a purchase invoice, stock increments and invoice audit records are saved atomically.
3. When a bill is cancelled via `cancelBill(billId)`, the bill status changes to `CANCELLED` and every line item's quantity is atomically added back to the inventory in a single transaction.

#### Viva Explanation:
> *"Sir/Madam, We applied Spring's `@Transactional` annotation on our service methods. In `createBill()`, deducting inventory quantity and creating the bill record happen in a single atomic transaction. If any medicine runs out of stock midway, the entire operation is automatically rolled back, guaranteeing zero data inconsistency or negative stock."*

---

### 10. Two-Stage Processing Pattern (Preview vs Confirmation)

#### Where it is implemented:
- **File**: `src/main/java/com/pharmatech/service/impl/InvoiceProcessingServiceImpl.java`
- **File**: `src/main/java/com/pharmatech/controller/InvoiceController.java`

#### Design Pattern:
1. **Stage 1 (Extraction & Preview)**: The owner uploads a PDF/image/text invoice via `POST /api/pharmacy/invoice/extract/{id}`. Apache PDFBox extracts the text and regular expressions parse medicine names, batches, expiry dates, quantities, and prices. The service retrieves the current stock from the database, compares it, and returns an **editable preview** (`InvoiceParseResultDTO`) with `confirmed = false`. **The database is NOT modified.**
2. **Owner Review & Correction**: The owner can correct any OCR typos, add missing items, or remove faulty entries on the web dashboard.
3. **Stage 2 (Confirmation)**: The owner clicks "Confirm & Update Stock", submitting the reviewed list to `POST /api/pharmacy/invoice/confirm/{id}`.
4. **Duplicate Rejection**: The backend checks `invoiceUploadRepository.existsByPharmacyAndInvoiceNumber(pharmacy, invoiceNumber)`. If the invoice was already processed, it blocks duplicate submissions with HTTP 400 Bad Request.

---

### 11. Top Viva Questions & Ready-to-Speak Answers

#### Q1: "Why did you choose Spring Boot over pure Servlets / JSP?"
> *"Spring Boot provides production-grade features like embedded Tomcat server, Spring Data JPA with Hibernate ORM, declarative transaction management via `@Transactional`, and dependency injection, eliminating thousands of lines of boilerplate JDBC code."*

#### Q2: "How did you prevent negative stock or over-billing?"
> *"In `BillingServiceImpl.java`, we perform a strict quantity check before deducting stock: `if (inventory.getQuantity() < requestedQty) throw new BadRequestException(...)`. Both the check and update occur inside a `@Transactional` block, ensuring atomic consistency."*

#### Q3: "What prevents duplicate invoice processing?"
> *"In `InvoiceProcessingServiceImpl.java`, we query `InvoiceUploadRepository.existsByPharmacyAndInvoiceNumber(pharmacy, invoiceNumber)`. If the invoice number already exists for that pharmacy, a `BadRequestException` is thrown immediately, preventing double stock additions."*

#### Q4: "How does runtime polymorphism help in your User model?"
> *"Our base class `User` declares the method `getUserSummary()`. The subclasses `Customer` and `PharmacyOwner` provide their own `@Override` implementations. When invoked through a `User` reference, dynamic method dispatch executes the subclass-specific version based on the runtime type of the object."*

