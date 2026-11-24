/**
 * PharmaTech Unified API Client
 * Centralized fetch wrapper handling requests, responses, session persistence,
 * location management, nearby 10km medicine search, order lifecycle, and delivery dispatch.
 */
const API = {
  BASE_URL: '', // Relative path to support both localhost and deployed servers

  // Session & Local Storage Keys
  STORAGE_KEY_USER: 'pharmatech_user',
  STORAGE_KEY_CART: 'pharmatech_cart',
  STORAGE_KEY_LOCATION: 'pharmatech_customer_location',

  // Generic Request Handler
  async request(endpoint, options = {}) {
    const defaultHeaders = {
      'Accept': 'application/json'
    };

    if (!(options.body instanceof FormData)) {
      defaultHeaders['Content-Type'] = 'application/json';
    }

    const token = this.getToken();
    if (token) {
      defaultHeaders['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...(options.headers || {})
      }
    };

    try {
      const response = await fetch(`${this.BASE_URL}${endpoint}`, config);
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        let errorMsg = 'An error occurred. Please try again.';
        if (data && data.message) {
          errorMsg = data.message;
        } else if (data && data.errors) {
          errorMsg = Object.values(data.errors).join(', ');
        }
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error on [${endpoint}]:`, err);
      throw err;
    }
  },

  // Helper to normalize login payload
  _formatLoginPayload(payload) {
    if (!payload) return {};
    const identifier = payload.identifier || payload.email || payload.phoneNumber || '';
    return {
      identifier: identifier.trim(),
      password: payload.password || ''
    };
  },

  // Auth Endpoints
  async registerCustomer(payload) {
    return this.request('/api/auth/customer/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async loginCustomer(payload) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(this._formatLoginPayload(payload))
    });
  },

  async registerPharmacy(payload) {
    return this.request('/api/auth/pharmacy/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async loginPharmacy(payload) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(this._formatLoginPayload(payload))
    });
  },

  async loginDelivery(payload) {
    return this.request('/api/delivery/login', {
      method: 'POST',
      body: JSON.stringify(this._formatLoginPayload(payload))
    });
  },

  async loginAdmin(payload) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(this._formatLoginPayload(payload))
    });
  },

  // Customer Location & 10km Nearby Medicine Search
  async getSuggestedLocations() {
    return this.request('/api/customer/locations/suggest');
  },

  async getLocationSuggestions(query = '') {
    return this.getSuggestedLocations();
  },

  getUserLocation() {
    return this.getLocation();
  },

  saveUserLocation(loc) {
    return this.saveLocation(loc);
  },

  async searchNearbyPharmacies(medicine = '', lat = null, lng = null, radius = 10.0) {
    let q = `/api/customer/search-nearby?radius=${radius}`;
    if (medicine) q += `&medicine=${encodeURIComponent(medicine)}`;
    if (lat !== null && lat !== undefined) q += `&lat=${lat}`;
    if (lng !== null && lng !== undefined) q += `&lng=${lng}`;
    return this.request(q);
  },

  async updateCustomerLocation(customerId, payload) {
    return this.request(`/api/customer/location/${customerId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  // Customer Catalog & Cart
  async getProducts(query = '', category = '', city = '') {
    let q = `/api/customer/products?`;
    if (query) q += `query=${encodeURIComponent(query)}&`;
    if (category && category !== 'All') q += `category=${encodeURIComponent(category)}&`;
    if (city) q += `city=${encodeURIComponent(city)}`;
    return this.request(q);
  },

  async getCategories() {
    return this.request('/api/customer/categories');
  },

  async getNearbyPharmacies(city = '', pincode = '') {
    let q = `/api/customer/nearby-pharmacies?`;
    if (city) q += `city=${encodeURIComponent(city)}&`;
    if (pincode) q += `pincode=${encodeURIComponent(pincode)}`;
    return this.request(q);
  },

  // Customer Medicine Ordering & Tracking
  async createCustomerOrder(payload) {
    return this.request('/api/customer/orders/create', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async trackOrder(orderId) {
    return this.request(`/api/customer/orders/track/${orderId}`);
  },

  async trackOrderByNumber(orderNumber) {
    return this.request(`/api/customer/orders/track-number/${encodeURIComponent(orderNumber)}`);
  },

  async getCustomerOrders(customerId) {
    return this.request(`/api/customer/orders/customer/${customerId}`);
  },

  // Pharmacy Owner Dashboard & Inventory Endpoints
  async getDashboardStats(ownerId) {
    return this.request(`/api/pharmacy/dashboard/${ownerId}`);
  },

  async getPharmacyInventory(pharmacyId) {
    return this.request(`/api/inventory/pharmacy/${pharmacyId}`);
  },

  async searchPharmacyInventory(pharmacyId, query) {
    return this.request(`/api/inventory/pharmacy/${pharmacyId}/search?query=${encodeURIComponent(query)}`);
  },

  async addMedicine(pharmacyId, payload) {
    return this.request(`/api/inventory/pharmacy/${pharmacyId}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async updateMedicine(inventoryId, payload) {
    return this.request(`/api/inventory/${inventoryId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  async deleteMedicine(inventoryId) {
    return this.request(`/api/inventory/${inventoryId}`, {
      method: 'DELETE'
    });
  },

  async getMasterCatalog() {
    return this.request('/api/medicines/catalog');
  },

  // Pharmacy Orders & Notifications
  async getPharmacyOrders(pharmacyId) {
    return this.request(`/api/pharmacy/orders/${pharmacyId}`);
  },

  async updatePharmacyOrderStatus(orderId, param2, param3) {
    let pharmacyId = null;
    let status = null;
    if (param3 !== undefined) {
      pharmacyId = param2;
      status = param3;
    } else {
      status = param2;
    }
    return this.request(`/api/pharmacy/orders/${orderId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ pharmacyId, status })
    });
  },

  async getPharmacyNotifications(pharmacyId) {
    return this.request(`/api/pharmacy/notifications/${pharmacyId}`);
  },

  async getPharmacyProfile(ownerId) {
    return this.request(`/api/pharmacy/profile/${ownerId}`);
  },

  async activateSubscription(param) {
    const payload = typeof param === 'object' ? param : { ownerId: param };
    return this.request('/api/pharmacy/subscription/activate', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  // Purchase Invoice Upload & Automatic Stock Update (Two-Stage)
  async extractInvoiceFile(pharmacyId, formData) {
    return this.request(`/api/pharmacy/invoice/extract/${pharmacyId}`, {
      method: 'POST',
      body: formData
    });
  },

  async extractInvoiceRaw(pharmacyId, rawText, fileName) {
    return this.request(`/api/pharmacy/invoice/extract-raw/${pharmacyId}`, {
      method: 'POST',
      body: JSON.stringify({ rawText, fileName })
    });
  },

  async confirmInvoice(pharmacyId, payload) {
    return this.request(`/api/pharmacy/invoice/confirm/${pharmacyId}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async previewInvoiceReversal(pharmacyId, invoiceNumber) {
    return this.request(`/api/pharmacy/invoice/revert-preview/${pharmacyId}/${encodeURIComponent(invoiceNumber)}`);
  },

  async confirmInvoiceReversal(pharmacyId, invoiceNumber) {
    return this.request(`/api/pharmacy/invoice/revert-confirm/${pharmacyId}/${encodeURIComponent(invoiceNumber)}`, {
      method: 'POST'
    });
  },

  async getInvoiceHistory(pharmacyId) {
    return this.request(`/api/pharmacy/invoice/history/${pharmacyId}`);
  },

  async getSampleInvoiceTemplate() {
    return this.request('/api/pharmacy/invoice/sample-template');
  },

  // Counter Billing & POS Endpoints (Stock Reduced on Save)
  async createBill(payload) {
    return this.request('/api/pharmacy/billing/create', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async getBillDetails(billId) {
    return this.request(`/api/pharmacy/billing/${billId}`);
  },

  async getBillByNumber(billNumber) {
    return this.request(`/api/pharmacy/billing/by-number/${encodeURIComponent(billNumber)}`);
  },

  async getBillingHistory(pharmacyId) {
    return this.request(`/api/pharmacy/billing/history/${pharmacyId}`);
  },

  async cancelBill(billId) {
    return this.request(`/api/pharmacy/billing/${billId}/cancel`, {
      method: 'POST'
    });
  },

  // Delivery Partner Endpoints
  async getDeliveryDashboard(partnerId) {
    return this.request(`/api/delivery/dashboard/${partnerId}`);
  },

  async getAssignedOrders(partnerId) {
    return this.request(`/api/delivery/orders/${partnerId}`);
  },

  async getPendingDeliveryRequests(partnerId) {
    return this.request(`/api/delivery/pending-requests/${partnerId}`);
  },

  async acceptDelivery(orderId, partnerId) {
    return this.request(`/api/delivery/orders/${orderId}/accept`, {
      method: 'POST',
      body: JSON.stringify({ partnerId })
    });
  },

  async acceptDeliveryOrder(orderId, partnerId) {
    return this.acceptDelivery(orderId, partnerId);
  },

  async rejectDelivery(orderId, partnerId) {
    return this.request(`/api/delivery/orders/${orderId}/reject`, {
      method: 'POST',
      body: JSON.stringify({ partnerId })
    });
  },

  async rejectDeliveryOrder(orderId, partnerId) {
    return this.rejectDelivery(orderId, partnerId);
  },

  async updateDeliveryStatus(orderId, status, partnerId) {
    return this.request(`/api/delivery/orders/${orderId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status, partnerId })
    });
  },

  async toggleDeliveryOnline(partnerId, online) {
    return this.request(`/api/delivery/${partnerId}/online`, {
      method: 'PUT',
      body: JSON.stringify({ online })
    });
  },

  async recordDeliveryPayment(orderId, param2, param3) {
    let partnerId = null;
    let paymentMode = 'CASH';
    if (param3 !== undefined) {
      partnerId = param2;
      paymentMode = param3;
    } else if (typeof param2 === 'string') {
      paymentMode = param2;
    } else if (typeof param2 === 'number') {
      partnerId = param2;
    }
    return this.request(`/api/delivery/orders/${orderId}/record-payment`, {
      method: 'POST',
      body: JSON.stringify({ partnerId, paymentMode })
    });
  },

  async generateDeliveryQr(orderId, partnerId) {
    return this.request(`/api/delivery/orders/${orderId}/generate-qr`, {
      method: 'POST',
      body: JSON.stringify({ partnerId })
    });
  },

  async checkDeliveryQrStatus(orderId) {
    return this.request(`/api/delivery/orders/${orderId}/check-qr-status`);
  },

  async confirmCashCollected(orderId, partnerId, amount) {
    return this.request(`/api/delivery/orders/${orderId}/confirm-cash`, {
      method: 'POST',
      body: JSON.stringify({ partnerId, amount })
    });
  },

  async completeDeliveryOrder(orderId, partnerId) {
    return this.request(`/api/delivery/orders/${orderId}/complete-delivery`, {
      method: 'POST',
      body: JSON.stringify({ partnerId })
    });
  },

  async getCashRemittanceSummary(partnerId) {
    return this.request(`/api/delivery/remittance/${partnerId}`);
  },

  async getDeliveryEarnings(partnerId) {
    return this.request(`/api/delivery/earnings/${partnerId}`);
  },

  async updateRiderLocation(partnerId, orderId, latitude, longitude, heading = 0, speed = 0) {
    return this.request('/api/delivery/location-update', {
      method: 'POST',
      body: JSON.stringify({ partnerId, orderId, latitude, longitude, heading, speed })
    });
  },

  // Payment Gateway Endpoints (Razorpay UPI & Cards)
  async getPaymentConfig() {
    return this.request('/api/customer/payment/config');
  },

  async getMapsConfig() {
    return this.request('/api/customer/maps/config');
  },

  async createRazorpayOrder(payload) {
    return this.request('/api/customer/payment/create-order', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async verifyRazorpayPayment(payload) {
    return this.request('/api/customer/payment/verify', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  // Razorpay UPI QR Code Methods
  async generateUpiQrCode(payload) {
    return this.request('/api/customer/payment/generate-upi-qr', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async checkPaymentStatus(orderId) {
    return this.request(`/api/customer/payment/check-status/${orderId}`);
  },

  async confirmUpiPayment(payload) {
    return this.request('/api/customer/payment/confirm-upi-payment', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async simulateUpiPaymentSuccess(orderId) {
    return this.request(`/api/customer/payment/simulate-success/${orderId}`, {
      method: 'POST'
    });
  },

  async simulateDemoPayment(orderId) {
    return this.request(`/api/customer/payment/demo/simulate-payment/${orderId}`, {
      method: 'POST'
    });
  },

  // Customer Order Operations
  async createCustomerOrder(payload) {
    return this.request('/api/customer/orders/create', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  async trackOrder(orderId) {
    return this.request(`/api/customer/orders/track/${orderId}`);
  },

  async trackOrderByNumber(orderNumber) {
    return this.request(`/api/customer/orders/track-by-number/${encodeURIComponent(orderNumber)}`);
  },

  // Admin Endpoints
  async getAdminStats() {
    return this.request('/api/admin/stats');
  },

  async getAdminPharmacies() {
    return this.request('/api/admin/pharmacies');
  },

  async getAdminFleet() {
    return this.request('/api/admin/fleet');
  },

  async getAdminCommissionSetting() {
    return this.request('/api/admin/commission-setting');
  },

  async updateAdminCommissionSetting(payload) {
    return this.request('/api/admin/commission-setting', {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
  },

  async autoSettleAllPayments() {
    return this.request('/api/admin/payments/auto-settle-all', {
      method: 'POST'
    });
  },

  async getAdminPayments(params = {}) {
    const q = new URLSearchParams();
    if (params.status) q.append('status', params.status);
    if (params.pharmacyId) q.append('pharmacyId', params.pharmacyId);
    if (params.date) q.append('date', params.date);
    if (params.search) q.append('search', params.search);
    const qs = q.toString();
    return this.request(`/api/admin/payments${qs ? '?' + qs : ''}`);
  },

  async getAdminPaymentSummary() {
    return this.request('/api/admin/payment-summary');
  },

  async getPharmacyEarnings(pharmacyId) {
    return this.request(`/api/pharmacy/earnings/${pharmacyId}`);
  },

  async processTestPayment(payload) {
    return this.request('/api/customer/payment/process-test-payment', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  },

  // UPI Screenshot Proof & Verification Endpoints
  async uploadPaymentProof(orderId, fileOrBase64, fileName, utrNumber) {
    if (fileOrBase64 instanceof FormData) {
      return this.request('/api/customer/payment/upload-proof', {
        method: 'POST',
        body: fileOrBase64
      });
    } else if (fileOrBase64 instanceof File) {
      const fd = new FormData();
      fd.append('orderId', orderId);
      fd.append('file', fileOrBase64);
      if (utrNumber) fd.append('utrNumber', utrNumber);
      return this.request('/api/customer/payment/upload-proof', {
        method: 'POST',
        body: fd
      });
    } else {
      return this.request('/api/customer/payment/upload-proof', {
        method: 'POST',
        body: JSON.stringify({
          orderId: orderId,
          screenshotBase64: fileOrBase64,
          fileName: fileName || 'screenshot.png',
          utrNumber: utrNumber
        })
      });
    }
  },

  async getPaymentVerificationStatus(orderId) {
    return this.request(`/api/customer/payment/verification-status/${orderId}`);
  },

  async retryUpiPayment(orderId) {
    return this.request(`/api/customer/payment/retry-upi/${orderId}`, {
      method: 'POST'
    });
  },

  async getPendingUpiVerifications() {
    return this.request('/api/admin/payments/pending-verifications');
  },

  async approveUpiPayment(orderId) {
    return this.request(`/api/admin/payments/${orderId}/approve`, {
      method: 'POST'
    });
  },

  async rejectUpiPayment(orderId, reason) {
    return this.request(`/api/admin/payments/${orderId}/reject`, {
      method: 'POST',
      body: JSON.stringify({ reason: reason || 'Transaction could not be verified against bank statement' })
    });
  },

  // Session & Local Storage Management
  saveUserSession(userData) {
    if (!userData) return;
    if (userData.userId && !userData.id) userData.id = userData.userId;
    if (userData.id && !userData.userId) userData.userId = userData.id;
    localStorage.setItem(this.STORAGE_KEY_USER, JSON.stringify(userData));
    localStorage.setItem('pharmatech_customer', JSON.stringify(userData));
  },

  getUserSession() {
    const raw = localStorage.getItem(this.STORAGE_KEY_USER) || localStorage.getItem('pharmatech_customer');
    if (!raw) return null;
    try {
      const u = JSON.parse(raw);
      if (u && u.userId && !u.id) u.id = u.userId;
      if (u && u.id && !u.userId) u.userId = u.id;
      return u;
    } catch (e) {
      return null;
    }
  },

  getToken() {
    const user = this.getUserSession();
    return user ? user.token : null;
  },

  clearSession() {
    localStorage.removeItem(this.STORAGE_KEY_USER);
    localStorage.removeItem('pharmatech_customer');
  },

  // Location Management
  saveLocation(loc) {
    localStorage.setItem(this.STORAGE_KEY_LOCATION, JSON.stringify(loc));
  },

  getLocation() {
    const raw = localStorage.getItem(this.STORAGE_KEY_LOCATION);
    return raw ? JSON.parse(raw) : {
      label: 'Indiranagar, Bangalore',
      locality: 'Indiranagar',
      city: 'Bangalore',
      pincode: '560038',
      latitude: 12.9784,
      longitude: 77.6408
    };
  },

  // Cart Management
  getCart() {
    const raw = localStorage.getItem(this.STORAGE_KEY_CART);
    return raw ? JSON.parse(raw) : [];
  },

  saveCart(cart) {
    localStorage.setItem(this.STORAGE_KEY_CART, JSON.stringify(cart));
  },

  addToCart(item) {
    const cart = this.getCart();
    const existingIndex = cart.findIndex(c => c.inventoryId === item.inventoryId);
    if (existingIndex > -1) {
      cart[existingIndex].quantity += 1;
    } else {
      cart.push({
        inventoryId: item.inventoryId,
        medicineId: item.medicineId,
        pharmacyId: item.pharmacyId,
        pharmacyName: item.pharmacyName || 'Partner Pharmacy',
        name: item.medicineName,
        brand: item.brand,
        strength: item.strength,
        price: Number(item.price),
        quantity: 1
      });
    }
    this.saveCart(cart);
    return cart;
  },

  updateCartQty(inventoryId, delta) {
    let cart = this.getCart();
    const idx = cart.findIndex(c => c.inventoryId === inventoryId);
    if (idx > -1) {
      cart[idx].quantity += delta;
      if (cart[idx].quantity <= 0) {
        cart.splice(idx, 1);
      }
    }
    this.saveCart(cart);
    return cart;
  },

  clearCart() {
    localStorage.removeItem(this.STORAGE_KEY_CART);
  }
};
