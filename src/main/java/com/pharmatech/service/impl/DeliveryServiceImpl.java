package com.pharmatech.service.impl;

import com.pharmatech.dto.AuthResponse;
import com.pharmatech.dto.LoginRequest;
import com.pharmatech.dto.OrderResponseDTO;
import com.pharmatech.exception.BadRequestException;
import com.pharmatech.exception.ResourceNotFoundException;
import com.pharmatech.exception.UnauthorizedException;
import com.pharmatech.model.DeliveryPartner;
import com.pharmatech.model.Order;
import com.pharmatech.model.Role;
import com.pharmatech.model.User;
import com.pharmatech.repository.DeliveryPartnerRepository;
import com.pharmatech.repository.OrderRepository;
import com.pharmatech.repository.UserRepository;
import com.pharmatech.service.DeliveryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final UserRepository userRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final com.pharmatech.repository.RiderLocationUpdateRepository riderLocationUpdateRepository;
    private final com.pharmatech.service.SettlementService settlementService;
    private final com.pharmatech.repository.CashRemittanceRepository cashRemittanceRepository;
    private final com.pharmatech.repository.PaymentTransactionRepository paymentTransactionRepository;

    public DeliveryServiceImpl(UserRepository userRepository,
                               DeliveryPartnerRepository deliveryPartnerRepository,
                               OrderRepository orderRepository,
                               com.pharmatech.repository.RiderLocationUpdateRepository riderLocationUpdateRepository,
                               com.pharmatech.service.SettlementService settlementService,
                               com.pharmatech.repository.CashRemittanceRepository cashRemittanceRepository,
                               com.pharmatech.repository.PaymentTransactionRepository paymentTransactionRepository) {
        this.userRepository = userRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.orderRepository = orderRepository;
        this.riderLocationUpdateRepository = riderLocationUpdateRepository;
        this.settlementService = settlementService;
        this.cashRemittanceRepository = cashRemittanceRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim().toLowerCase();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByEmailOrPhoneNumber(identifier, identifier))
                .orElseThrow(() -> new UnauthorizedException("Invalid email/phone or password"));

        if (!validatePassword(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email/phone or password");
        }

        if (user.getRole() != Role.ROLE_DELIVERY_PARTNER) {
            throw new UnauthorizedException("This account is not registered as a Delivery Partner");
        }

        DeliveryPartner partner = deliveryPartnerRepository.findById(user.getId())
                .orElseThrow(() -> new UnauthorizedException("Delivery Partner profile not found"));

        String token = "deliv-" + UUID.randomUUID().toString();

        return new AuthResponse(
                partner.getId(),
                partner.getFullName(),
                partner.getEmail(),
                partner.getPhoneNumber(),
                partner.getRole(),
                null,
                partner.getVehicleNumber(),
                true,
                token,
                "Delivery Partner logged in successfully"
        );
    }

    @Override
    public Map<String, Object> getDeliveryDashboard(Long partnerId) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        List<Order> orders = orderRepository.findByDeliveryPartnerPrioritized(partner);

        long activeCount = orders.stream()
                .filter(o -> Arrays.asList("PARTNER_ASSIGNED", "PICKED_UP", "OUT_FOR_DELIVERY").contains(o.getStatus().toUpperCase()))
                .count();

        long deliveredCount = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .count();

        long emergencyCount = orders.stream()
                .filter(o -> "EMERGENCY".equalsIgnoreCase(o.getPriority()))
                .count();

        BigDecimal estimatedEarnings = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .map(o -> o.getDeliveryEarnings() != null ? o.getDeliveryEarnings() : new BigDecimal("45.00"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new HashMap<>();
        map.put("partnerId", partner.getId());
        map.put("fullName", partner.getFullName());
        map.put("email", partner.getEmail());
        map.put("phoneNumber", partner.getPhoneNumber());
        map.put("vehicleNumber", partner.getVehicleNumber());
        map.put("drivingLicense", partner.getDrivingLicense());
        map.put("assignedCity", partner.getAssignedCity());
        map.put("currentArea", partner.getCurrentArea());
        map.put("available", partner.isAvailable());
        map.put("online", partner.isOnline());
        map.put("rating", partner.getRating());
        map.put("activeDeliveries", activeCount);
        map.put("deliveredCount", deliveredCount);
        map.put("emergencyCount", emergencyCount);
        map.put("totalTrips", orders.size());
        map.put("estimatedEarnings", estimatedEarnings);
        map.put("orders", orders.stream().map(this::mapOrderToDTO).collect(Collectors.toList()));

        return map;
    }

    @Override
    public List<OrderResponseDTO> getAssignedOrders(Long partnerId) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        return orderRepository.findByDeliveryPartnerPrioritized(partner).stream()
                .map(this::mapOrderToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getPendingDeliveryRequests(Long partnerId) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        if (!partner.isOnline()) {
            return Collections.emptyList(); // Offline partners receive no new delivery requests
        }

        // Only orders that are READY_FOR_PICKUP and unassigned, or already assigned to this partner
        String partnerCity = partner.getAssignedCity() != null ? partner.getAssignedCity().trim() : "";
        List<Order> allOrders = orderRepository.findAll();
        List<Order> availableOrders = allOrders.stream()
                .filter(o -> {
                    boolean isAssignedToThisPartner = (o.getDeliveryPartner() != null && o.getDeliveryPartner().getId().equals(partnerId)
                            && Arrays.asList("PARTNER_ASSIGNED", "ARRIVED_AT_PHARMACY", "PICKED_UP", "OUT_FOR_DELIVERY").contains(o.getStatus()));
                    boolean isUnassignedReady = (o.getDeliveryPartner() == null && Arrays.asList("ORDER_PLACED", "PREPARING", "READY_FOR_PICKUP").contains(o.getStatus() != null ? o.getStatus().toUpperCase() : ""));
                    return isAssignedToThisPartner || isUnassignedReady;
                })
                .filter(o -> partnerCity.isEmpty() || (o.getPharmacy() != null && (o.getPharmacy().getCity() == null || partnerCity.equalsIgnoreCase(o.getPharmacy().getCity().trim()))))
                .filter(o -> o.getDeliveredAt() == null && !"DELIVERED".equalsIgnoreCase(o.getStatus()) && !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .sorted(Comparator.comparing((Order o) -> "EMERGENCY".equalsIgnoreCase(o.getPriority()) ? 0 : 1)
                        .thenComparing(Order::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return availableOrders.stream().map(this::mapOrderToDTO).collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO acceptDelivery(Long orderId, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        // Concurrency protection: prevent two delivery partners from accepting the same order simultaneously
        if (order.getDeliveryPartner() != null && !order.getDeliveryPartner().getId().equals(partnerId)) {
            throw new BadRequestException("This delivery request has already been accepted by another partner.");
        }

        if (!Arrays.asList("ORDER_PLACED", "PREPARING", "READY_FOR_PICKUP", "PARTNER_ASSIGNED")
                .contains(order.getStatus() != null ? order.getStatus().toUpperCase() : "")) {
            throw new BadRequestException("Order cannot be accepted for delivery. Current status: " + order.getStatus());
        }

        order.setDeliveryPartner(partner);
        order.setStatus("PARTNER_ASSIGNED");

        partner.setAvailable(false); // Rider is now active on delivery
        deliveryPartnerRepository.save(partner);

        Order saved = orderRepository.save(order);
        return mapOrderToDTO(saved);
    }

    @Override
    public OrderResponseDTO rejectDelivery(Long orderId, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getDeliveryPartner() != null && order.getDeliveryPartner().getId().equals(partnerId)) {
            order.setDeliveryPartner(null);
            order.setStatus("READY_FOR_PICKUP"); // Keep available for other online riders to claim
        }

        Order saved = orderRepository.save(order);
        return mapOrderToDTO(saved);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatus, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (partnerId != null && order.getDeliveryPartner() != null && !order.getDeliveryPartner().getId().equals(partnerId)) {
            throw new BadRequestException("Order is assigned to a different delivery partner");
        }

        String validStatus = newStatus.trim().toUpperCase();
        order.setStatus(validStatus);

        if ("PICKED_UP".equalsIgnoreCase(validStatus)) {
            order.setPickedUpAt(LocalDateTime.now());
        } else if ("DELIVERED".equalsIgnoreCase(validStatus)) {
            order.setDeliveredAt(LocalDateTime.now());
            if (order.getDeliveryPartner() != null) {
                DeliveryPartner dp = order.getDeliveryPartner();
                dp.setAvailable(true); // Rider is now available again for next orders
                dp.setTotalDeliveries(dp.getTotalDeliveries() != null ? dp.getTotalDeliveries() + 1 : 1);
                deliveryPartnerRepository.save(dp);
            }
        }

        Order saved = orderRepository.save(order);
        return mapOrderToDTO(saved);
    }

    @Override
    public OrderResponseDTO recordPayment(Long orderId, Long partnerId, String paymentMode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        String method = (paymentMode != null && !paymentMode.trim().isEmpty()) ? paymentMode.trim().toUpperCase() : "COD";
        if ("CASH".equalsIgnoreCase(method) || "COD_CASH".equalsIgnoreCase(method)) {
            return confirmCashCollected(orderId, partnerId, order.getTotalAmount());
        }

        order.setPaymentMethod(method);
        order.setPaymentStatus("COD_COLLECTED");
        Order saved = orderRepository.save(order);
        settlementService.processCodCollection(saved, order.getDeliveryPartner());
        return mapOrderToDTO(saved);
    }

    @Override
    public Map<String, Object> generateOrderPaymentQr(Long orderId, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Check if already paid
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus()) || "COD_COLLECTED".equalsIgnoreCase(order.getPaymentStatus())) {
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("alreadyPaid", true);
            res.put("paymentStatus", "PAID");
            res.put("orderId", order.getId());
            res.put("orderNumber", order.getOrderNumber());
            res.put("amount", order.getTotalAmount());
            res.put("message", "This order is already marked as PAID.");
            return res;
        }

        BigDecimal amount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        String upiId = "srrivarshanmr@okaxis"; // Gateway collection merchant VPA
        String upiString = "upi://pay?pa=" + upiId + "&pn=PharmaTech%20Healthcare&am="
                + amount.setScale(2, java.math.RoundingMode.HALF_UP) + "&cu=INR&tr="
                + order.getOrderNumber() + "&tn=Delivery%20Collection%20" + order.getOrderNumber();

        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data="
                + java.net.URLEncoder.encode(upiString, StandardCharsets.UTF_8);

        order.setPaymentStatus("AWAITING_CUSTOMER_PAYMENT");
        order.setPaymentMethod("COD_QR");
        orderRepository.save(order);

        // Ensure PaymentTransaction record exists
        String txnRef = "QR-COL-" + order.getOrderNumber();
        com.pharmatech.model.PaymentTransaction txn = paymentTransactionRepository.findByRazorpayOrderId(txnRef).orElse(null);
        if (txn == null) {
            txn = new com.pharmatech.model.PaymentTransaction(order, "COD_QR", txnRef, amount, "AWAITING_PAYMENT");
            txn.setCustomer(order.getCustomer());
            txn.setPharmacy(order.getPharmacy());
            txn.setPaymentMethod("COD_QR");
            paymentTransactionRepository.save(txn);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("orderId", order.getId());
        res.put("orderNumber", order.getOrderNumber());
        res.put("amount", amount);
        res.put("qrString", upiString);
        res.put("qrImageUrl", qrImageUrl);
        res.put("paymentStatus", "AWAITING_CUSTOMER_PAYMENT");
        res.put("message", "Unique single-use payment QR generated for Order #" + order.getOrderNumber());
        return res;
    }

    @Override
    public Map<String, Object> checkOrderQrPaymentStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Map<String, Object> res = new HashMap<>();
        res.put("orderId", order.getId());
        res.put("orderNumber", order.getOrderNumber());
        res.put("amount", order.getTotalAmount());

        boolean isPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus()) || "COD_COLLECTED".equalsIgnoreCase(order.getPaymentStatus());
        if (isPaid) {
            res.put("paid", true);
            res.put("paymentStatus", "PAID");
            res.put("transactionReference", order.getRazorpayPaymentId() != null ? order.getRazorpayPaymentId() : "pay_qr_" + order.getOrderNumber());
            res.put("message", "Payment Received and verified.");
        } else {
            res.put("paid", false);
            res.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus() : "AWAITING_CUSTOMER_PAYMENT");
            res.put("message", "Waiting for customer payment...");
        }
        return res;
    }

    @Override
    public OrderResponseDTO confirmCashCollected(Long orderId, Long partnerId, BigDecimal amount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        DeliveryPartner partner = null;
        if (partnerId != null) {
            partner = deliveryPartnerRepository.findById(partnerId).orElse(order.getDeliveryPartner());
        } else {
            partner = order.getDeliveryPartner();
        }

        BigDecimal effectiveAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) ? amount : order.getTotalAmount();

        // Prevent duplicate cash remittance records
        com.pharmatech.model.CashRemittance remittance = cashRemittanceRepository.findByOrder(order).orElse(null);
        if (remittance == null && partner != null) {
            remittance = new com.pharmatech.model.CashRemittance(
                    partner, order, effectiveAmount,
                    "Physical cash collected on doorstep delivery by " + partner.getFullName()
            );
            cashRemittanceRepository.save(remittance);
        }

        order.setPaymentMethod("COD_CASH");
        order.setPaymentStatus("PAID");
        Order saved = orderRepository.save(order);

        // Record physical cash liability (cash held by partner, not admin bank)
        settlementService.processCodCollection(saved, partner);

        return mapOrderToDTO(saved);
    }

    @Override
    public OrderResponseDTO completeDelivery(Long orderId, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Require payment to be settled or collected first
        boolean isPaid = "PAID".equalsIgnoreCase(order.getPaymentStatus()) || "COD_COLLECTED".equalsIgnoreCase(order.getPaymentStatus());
        if (!isPaid) {
            throw new BadRequestException("Cannot complete delivery: Payment is pending collection. Please collect cash or verify payment QR first.");
        }

        order.setStatus("DELIVERED");
        order.setDeliveredAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        if (partnerId != null) {
            DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId).orElse(null);
            if (partner != null) {
                partner.setTotalDeliveries((partner.getTotalDeliveries() != null ? partner.getTotalDeliveries() : 0) + 1);
                deliveryPartnerRepository.save(partner);
            }
        }

        return mapOrderToDTO(saved);
    }

    @Override
    public Map<String, Object> getCashRemittanceSummary(Long partnerId) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        BigDecimal outstanding = cashRemittanceRepository.sumAmountByDeliveryPartnerAndStatus(partner, "PENDING_REMITTANCE");
        if (outstanding == null) outstanding = BigDecimal.ZERO;

        List<com.pharmatech.model.CashRemittance> remittances = cashRemittanceRepository.findByDeliveryPartnerOrderByCollectedAtDesc(partner);
        List<Map<String, Object>> remittanceList = new ArrayList<>();
        for (com.pharmatech.model.CashRemittance r : remittances) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("orderId", r.getOrder() != null ? r.getOrder().getId() : null);
            map.put("orderNumber", r.getOrder() != null ? r.getOrder().getOrderNumber() : null);
            map.put("amountCollected", r.getAmountCollected());
            map.put("collectedAt", r.getCollectedAt());
            map.put("remittanceStatus", r.getRemittanceStatus());
            map.put("remittedAt", r.getRemittedAt());
            map.put("notes", r.getNotes());
            remittanceList.add(map);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("partnerId", partner.getId());
        res.put("partnerName", partner.getFullName());
        res.put("outstandingRemittance", outstanding);
        res.put("totalCollectionsCount", remittances.size());
        res.put("remittances", remittanceList);
        return res;
    }

    @Override
    public DeliveryPartner toggleOnlineStatus(Long partnerId, boolean online) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        partner.setOnline(online);
        return deliveryPartnerRepository.save(partner);
    }

    @Override
    public Map<String, Object> getEarningsSummary(Long partnerId) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        List<Order> orders = orderRepository.findByDeliveryPartnerOrderByCreatedAtDesc(partner);
        List<Order> deliveredOrders = orders.stream()
                .filter(o -> "DELIVERED".equalsIgnoreCase(o.getStatus()))
                .collect(Collectors.toList());

        BigDecimal total = deliveredOrders.stream()
                .map(o -> o.getDeliveryEarnings() != null ? o.getDeliveryEarnings() : new BigDecimal("45.00"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new HashMap<>();
        map.put("totalDeliveries", deliveredOrders.size());
        map.put("totalEarnings", total);
        map.put("basePayoutPerOrder", 40.00);
        map.put("emergencyBonus", 30.00);
        map.put("history", deliveredOrders.stream().map(this::mapOrderToDTO).collect(Collectors.toList()));
        return map;
    }

    private OrderResponseDTO mapOrderToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setCustomerLatitude(order.getCustomerLatitude());
        dto.setCustomerLongitude(order.getCustomerLongitude());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPriority(order.getPriority());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMode(order.getPaymentMode() != null ? order.getPaymentMode() : "DEMO");
        dto.setDemo("DEMO".equalsIgnoreCase(order.getPaymentMode()));
        dto.setDeliveryDistanceKm(order.getDeliveryDistanceKm());
        dto.setEstimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes());
        dto.setDeliveryEarnings(order.getDeliveryEarnings());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setReadyAt(order.getReadyAt());
        dto.setPickedUpAt(order.getPickedUpAt());
        dto.setDeliveredAt(order.getDeliveredAt());

        dto.setPaymentProofData(order.getPaymentProofData());
        dto.setPaymentProofFileName(order.getPaymentProofFileName());
        dto.setPaymentProofUploadedAt(order.getPaymentProofUploadedAt());
        dto.setPaymentProofExpiresAt(order.getPaymentProofExpiresAt());
        dto.setPaymentUtr(order.getPaymentUtr());
        dto.setPaymentApprovalStatus(order.getPaymentApprovalStatus() != null ? order.getPaymentApprovalStatus() : "NONE");
        dto.setPaymentApprovedAt(order.getPaymentApprovedAt());
        dto.setPaymentRejectReason(order.getPaymentRejectReason());

        if (order.getPharmacy() != null) {
            dto.setPharmacyId(order.getPharmacy().getId());
            dto.setPharmacyName(order.getPharmacy().getName());
            dto.setPharmacyAddress(order.getPharmacy().getAddress() + ", " + order.getPharmacy().getCity());
            dto.setPharmacyPhone(order.getPharmacy().getPhoneNumber());
            dto.setPharmacyLatitude(order.getPharmacy().getLatitude());
            dto.setPharmacyLongitude(order.getPharmacy().getLongitude());
            dto.setMerchantUpiId(order.getPharmacy().getMerchantUpiId());
        }

        if (order.getDeliveryPartner() != null) {
            dto.setDeliveryPartnerId(order.getDeliveryPartner().getId());
            dto.setDeliveryPartnerName(order.getDeliveryPartner().getFullName());
            dto.setDeliveryPartnerPhone(order.getDeliveryPartner().getPhoneNumber());
            dto.setVehicleNumber(order.getDeliveryPartner().getVehicleNumber());
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                    .map(it -> new OrderResponseDTO.OrderItemDetailDTO(
                            it.getMedicineName(),
                            it.getQuantity(),
                            it.getUnitPrice(),
                            it.getTotalPrice()
                    ))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public Map<String, Object> recordLocationUpdate(Long partnerId, Long orderId, Double latitude, Double longitude, Double heading, Double speed) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Partner not found with id: " + partnerId));

        partner.setLatitude(latitude);
        partner.setLongitude(longitude);
        deliveryPartnerRepository.save(partner);

        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId).orElse(null);
        }

        com.pharmatech.model.RiderLocationUpdate update = new com.pharmatech.model.RiderLocationUpdate(
                partner, order, latitude, longitude, heading, speed
        );
        riderLocationUpdateRepository.save(update);

        Map<String, Object> map = new HashMap<>();
        map.put("partnerId", partner.getId());
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("heading", heading);
        map.put("speed", speed);
        map.put("recordedAt", update.getRecordedAt());
        return map;
    }

    private boolean validatePassword(String plainPassword, String hashedPassword) {
        String hashedInput = hashPassword(plainPassword);
        return hashedInput.equals(hashedPassword);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
