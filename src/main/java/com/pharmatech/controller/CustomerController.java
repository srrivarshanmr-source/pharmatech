package com.pharmatech.controller;

import com.pharmatech.dto.*;
import com.pharmatech.model.Customer;
import com.pharmatech.model.Pharmacy;
import com.pharmatech.service.CustomerService;
import com.pharmatech.service.InventoryService;
import com.pharmatech.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller serving the Customer Interface:
 * - Product catalog & Categories
 * - Location suggestions & coordinates
 * - 10 km Radius Medicine Search with nearby pharmacies
 * - Customer Order Placement & Atomic Stock Reservation
 * - Live Order Tracking
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final InventoryService inventoryService;
    private final MedicineService medicineService;
    private final CustomerService customerService;

    public CustomerController(InventoryService inventoryService,
                              MedicineService medicineService,
                              CustomerService customerService) {
        this.inventoryService = inventoryService;
        this.medicineService = medicineService;
        this.customerService = customerService;
    }

    /**
     * Get available products with optional search and category filters.
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<InventoryItemDTO>>> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city) {
        List<InventoryItemDTO> products = inventoryService.getAvailableProductsForCustomer(query, category, city);
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    /**
     * Get distinct medicine categories for homepage navigation.
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = medicineService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    /**
     * Search for medicines across all pharmacies within a 10 km radius.
     */
    @GetMapping("/search-nearby")
    public ResponseEntity<ApiResponse<List<NearbyPharmacySearchResultDTO>>> searchNearby(
            @RequestParam(required = false) String medicine,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "10.0") Double radius) {
        List<NearbyPharmacySearchResultDTO> results = customerService.searchNearbyPharmacies(medicine, lat, lng, radius);
        return ResponseEntity.ok(ApiResponse.ok("Nearby pharmacies search completed", results));
    }

    @org.springframework.beans.factory.annotation.Value("${google.maps.api-key:}")
    private String googleMapsApiKey;

    /**
     * Get Google Maps client configuration
     */
    @GetMapping("/maps/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMapsConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", googleMapsApiKey != null ? googleMapsApiKey : "");
        config.put("hasCustomKey", googleMapsApiKey != null && !googleMapsApiKey.trim().isEmpty() && !googleMapsApiKey.startsWith("YOUR_"));
        return ResponseEntity.ok(ApiResponse.ok("Maps configuration", config));
    }

    /**
     * Pre-defined verified nationwide location database for instant autocomplete & coordinate resolution.
     * Supports query parameter for instant filtering across cities, localities, and pincodes.
     */
    @GetMapping("/locations/suggest")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> suggestLocations(
            @RequestParam(required = false) String query) {
        List<Map<String, Object>> allLocations = Arrays.asList(
                // Tamil Nadu - Coimbatore
                createLocation("Neelambur, Coimbatore, Tamil Nadu", "Neelambur", "Coimbatore", "641062", 11.0583, 77.0872),
                createLocation("SITRA, Avinashi Road, Coimbatore, Tamil Nadu", "SITRA", "Coimbatore", "641014", 11.0336, 77.0396),
                createLocation("Avinashi Road, Coimbatore, Tamil Nadu", "Avinashi Road", "Coimbatore", "641014", 11.0315, 77.0298),
                createLocation("Gandhipuram, Coimbatore, Tamil Nadu", "Gandhipuram", "Coimbatore", "641012", 11.0168, 76.9558),
                createLocation("RS Puram, Coimbatore, Tamil Nadu", "RS Puram", "Coimbatore", "641002", 11.0089, 76.9511),
                createLocation("Peelamedu, Coimbatore, Tamil Nadu", "Peelamedu", "Coimbatore", "641004", 11.0267, 77.0118),
                createLocation("Singanallur, Coimbatore, Tamil Nadu", "Singanallur", "Coimbatore", "641005", 10.9996, 77.0261),
                // Tamil Nadu - Chennai
                createLocation("Anna Nagar, Chennai, Tamil Nadu", "Anna Nagar", "Chennai", "600040", 13.0850, 80.2101),
                createLocation("Greams Road, Thousand Lights, Chennai", "Greams Road", "Chennai", "600006", 13.0569, 80.2525),
                createLocation("T. Nagar, Chennai, Tamil Nadu", "T. Nagar", "Chennai", "600017", 13.0418, 80.2341),
                createLocation("Velachery, Chennai, Tamil Nadu", "Velachery", "Chennai", "600042", 12.9815, 80.2180)
        );

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("Verified nationwide locations loaded", allLocations));
        }

        String q = query.trim().toLowerCase();
        List<Map<String, Object>> filtered = allLocations.stream()
                .filter(loc -> {
                    String label = String.valueOf(loc.get("label")).toLowerCase();
                    String locality = String.valueOf(loc.get("locality")).toLowerCase();
                    String city = String.valueOf(loc.get("city")).toLowerCase();
                    String pin = String.valueOf(loc.get("pincode")).toLowerCase();
                    return label.contains(q) || locality.contains(q) || city.contains(q) || pin.contains(q);
                })
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok("Location suggestions filtered", filtered));
    }

    private Map<String, Object> createLocation(String label, String locality, String city, String pincode, double lat, double lng) {
        Map<String, Object> map = new HashMap<>();
        map.put("label", label);
        map.put("area", locality); // Support 'area'
        map.put("locality", locality); // Support 'locality'
        map.put("city", city);
        map.put("pincode", pincode);
        map.put("latitude", lat);
        map.put("longitude", lng);
        return map;
    }

    /**
     * Create customer medicine order with atomic stock deduction and partner assignment.
     */
    @PostMapping("/orders/create")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        OrderResponseDTO created = customerService.createOrder(request);
        return new ResponseEntity<>(ApiResponse.ok("Order placed successfully. Selected pharmacy and delivery partner notified.", created), HttpStatus.CREATED);
    }

    /**
     * Track customer order by ID.
     */
    @GetMapping("/orders/track/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> trackOrder(@PathVariable Long orderId) {
        OrderResponseDTO order = customerService.trackOrder(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Order tracking loaded", order));
    }

    /**
     * Track customer order by Order Number (e.g. ORD-2026-XXXX).
     */
    @GetMapping({"/orders/track-number/{orderNumber}", "/orders/track-by-number/{orderNumber}"})
    public ResponseEntity<ApiResponse<OrderResponseDTO>> trackOrderByNumber(@PathVariable String orderNumber) {
        OrderResponseDTO order = customerService.trackOrderByNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok("Order tracking loaded", order));
    }

    /**
     * Get orders for a logged-in customer.
     */
    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> getCustomerOrders(@PathVariable Long customerId) {
        List<OrderResponseDTO> orders = customerService.getCustomerOrders(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Customer orders loaded", orders));
    }

    /**
     * Update customer delivery address & coordinates.
     */
    @PutMapping("/location/{customerId}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomerLocation(
            @PathVariable Long customerId,
            @RequestBody Map<String, Object> body) {
        String locality = (String) body.get("locality");
        String city = (String) body.get("city");
        String pincode = (String) body.get("pincode");
        Double latitude = body.get("latitude") != null ? Double.valueOf(body.get("latitude").toString()) : null;
        Double longitude = body.get("longitude") != null ? Double.valueOf(body.get("longitude").toString()) : null;

        Customer updated = customerService.updateLocationCoordinates(customerId, locality, city, pincode, latitude, longitude);
        return ResponseEntity.ok(ApiResponse.ok("Location updated successfully", updated));
    }

    /**
     * Get list of nearby registered pharmacies.
     */
    @GetMapping("/nearby-pharmacies")
    public ResponseEntity<ApiResponse<List<Pharmacy>>> getNearbyPharmacies(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String pincode) {
        List<Pharmacy> pharmacies = customerService.getNearbyPharmacies(city, pincode);
        return ResponseEntity.ok(ApiResponse.ok(pharmacies));
    }

    /**
     * Get customer profile.
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerProfile(@PathVariable Long id) {
        Customer customer = customerService.getCustomerProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(customer));
    }
}
