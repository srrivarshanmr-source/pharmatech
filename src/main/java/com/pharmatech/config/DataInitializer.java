package com.pharmatech.config;

import com.pharmatech.model.*;
import com.pharmatech.repository.*;
import com.pharmatech.service.SettlementService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Seeds realistic demonstration data:
 * - 6 Coimbatore Partner Pharmacies (Shree Lakshmi Medicals, KMCH Wellness, Apollo Pharmacy Gandhipuram,
 *   MedPlus Wellness Peelamedu, Thulasi Pharmacies RS Puram, Ganga Healthcare Pharmacy)
 * - 2 Chennai Partner Pharmacies (Apollo Pharmacy Greams Road, MedPlus Wellness Anna Nagar)
 * - Zero distant/Bangalore pharmacies, ensuring Coimbatore location displays only Coimbatore pharmacies
 *   and changing location displays the target city's pharmacies.
 * - Dedicated Delivery Partners with assigned service zones and vehicles (Murugan K, Senthil Kumar, etc.)
 * - Automatic money transfer ledger records in both Admin and Pharmacy Owner histories.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PharmacyOwnerRepository pharmacyOwnerRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final BillRepository billRepository;
    private final PharmacyPayoutAccountRepository pharmacyPayoutAccountRepository;
    private final SettlementService settlementService;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PharmacyOwnerRepository pharmacyOwnerRepository,
                           PharmacyRepository pharmacyRepository,
                           MedicineRepository medicineRepository,
                           InventoryRepository inventoryRepository,
                           SubscriptionRepository subscriptionRepository,
                           DeliveryPartnerRepository deliveryPartnerRepository,
                           OrderRepository orderRepository,
                           BillRepository billRepository,
                           PharmacyPayoutAccountRepository pharmacyPayoutAccountRepository,
                           SettlementService settlementService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.pharmacyOwnerRepository = pharmacyOwnerRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
        this.inventoryRepository = inventoryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.orderRepository = orderRepository;
        this.billRepository = billRepository;
        this.pharmacyPayoutAccountRepository = pharmacyPayoutAccountRepository;
        this.settlementService = settlementService;
    }

    @Override
    public void run(String... args) {
        if (medicineRepository.count() > 0) {
            return; // Already initialized
        }

        System.out.println("=================================================");
        System.out.println("  PharmaTech: Seeding Coimbatore & Chennai Data  ");
        System.out.println("=================================================");

        // 1. Seed Master Medicine Catalog
        List<Medicine> catalog = Arrays.asList(
                new Medicine("Dolo 650", "Micro Labs", "650mg", "Tablets",
                        "Paracetamol 650mg for fast fever and headache relief.",
                        new BigDecimal("30.50"), false, "dolo650.svg"),
                new Medicine("Augmentin 625 Duo", "GSK", "625mg", "Tablets",
                        "Amoxicillin and Potassium Clavulanate antibacterial tablet.",
                        new BigDecimal("204.00"), true, "augmentin.svg"),
                new Medicine("Pan 40", "Alkem Laboratories", "40mg", "Tablets",
                        "Pantoprazole gastro-resistant tablets for acidity and GERD.",
                        new BigDecimal("155.00"), false, "pan40.svg"),
                new Medicine("Benadryl Cough Syrup", "Johnson & Johnson", "150ml", "Syrups",
                        "Soothing cough syrup for dry and allergic cough relief.",
                        new BigDecimal("138.00"), false, "benadryl.svg"),
                new Medicine("Ascoril D Plus Syrup", "Glenmark", "100ml", "Syrups",
                        "Sugar-free cough syrup for persistent throat irritation.",
                        new BigDecimal("115.00"), false, "ascoril.svg"),
                new Medicine("Betadine 10% Antiseptic Solution", "Win-Medicare", "100ml", "First Aid",
                        "Povidone-Iodine topical antiseptic for cuts, burns and wounds.",
                        new BigDecimal("145.00"), false, "betadine.svg"),
                new Medicine("Band-Aid Waterproof Strips", "Johnson & Johnson", "Pack of 20", "First Aid",
                        "Waterproof antiseptic adhesive bandages for minor wounds.",
                        new BigDecimal("85.00"), false, "bandaid.svg"),
                new Medicine("Becosules Z Capsules", "Pfizer", "30 Capsules", "Vitamins",
                        "Vitamin B-Complex with Vitamin C and Zinc for daily vitality.",
                        new BigDecimal("54.00"), false, "becosules.svg"),
                new Medicine("Shelcal 500", "Torrent Pharma", "15 Tablets", "Vitamins",
                        "Calcium 500mg and Vitamin D3 for bone and joint strength.",
                        new BigDecimal("131.00"), false, "shelcal.svg"),
                new Medicine("Dettol Antiseptic Liquid", "Reckitt", "550ml", "Personal Care",
                        "Multi-purpose antiseptic disinfection for skin cuts and hygiene.",
                        new BigDecimal("215.00"), false, "dettol.svg"),
                new Medicine("Cetaphil Gentle Skin Cleanser", "Galderma", "125ml", "Personal Care",
                        "Hydrating face and body wash recommended by dermatologists.",
                        new BigDecimal("335.00"), false, "cetaphil.svg"),
                new Medicine("Omron HEM-7120 BP Monitor", "Omron Healthcare", "1 Unit", "Healthcare Devices",
                        "Fully automatic digital blood pressure monitor with Intellisense technology.",
                        new BigDecimal("2150.00"), false, "omron_bp.svg"),
                new Medicine("Accu-Chek Active Glucometer", "Roche", "Kit + 10 Strips", "Healthcare Devices",
                        "Blood glucose monitor for accurate and easy diabetes self-testing.",
                        new BigDecimal("1420.00"), false, "accuchek.svg")
        );
        List<Medicine> savedMedicines = medicineRepository.saveAll(catalog);

        // 2. Seed Customer (Rahul Sharma - Neelambur, Coimbatore)
        Customer customer = new Customer(
                "Rahul Sharma",
                "customer@pharmatech.com",
                "9876543210",
                hashPassword("password123"),
                "14/B, Avinashi Road, Neelambur",
                "Coimbatore",
                "641062",
                "Neelambur",
                11.0583,
                77.0872
        );
        customerRepository.save(customer);

        // =====================================================================
        // COIMBATORE PARTNER PHARMACIES
        // =====================================================================

        // Pharmacy 1: Shree Lakshmi Medicals - Sitra, Coimbatore, Tamil Nadu
        PharmacyOwner ownerLakshmi = new PharmacyOwner(
                "Shree Lakshmi Medicals",
                "owner@shreelakshmi.com",
                "9842211223",
                hashPassword("password123"),
                "DL-TN-2024-771199"
        );
        ownerLakshmi.setSubscriptionActive(true);
        ownerLakshmi.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(30));
        PharmacyOwner savedOwnerLakshmi = pharmacyOwnerRepository.save(ownerLakshmi);

        Pharmacy pharmacyLakshmi = new Pharmacy(
                "Shree Lakshmi Medicals",
                "DL-TN-2024-771199",
                "123, Avinashi Rd, Sitra",
                "Coimbatore",
                "641014",
                "0422-2571234",
                11.0336,
                77.0396,
                "08:00 AM - 11:00 PM",
                "shreelakshmimedicals@okaxis",
                savedOwnerLakshmi
        );
        pharmacyLakshmi.setRating(4.9);
        Pharmacy savedPharmacyLakshmi = pharmacyRepository.save(pharmacyLakshmi);
        savedOwnerLakshmi.setPharmacy(savedPharmacyLakshmi);

        Subscription subLakshmi = new Subscription(
                savedOwnerLakshmi, "PharmaTech Pharmacy Partner Plan", new BigDecimal("200.00"),
                "ACTIVE", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(29), "SIM-TXN-LAKSHMI88"
        );
        subscriptionRepository.save(subLakshmi);

        List<Inventory> inventoryLakshmi = Arrays.asList(
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(0), 150, new BigDecimal("30.50"), LocalDate.now().plusMonths(18), "BAT-SLM-DOLO"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(1), 45, new BigDecimal("204.00"), LocalDate.now().plusMonths(14), "BAT-SLM-AUG"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(2), 60, new BigDecimal("155.00"), LocalDate.now().plusMonths(24), "BAT-SLM-PAN"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(3), 25, new BigDecimal("138.00"), LocalDate.now().plusMonths(15), "BAT-SLM-BEN"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(4), 30, new BigDecimal("115.00"), LocalDate.now().plusMonths(20), "BAT-SLM-ASC"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(5), 40, new BigDecimal("145.00"), LocalDate.now().plusMonths(16), "BAT-SLM-BET"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(6), 85, new BigDecimal("85.00"), LocalDate.now().plusMonths(36), "BAT-SLM-BAN"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(7), 90, new BigDecimal("54.00"), LocalDate.now().plusMonths(20), "BAT-SLM-BEC"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(8), 80, new BigDecimal("131.00"), LocalDate.now().plusMonths(18), "BAT-SLM-SHE"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(9), 50, new BigDecimal("215.00"), LocalDate.now().plusMonths(28), "BAT-SLM-DET"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(10), 25, new BigDecimal("335.00"), LocalDate.now().plusMonths(18), "BAT-SLM-CET"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(11), 10, new BigDecimal("2150.00"), LocalDate.now().plusMonths(36), "BAT-SLM-OMR"),
                new Inventory(savedPharmacyLakshmi, savedMedicines.get(12), 15, new BigDecimal("1420.00"), LocalDate.now().plusMonths(36), "BAT-SLM-ACC")
        );
        inventoryRepository.saveAll(inventoryLakshmi);

        // Pharmacy 2: KMCH Wellness Pharmacy - Neelambur, Coimbatore, Tamil Nadu
        PharmacyOwner ownerKMCH = new PharmacyOwner(
                "Dr. S. K. Subramanian",
                "owner@kmchpharmacy.com",
                "9876500005",
                hashPassword("password123"),
                "DL-TN-2024-884422"
        );
        ownerKMCH.setSubscriptionActive(true);
        ownerKMCH.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(30));
        PharmacyOwner savedOwnerKMCH = pharmacyOwnerRepository.save(ownerKMCH);

        Pharmacy pharmacyKMCH = new Pharmacy(
                "KMCH Wellness Pharmacy - Neelambur, Coimbatore",
                "DL-TN-2024-884422",
                "Avinashi Road, Near PSG iTech, Neelambur",
                "Coimbatore",
                "641062",
                "0422-4323800",
                11.0583,
                77.0872,
                "24 Hours Open",
                "pharmatech.kmch@sbi",
                savedOwnerKMCH
        );
        pharmacyKMCH.setRating(4.9);
        Pharmacy savedPharmacyKMCH = pharmacyRepository.save(pharmacyKMCH);
        savedOwnerKMCH.setPharmacy(savedPharmacyKMCH);

        List<Inventory> inventoryKMCH = Arrays.asList(
                new Inventory(savedPharmacyKMCH, savedMedicines.get(0), 85, new BigDecimal("30.50"), LocalDate.now().plusMonths(18), "BAT-KMCH-D1"),
                new Inventory(savedPharmacyKMCH, savedMedicines.get(1), 40, new BigDecimal("204.00"), LocalDate.now().plusMonths(14), "BAT-KMCH-A2"),
                new Inventory(savedPharmacyKMCH, savedMedicines.get(2), 55, new BigDecimal("155.00"), LocalDate.now().plusMonths(24), "BAT-KMCH-P3"),
                new Inventory(savedPharmacyKMCH, savedMedicines.get(7), 45, new BigDecimal("54.00"), LocalDate.now().plusMonths(20), "BAT-KMCH-BC"),
                new Inventory(savedPharmacyKMCH, savedMedicines.get(8), 60, new BigDecimal("131.00"), LocalDate.now().plusMonths(18), "BAT-KMCH-SH")
        );
        inventoryRepository.saveAll(inventoryKMCH);

        // Pharmacy 3: Apollo Pharmacy - Gandhipuram, Coimbatore, Tamil Nadu
        PharmacyOwner ownerApollo = new PharmacyOwner(
                "Dr. Vikram Patel",
                "owner@apollo.com",
                "9888877771",
                hashPassword("password123"),
                "DL-TN-2024-984321"
        );
        ownerApollo.setSubscriptionActive(true);
        ownerApollo.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(28));
        PharmacyOwner savedOwnerApollo = pharmacyOwnerRepository.save(ownerApollo);

        Pharmacy pharmacyApollo = new Pharmacy(
                "Apollo Pharmacy - Gandhipuram, Coimbatore",
                "DL-TN-2024-984321",
                "Cross Cut Road, 7th Street, Gandhipuram",
                "Coimbatore",
                "641012",
                "0422-2495678",
                11.0168,
                76.9558,
                "24 Hours Open",
                "apollo.gandhipuram@okaxis",
                savedOwnerApollo
        );
        pharmacyApollo.setRating(4.8);
        Pharmacy savedPharmacyApollo = pharmacyRepository.save(pharmacyApollo);
        savedOwnerApollo.setPharmacy(savedPharmacyApollo);

        List<Inventory> inventoryApollo = Arrays.asList(
                new Inventory(savedPharmacyApollo, savedMedicines.get(0), 120, new BigDecimal("30.50"), LocalDate.now().plusMonths(18), "BAT-APO-D1"),
                new Inventory(savedPharmacyApollo, savedMedicines.get(1), 50, new BigDecimal("204.00"), LocalDate.now().plusMonths(12), "BAT-APO-A2"),
                new Inventory(savedPharmacyApollo, savedMedicines.get(2), 65, new BigDecimal("155.00"), LocalDate.now().plusMonths(24), "BAT-APO-P3"),
                new Inventory(savedPharmacyApollo, savedMedicines.get(3), 12, new BigDecimal("138.00"), LocalDate.now().plusMonths(15), "BAT-APO-B4"),
                new Inventory(savedPharmacyApollo, savedMedicines.get(4), 28, new BigDecimal("115.00"), LocalDate.now().plusMonths(20), "BAT-APO-C5")
        );
        inventoryRepository.saveAll(inventoryApollo);

        // Pharmacy 4: MedPlus Wellness - Peelamedu, Coimbatore, Tamil Nadu
        PharmacyOwner ownerMedPlus = new PharmacyOwner(
                "Ananya Sen",
                "owner@medplus.com",
                "9876500002",
                hashPassword("password123"),
                "DL-TN-2024-554411"
        );
        ownerMedPlus.setSubscriptionActive(true);
        ownerMedPlus.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(25));
        PharmacyOwner savedOwnerMedPlus = pharmacyOwnerRepository.save(ownerMedPlus);

        Pharmacy pharmacyMedPlus = new Pharmacy(
                "MedPlus Wellness - Peelamedu, Coimbatore",
                "DL-TN-2024-554411",
                "Avinashi Road, Near Fun Republic Mall, Peelamedu",
                "Coimbatore",
                "641004",
                "0422-2599988",
                11.0267,
                77.0118,
                "08:00 AM - 11:00 PM",
                "medplus.peelamedu@icici",
                savedOwnerMedPlus
        );
        pharmacyMedPlus.setRating(4.7);
        Pharmacy savedPharmacyMedPlus = pharmacyRepository.save(pharmacyMedPlus);
        savedOwnerMedPlus.setPharmacy(savedPharmacyMedPlus);

        List<Inventory> inventoryMedPlus = Arrays.asList(
                new Inventory(savedPharmacyMedPlus, savedMedicines.get(0), 95, new BigDecimal("29.50"), LocalDate.now().plusMonths(18), "BAT-MP-D1"),
                new Inventory(savedPharmacyMedPlus, savedMedicines.get(1), 35, new BigDecimal("200.00"), LocalDate.now().plusMonths(12), "BAT-MP-A2"),
                new Inventory(savedPharmacyMedPlus, savedMedicines.get(2), 50, new BigDecimal("150.00"), LocalDate.now().plusMonths(24), "BAT-MP-P4"),
                new Inventory(savedPharmacyMedPlus, savedMedicines.get(7), 60, new BigDecimal("52.00"), LocalDate.now().plusMonths(20), "BAT-MP-BC")
        );
        inventoryRepository.saveAll(inventoryMedPlus);

        // Pharmacy 5: Thulasi Pharmacies - RS Puram, Coimbatore, Tamil Nadu
        PharmacyOwner ownerThulasi = new PharmacyOwner(
                "Dr. Rajesh Hegde",
                "owner@thulasi.com",
                "9876500003",
                hashPassword("password123"),
                "DL-TN-2024-776633"
        );
        ownerThulasi.setSubscriptionActive(true);
        ownerThulasi.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(30));
        PharmacyOwner savedOwnerThulasi = pharmacyOwnerRepository.save(ownerThulasi);

        Pharmacy pharmacyThulasi = new Pharmacy(
                "Thulasi Pharmacies - RS Puram, Coimbatore",
                "DL-TN-2024-776633",
                "DB Road, Near Flower Market, RS Puram",
                "Coimbatore",
                "641002",
                "0422-2541122",
                11.0089,
                76.9511,
                "08:00 AM - 10:30 PM",
                "thulasi.rspuram@hdfcbank",
                savedOwnerThulasi
        );
        pharmacyThulasi.setRating(4.9);
        Pharmacy savedPharmacyThulasi = pharmacyRepository.save(pharmacyThulasi);
        savedOwnerThulasi.setPharmacy(savedPharmacyThulasi);

        List<Inventory> inventoryThulasi = Arrays.asList(
                new Inventory(savedPharmacyThulasi, savedMedicines.get(0), 110, new BigDecimal("30.00"), LocalDate.now().plusMonths(24), "BAT-THU-D1"),
                new Inventory(savedPharmacyThulasi, savedMedicines.get(1), 50, new BigDecimal("198.00"), LocalDate.now().plusMonths(18), "BAT-THU-A1"),
                new Inventory(savedPharmacyThulasi, savedMedicines.get(5), 35, new BigDecimal("140.00"), LocalDate.now().plusMonths(20), "BAT-THU-BT")
        );
        inventoryRepository.saveAll(inventoryThulasi);

        // Pharmacy 6: Ganga Healthcare Pharmacy - Saibaba Colony, Coimbatore, Tamil Nadu
        PharmacyOwner ownerGanga = new PharmacyOwner(
                "Meera Nambiar",
                "owner@gangahealth.com",
                "9876500004",
                hashPassword("password123"),
                "DL-TN-2024-332211"
        );
        ownerGanga.setSubscriptionActive(true);
        ownerGanga.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(20));
        PharmacyOwner savedOwnerGanga = pharmacyOwnerRepository.save(ownerGanga);

        Pharmacy pharmacyGanga = new Pharmacy(
                "Ganga Healthcare Pharmacy - Saibaba Colony, Coimbatore",
                "DL-TN-2024-332211",
                "313, Mettupalayam Road, Saibaba Colony",
                "Coimbatore",
                "641043",
                "0422-2485000",
                11.0289,
                76.9467,
                "24 Hours Open",
                "gangahealth@sbi",
                savedOwnerGanga
        );
        pharmacyGanga.setRating(4.8);
        Pharmacy savedPharmacyGanga = pharmacyRepository.save(pharmacyGanga);
        savedOwnerGanga.setPharmacy(savedPharmacyGanga);

        List<Inventory> inventoryGanga = Arrays.asList(
                new Inventory(savedPharmacyGanga, savedMedicines.get(0), 100, new BigDecimal("31.00"), LocalDate.now().plusMonths(20), "BAT-GAN-D1"),
                new Inventory(savedPharmacyGanga, savedMedicines.get(2), 65, new BigDecimal("152.00"), LocalDate.now().plusMonths(24), "BAT-GAN-P2"),
                new Inventory(savedPharmacyGanga, savedMedicines.get(8), 50, new BigDecimal("130.00"), LocalDate.now().plusMonths(18), "BAT-GAN-S3")
        );
        inventoryRepository.saveAll(inventoryGanga);

        // =====================================================================
        // CHENNAI PARTNER PHARMACIES (1 OR 2 CHENNAI PHARMACIES)
        // =====================================================================

        // Pharmacy 7: Apollo Pharmacy - Greams Road, Chennai
        PharmacyOwner ownerChennaiApollo = new PharmacyOwner(
                "Dr. Arumugam Pillai",
                "owner@apollochennai.com",
                "9840011223",
                hashPassword("password123"),
                "DL-TN-2024-112233"
        );
        ownerChennaiApollo.setSubscriptionActive(true);
        ownerChennaiApollo.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(30));
        PharmacyOwner savedOwnerChennaiApollo = pharmacyOwnerRepository.save(ownerChennaiApollo);

        Pharmacy pharmacyChennaiApollo = new Pharmacy(
                "Apollo Pharmacy - Greams Road, Chennai",
                "DL-TN-2024-112233",
                "No. 21, Greams Road, Thousand Lights",
                "Chennai",
                "600006",
                "044-28290200",
                13.0569,
                80.2525,
                "24 Hours Open",
                "apollo.greams@okaxis",
                savedOwnerChennaiApollo
        );
        pharmacyChennaiApollo.setRating(4.9);
        Pharmacy savedPharmacyChennaiApollo = pharmacyRepository.save(pharmacyChennaiApollo);
        savedOwnerChennaiApollo.setPharmacy(savedPharmacyChennaiApollo);

        List<Inventory> inventoryChennaiApollo = Arrays.asList(
                new Inventory(savedPharmacyChennaiApollo, savedMedicines.get(0), 140, new BigDecimal("30.50"), LocalDate.now().plusMonths(18), "BAT-CHN-D1"),
                new Inventory(savedPharmacyChennaiApollo, savedMedicines.get(1), 50, new BigDecimal("204.00"), LocalDate.now().plusMonths(14), "BAT-CHN-A2"),
                new Inventory(savedPharmacyChennaiApollo, savedMedicines.get(2), 70, new BigDecimal("155.00"), LocalDate.now().plusMonths(24), "BAT-CHN-P3")
        );
        inventoryRepository.saveAll(inventoryChennaiApollo);

        // Pharmacy 8: MedPlus Wellness - Anna Nagar, Chennai
        PharmacyOwner ownerChennaiMedPlus = new PharmacyOwner(
                "Kavitha Natarajan",
                "owner@medpluschennai.com",
                "9840022334",
                hashPassword("password123"),
                "DL-TN-2024-445566"
        );
        ownerChennaiMedPlus.setSubscriptionActive(true);
        ownerChennaiMedPlus.setSubscriptionExpiresAt(LocalDateTime.now().plusDays(30));
        PharmacyOwner savedOwnerChennaiMedPlus = pharmacyOwnerRepository.save(ownerChennaiMedPlus);

        Pharmacy pharmacyChennaiMedPlus = new Pharmacy(
                "MedPlus Wellness - Anna Nagar, Chennai",
                "DL-TN-2024-445566",
                "2nd Avenue, Near Roundtana, Anna Nagar",
                "Chennai",
                "600040",
                "044-26213344",
                13.0850,
                80.2101,
                "08:00 AM - 11:00 PM",
                "medplus.annanagar@icici",
                savedOwnerChennaiMedPlus
        );
        pharmacyChennaiMedPlus.setRating(4.8);
        Pharmacy savedPharmacyChennaiMedPlus = pharmacyRepository.save(pharmacyChennaiMedPlus);
        savedOwnerChennaiMedPlus.setPharmacy(savedPharmacyChennaiMedPlus);

        List<Inventory> inventoryChennaiMedPlus = Arrays.asList(
                new Inventory(savedPharmacyChennaiMedPlus, savedMedicines.get(0), 100, new BigDecimal("30.00"), LocalDate.now().plusMonths(18), "BAT-AN-D1"),
                new Inventory(savedPharmacyChennaiMedPlus, savedMedicines.get(3), 30, new BigDecimal("138.00"), LocalDate.now().plusMonths(15), "BAT-AN-B2"),
                new Inventory(savedPharmacyChennaiMedPlus, savedMedicines.get(4), 25, new BigDecimal("115.00"), LocalDate.now().plusMonths(20), "BAT-AN-C3")
        );
        inventoryRepository.saveAll(inventoryChennaiMedPlus);

        // =====================================================================
        // DELIVERY PARTNERS
        // =====================================================================
        DeliveryPartner dpMurugan = new DeliveryPartner(
                "Murugan K", "murugan.delivery@pharmatech.com", "9845099885",
                hashPassword("password123"), "TN-38-BZ-2144", "DL-TN-2022-998811",
                "Coimbatore", "Neelambur", 11.0583, 77.0872
        );
        dpMurugan.setTotalDeliveries(16);
        dpMurugan.setAvailable(true);
        dpMurugan.setOnline(true);
        DeliveryPartner savedDpMurugan = deliveryPartnerRepository.save(dpMurugan);

        DeliveryPartner dpSenthil = new DeliveryPartner(
                "Senthil Kumar", "senthil.delivery@pharmatech.com", "9845099881",
                hashPassword("password123"), "TN-38-CA-8899", "DL-TN-2023-112233",
                "Coimbatore", "Sitra", 11.0336, 77.0396
        );
        dpSenthil.setTotalDeliveries(19);
        dpSenthil.setAvailable(true);
        dpSenthil.setOnline(true);
        deliveryPartnerRepository.save(dpSenthil);

        DeliveryPartner dpVelu = new DeliveryPartner(
                "Velu M", "velu.delivery@pharmatech.com", "9845099882",
                hashPassword("password123"), "TN-37-DE-1144", "DL-TN-2021-445566",
                "Coimbatore", "Gandhipuram", 11.0168, 76.9558
        );
        dpVelu.setTotalDeliveries(24);
        dpVelu.setAvailable(true);
        dpVelu.setOnline(true);
        deliveryPartnerRepository.save(dpVelu);

        DeliveryPartner dpKarthik = new DeliveryPartner(
                "Karthik Raja", "karthik.delivery@pharmatech.com", "9845099884",
                hashPassword("password123"), "TN-01-AB-3344", "DL-TN-2020-554422",
                "Chennai", "Anna Nagar", 13.0850, 80.2101
        );
        dpKarthik.setTotalDeliveries(28);
        dpKarthik.setAvailable(true);
        dpKarthik.setOnline(true);
        deliveryPartnerRepository.save(dpKarthik);

        DeliveryPartner dpRamesh = new DeliveryPartner(
                "Ramesh Kumar", "ramesh.delivery@pharmatech.com", "9845099886",
                hashPassword("password123"), "TN-02-XY-9900", "DL-TN-2019-778899",
                "Chennai", "Greams Road", 13.0569, 80.2525
        );
        dpRamesh.setTotalDeliveries(31);
        dpRamesh.setAvailable(true);
        dpRamesh.setOnline(true);
        deliveryPartnerRepository.save(dpRamesh);

        // =====================================================================
        // SAMPLE ORDERS
        // =====================================================================
        // Order 1: Placed, paid UPI order, sent to delivery pool with rider unassigned
        // (Customer tracking shows "Awaiting Delivery Partner Acceptance" until rider accepts)
        Order order1 = new Order(
                "ORD-2026-8812", customer, savedPharmacyLakshmi, null,
                customer.getFullName(), customer.getPhoneNumber(), "Neelambur, Avinashi Road, Coimbatore, Tamil Nadu 641062",
                new BigDecimal("234.50"), "ORDER_PLACED", "EMERGENCY", "UPI", "PAID",
                11.0583, 77.0872, 7.1, 14, new BigDecimal("75.00")
        );
        order1.addItem(new OrderItem(savedMedicines.get(0), "Dolo 650", 2, new BigDecimal("30.50"), new BigDecimal("61.00")));
        order1.addItem(new OrderItem(savedMedicines.get(3), "Benadryl Cough Syrup", 1, new BigDecimal("138.00"), new BigDecimal("138.00")));
        order1.setPaymentApprovalStatus("APPROVED");
        order1.setPaymentApprovedAt(LocalDateTime.now().minusMinutes(10));
        orderRepository.save(order1);

        // Order 2: Active delivery in transit by Murugan K (COD)
        Order order2 = new Order(
                "ORD-2026-9045", customer, savedPharmacyLakshmi, savedDpMurugan,
                "Rahul Sharma", "9876543210", "Neelambur, Avinashi Road, Coimbatore, Tamil Nadu 641062",
                new BigDecimal("419.00"), "OUT_FOR_DELIVERY", "NORMAL", "COD", "PENDING_COLLECTION",
                11.0583, 77.0872, 7.1, 22, new BigDecimal("45.00")
        );
        order2.addItem(new OrderItem(savedMedicines.get(1), "Augmentin 625 Duo", 1, new BigDecimal("204.00"), new BigDecimal("204.00")));
        order2.addItem(new OrderItem(savedMedicines.get(9), "Dettol Antiseptic Liquid", 1, new BigDecimal("215.00"), new BigDecimal("215.00")));
        order2.setPickedUpAt(LocalDateTime.now().minusMinutes(5));
        orderRepository.save(order2);

        // Order 3: Delivered COD order with cash collected & settled
        Order order3 = new Order(
                "ORD-2026-7731", customer, savedPharmacyLakshmi, savedDpMurugan,
                "Pooja Verma", "9876500112", "Neelambur, Avinashi Road, Coimbatore, Tamil Nadu 641062",
                new BigDecimal("185.00"), "DELIVERED", "NORMAL", "COD", "PAID",
                11.0583, 77.0872, 7.1, 25, new BigDecimal("45.00")
        );
        order3.addItem(new OrderItem(savedMedicines.get(8), "Shelcal 500", 1, new BigDecimal("131.00"), new BigDecimal("131.00")));
        order3.addItem(new OrderItem(savedMedicines.get(7), "Becosules Z Capsules", 1, new BigDecimal("54.00"), new BigDecimal("54.00")));
        order3.setDeliveredAt(LocalDateTime.now().minusHours(3));
        orderRepository.save(order3);

        // 10. Seed Sample Bill
        Bill sampleBill = new Bill(
                "BILL-2026-1001", savedPharmacyLakshmi, "Walk-in Customer (Amit Roy)", "9845011223",
                new BigDecimal("305.00"), new BigDecimal("15.25"), BigDecimal.ZERO, new BigDecimal("320.25"), "UPI"
        );
        sampleBill.addItem(new BillItem(savedMedicines.get(0), "Dolo 650", "BAT-SLM-DOLO", new BigDecimal("30.50"), 2, new BigDecimal("61.00")));
        sampleBill.addItem(new BillItem(savedMedicines.get(1), "Augmentin 625 Duo", "BAT-SLM-AUG", new BigDecimal("204.00"), 1, new BigDecimal("204.00")));
        billRepository.save(sampleBill);

        // 11. Seed Admin Account
        User admin = new User(
                "PharmaTech System Administrator", "admin@pharmatech.com", "9800011122",
                hashPassword("admin123"), Role.ROLE_ADMIN
        );
        userRepository.save(admin);

        // 12. Seed Verified Pharmacy Payout Accounts (Marketplace Linked Route Accounts)
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyLakshmi, savedOwnerLakshmi, "acc_shreelakshmi_route_01", "State Bank of India", "•••• 6414", "SBIN0006414", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyKMCH, savedOwnerKMCH, "acc_kmch_route_02", "Canara Bank", "•••• 7733", "CNRB0007733", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyApollo, savedOwnerApollo, "acc_apollo_route_03", "HDFC Bank", "•••• 9821", "HDFC0001234", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyMedPlus, savedOwnerMedPlus, "acc_medplus_route_04", "ICICI Bank", "•••• 5541", "ICIC0005541", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyThulasi, savedOwnerThulasi, "acc_thulasi_route_05", "HDFC Bank", "•••• 8812", "HDFC0008812", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyGanga, savedOwnerGanga, "acc_ganga_route_06", "Axis Bank", "•••• 4422", "UTIB0004422", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyChennaiApollo, savedOwnerChennaiApollo, "acc_chennai_apollo_07", "Indian Overseas Bank", "•••• 1122", "IOBA0001122", "VERIFIED", "ACTIVE"
        ));
        pharmacyPayoutAccountRepository.save(new PharmacyPayoutAccount(
                savedPharmacyChennaiMedPlus, savedOwnerChennaiMedPlus, "acc_chennai_medplus_08", "State Bank of India", "•••• 4455", "SBIN0004455", "VERIFIED", "ACTIVE"
        ));

        // 13. Automatic Money Sending from Admin to Pharmacy Owners
        // Automatically settle eligible initial orders so payouts appear in both Admin and Pharmacy histories immediately
        try {
            settlementService.autoSettleAllPendingOrders();
        } catch (Exception e) {
            System.out.println("Notice on initial auto-settlement: " + e.getMessage());
        }

        System.out.println("  6 Coimbatore Pharmacies & 2 Chennai Pharmacies Seeded!  ");
        System.out.println("  Automatic Admin-to-Owner Settlements Initialized!       ");
        System.out.println("=================================================");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}
