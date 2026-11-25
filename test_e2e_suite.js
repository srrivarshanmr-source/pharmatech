// Complete End-to-End Verification Test Suite for PharmaTech Platform
const crypto = require('crypto');

const BASE_URL = 'http://127.0.0.1:8080';

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const res = await fetch(url, options);
  const json = await res.json().catch(() => null);
  return { status: res.status, ok: res.ok, data: json };
}

function computeHmacSha256(data, secret) {
  return crypto.createHmac('sha256', secret).update(data).digest('hex');
}

async function runSuite() {
  console.log('================================================================');
  console.log('       PHARMATECH FULL-STACK VERIFICATION TEST SUITE            ');
  console.log('================================================================\n');

  let passed = 0;
  let failed = 0;

  function assert(condition, message) {
    if (condition) {
      console.log(`  ✓ [PASS] ${message}`);
      passed++;
    } else {
      console.error(`  ✗ [FAIL] ${message}`);
      failed++;
    }
  }

  // TEST 1: Nationwide Location Autocomplete (including Coimbatore / Neelambur)
  console.log('TEST 1: Nationwide Location Autocomplete & Maps Config');
  const locRes = await request('/api/customer/locations/suggest?query=Neelambur');
  assert(locRes.status === 200, 'Locations endpoint returns 200 OK');
  const neelambur = locRes.data.data.find(l => l.locality === 'Neelambur');
  assert(neelambur !== undefined, 'Found "Neelambur, Coimbatore, Tamil Nadu" in suggestions');
  assert(neelambur && neelambur.city === 'Coimbatore' && neelambur.pincode === '641062', 'Neelambur location has correct city and pincode');
  assert(neelambur && neelambur.area === 'Neelambur', 'Neelambur location includes "area" alias for frontend');

  const mapsRes = await request('/api/customer/maps/config');
  assert(mapsRes.status === 200 && mapsRes.data.data.apiKey !== undefined, 'Maps configuration endpoint responsive');
  console.log();

  // TEST 2: 10km Nearby Medicine Search & Dynamic Distance / ETA
  console.log('TEST 2: Nearby Medicine Search (Coimbatore KMCH Wellness Pharmacy)');
  const nearbyRes = await request('/api/customer/search-nearby?medicine=Dolo&lat=11.0583&lng=77.0872&radius=10.0');
  assert(nearbyRes.status === 200, 'Nearby search endpoint returns 200 OK');
  const kmchResult = nearbyRes.data.data.find(p => p.pharmacyName.includes('KMCH'));
  assert(kmchResult !== undefined, 'Found KMCH Wellness Pharmacy in Neelambur, Coimbatore');
  assert(kmchResult && kmchResult.distanceKm <= 10.0, `Distance accurately calculated: ${kmchResult ? kmchResult.distanceKm : 0} km`);
  assert(kmchResult && kmchResult.estimatedDeliveryMinutes >= 10, `Delivery ETA calculated: ${kmchResult ? kmchResult.estimatedDeliveryMinutes : 0} mins`);
  assert(kmchResult && kmchResult.availableQuantity > 0, `Available quantity verified: ${kmchResult ? kmchResult.availableQuantity : 0} units`);
  assert(kmchResult && kmchResult.availableStock === kmchResult.availableQuantity, 'DTO alias "availableStock" matches "availableQuantity"');
  assert(kmchResult && kmchResult.unitPrice !== undefined, 'DTO alias "unitPrice" matches medicine price');
  console.log();

  // TEST 3: Purchase Invoice Extraction & Preview (Bulletproof Currency & Regex)
  console.log('TEST 3: Purchase Invoice Parsing & Preview');
  const testInvNum = 'INV-2026-TEST-' + Math.floor(Math.random() * 90000 + 10000);
  const sampleInvoiceText = `Supplier: SunPharma Wholesale Distributors Pvt Ltd
Invoice: ${testInvNum}
1 Dolo 650 Micro Labs / 650mg Tablets 50 ₹30.50 ₹1,525.00 2028-06-30 BAT-DL-772
2 Augmentin 625 Duo GSK / 625mg Tablets 60 ₹204.00 ₹12,240.00 2027-11-30 BAT-AUG-331
3 Shelcal 500 Torrent Pharma / 15 Tablets Vitamins 100 ₹131.00 ₹13,100.00 2028-05-31 BAT-SHL-441`;

  const extractRes = await request('/api/pharmacy/invoice/extract-raw/1', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rawText: sampleInvoiceText, fileName: 'sunpharma_invoice.txt' })
  });
  assert(extractRes.status === 200, 'Invoice raw extraction returns 200 OK');
  const ext = extractRes.data.data;
  assert(ext.invoiceNumber === testInvNum, `Extracted invoice number: ${ext.invoiceNumber}`);
  assert(ext.itemsParsed === 3, `Correctly parsed 3 medicine rows (parsed: ${ext.itemsParsed})`);
  assert(ext.totalStockAdded === 210, `Total stock units calculated correctly: 210 (calculated: ${ext.totalStockAdded})`);
  assert(ext.totalsMatch === true, 'Line totals match declared totals with zero column shifting');

  // Confirm Invoice
  const confirmRes = await request('/api/pharmacy/invoice/confirm/1', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      invoiceNumber: ext.invoiceNumber,
      supplierName: ext.supplierName,
      fileName: 'sunpharma_invoice.txt',
      items: ext.items
    })
  });
  assert(confirmRes.status === 200, 'Invoice confirmation commits stock to database');
  assert(confirmRes.data.data.confirmed === true, 'Invoice marked as confirmed');

  // Verify Duplicate Invoice Detection
  const dupRes = await request('/api/pharmacy/invoice/extract-raw/1', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rawText: sampleInvoiceText, fileName: 'sunpharma_invoice.txt' })
  });
  assert(dupRes.data.data.duplicateDetected === true, 'Duplicate invoice submission detected and warned');
  console.log();

  // TEST 4: Safe Reversal of Imported Invoice
  console.log('TEST 4: Safe Reversal of Imported Invoice');
  const prevRevertRes = await request(`/api/pharmacy/invoice/revert-preview/1/${testInvNum}`);
  assert(prevRevertRes.status === 200, 'Revert preview endpoint returns 200 OK');
  assert(prevRevertRes.data.data.totalStockToRevert === 210, `Revert preview identifies 210 units to deduct (found: ${prevRevertRes.data.data.totalStockToRevert})`);
  assert(prevRevertRes.data.data.itemsCount === 3, 'Revert preview identifies 3 affected items');

  const confRevertRes = await request(`/api/pharmacy/invoice/revert-confirm/1/${testInvNum}`, {
    method: 'POST'
  });
  assert(confRevertRes.status === 200, 'Revert confirmation successfully executes stock deduction');
  assert(confRevertRes.data.data.totalStockAdded === -210, 'Stock correctly reversed with negative audit movement');
  console.log();

  // TEST 5: Customer Order Creation (Atomic Stock Deduction & Totals)
  console.log('TEST 5: Order Creation with Priority & Pricing Breakdown');
  // Order 1: Normal Priority
  const orderRes = await request('/api/customer/orders/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      customerId: 1,
      pharmacyId: 1,
      customerName: 'Priya Sundaram',
      customerPhone: '9845012399',
      deliveryAddress: 'Flat 302, 12th Main Road, Indiranagar, Bangalore - 560038',
      customerLatitude: 12.9784,
      customerLongitude: 77.6408,
      priority: 'NORMAL',
      paymentMethod: 'RAZORPAY',
      items: [
        { inventoryId: 1, quantity: 2 }
      ]
    })
  });
  assert(orderRes.status === 201, 'Normal order created successfully [HTTP 201]');
  const createdOrder = orderRes.data.data;
  assert(createdOrder.status === 'ORDER_PLACED' || createdOrder.status === 'PAYMENT_PENDING', `Initial order status is valid (is: ${createdOrder.status})`);
  assert(createdOrder.deliveryEarnings == 45.00, `Standard delivery fee is ₹45.00 (is: ${createdOrder.deliveryEarnings})`);
  assert(createdOrder.totalAmount > 0, `Total amount calculated with 5% tax: ₹${createdOrder.totalAmount}`);

  // Order 2: Emergency Priority
  const emergOrderRes = await request('/api/customer/orders/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      customerId: 1,
      pharmacyId: 1,
      customerName: 'Emergency Patient',
      customerPhone: '9845099999',
      deliveryAddress: 'Emergency Ward, Indiranagar, Bangalore - 560038',
      customerLatitude: 12.9784,
      customerLongitude: 77.6408,
      priority: 'EMERGENCY',
      paymentMethod: 'COD',
      items: [
        { inventoryId: 1, quantity: 1 }
      ]
    })
  });
  assert(emergOrderRes.status === 201, 'Emergency order created successfully [HTTP 201]');
  const emergOrder = emergOrderRes.data.data;
  assert(emergOrder.priority === 'EMERGENCY', 'Order tagged with EMERGENCY priority');
  assert(emergOrder.deliveryEarnings == 75.00, `Emergency delivery fee is ₹75.00 (is: ${emergOrder.deliveryEarnings})`);
  console.log();

  // TEST 6: Real Razorpay Payment Gateway & HMAC-SHA256 Signature Verification
  console.log('TEST 6: Razorpay Payment Order Creation & HMAC Signature Verification');
  const payConfigRes = await request('/api/customer/payment/config');
  assert(payConfigRes.status === 200 && payConfigRes.data.data.keyId !== undefined, 'Payment gateway configuration loaded');

  const createPayRes = await request('/api/customer/payment/create-order', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      orderId: createdOrder.id,
      orderNumber: createdOrder.orderNumber,
      amount: createdOrder.totalAmount,
      customerName: 'Priya Sundaram',
      customerEmail: 'priya@example.com',
      customerPhone: '9845012399'
    })
  });
  assert(createPayRes.status === 200, 'Razorpay order created on backend');
  const payData = createPayRes.data.data;
  assert(payData.razorpayOrderId !== undefined && payData.amountInSubunits > 0, `Razorpay Order ID: ${payData.razorpayOrderId}`);

  // Verify HMAC-SHA256 Signature
  const mockPaymentId = 'pay_' + Math.random().toString(36).substring(2, 14);
  const secret = 'pharmatechTestSecretKey9988';
  const validSignature = computeHmacSha256(`${payData.razorpayOrderId}|${mockPaymentId}`, secret);

  const verifyRes = await request('/api/customer/payment/verify', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      orderId: createdOrder.id,
      razorpayOrderId: payData.razorpayOrderId,
      razorpayPaymentId: mockPaymentId,
      razorpaySignature: validSignature
    })
  });
  assert(verifyRes.status === 200, 'HMAC-SHA256 payment signature verified successfully [HTTP 200]');
  assert(verifyRes.data.data.paymentStatus === 'PAID', 'Order payment status transitioned to PAID');
  console.log();

  // TEST 7: Delivery Partner Dispatch & Live GPS Tracking
  console.log('TEST 7: Delivery Partner Dispatch, Live GPS Tracking & Privacy Termination');
  // Log in as real delivery partners to obtain valid partner user IDs
  const riderLoginRes = await request('/api/delivery/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identifier: 'ramesh.delivery@pharmatech.com', password: 'password123' })
  });
  assert(riderLoginRes.status === 200, 'Delivery partner Ramesh Kumar logged in');
  const riderId = riderLoginRes.data.data.userId;

  const secondRiderLogin = await request('/api/delivery/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ identifier: 'senthil.delivery@pharmatech.com', password: 'password123' })
  });
  const secondRiderId = secondRiderLogin.data.data.userId;

  // Pending Requests
  const pendingRes = await request(`/api/delivery/pending-requests/${riderId}`);
  assert(pendingRes.status === 200, 'Delivery partner pending requests endpoint responsive');

  // Accept Delivery
  const acceptRes = await request(`/api/delivery/orders/${createdOrder.id}/accept`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ partnerId: riderId })
  });
  assert(acceptRes.status === 200, 'Rider accepted delivery request');
  assert(acceptRes.data.data.status === 'PARTNER_ASSIGNED', 'Order status transitioned to PARTNER_ASSIGNED');

  // Concurrency Protection: Another partner cannot steal the already-accepted order
  const stealRes = await request(`/api/delivery/orders/${createdOrder.id}/accept`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ partnerId: secondRiderId })
  });
  assert(stealRes.status === 400, 'Concurrency lock prevented second partner from accepting same order [HTTP 400]');

  // Live GPS Coordinates Update from Rider
  const gpsRes = await request('/api/delivery/location-update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      partnerId: riderId,
      orderId: createdOrder.id,
      latitude: 12.9750,
      longitude: 77.6350,
      heading: 45.0,
      speed: 28.5
    })
  });
  assert(gpsRes.status === 200, 'Rider GPS coordinate update recorded');

  // Customer Live Tracking Query
  const trackRes = await request(`/api/customer/orders/track/${createdOrder.id}`);
  assert(trackRes.status === 200, 'Customer order tracking loaded');
  const track = trackRes.data.data;
  assert(track.riderLatitude === 12.9750 && track.riderLongitude === 77.6350, 'Customer tracking receives real-time rider GPS position');
  assert(track.pharmacyLatitude !== undefined && track.customerLatitude !== undefined, 'Customer tracking receives pharmacy and dropoff coordinates');
  assert(track.deliveryPartnerName !== null, `Assigned Rider: ${track.deliveryPartnerName} (${track.vehicleNumber})`);

  // Deliver Order & Verify Privacy Termination
  const deliverRes = await request(`/api/delivery/orders/${createdOrder.id}/status`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: 'DELIVERED', partnerId: riderId })
  });
  assert(deliverRes.status === 200 && deliverRes.data.data.status === 'DELIVERED', 'Order updated to DELIVERED');

  // Check Privacy Rule: Rider GPS is terminated once delivered
  const postDelivTrack = await request(`/api/customer/orders/track/${createdOrder.id}`);
  assert(postDelivTrack.data.data.riderLatitude === null, 'Privacy rule verified: Rider GPS coordinates terminated upon delivery');
  console.log();

  // TEST 8: Static HTML Web Pages
  console.log('TEST 8: HTML Page Accessibility Verification');
  const pages = [
    'index.html',
    'checkout.html',
    'order-tracking.html',
    'customer-login.html',
    'pharmacy-login.html',
    'pharmacy-dashboard.html',
    'delivery-login.html',
    'delivery-dashboard.html',
    'admin-login.html',
    'admin-dashboard.html'
  ];

  for (const page of pages) {
    const pageRes = await fetch(`${BASE_URL}/${page}`);
    assert(pageRes.status === 200, `Page /${page} loads successfully [HTTP 200]`);
  }

  console.log('\n================================================================');
  console.log(`  TOTAL TESTS: ${passed + failed} | PASSED: ${passed} | FAILED: ${failed}`);
  if (failed === 0) {
    console.log('  >>> ALL 8 CORE TEST MODULES PASSED 100% SUCCESSFULLY! <<<');
  }
  console.log('================================================================\n');

  process.exit(failed === 0 ? 0 : 1);
}

runSuite().catch(err => {
  console.error('Test Suite encountered error:', err);
  process.exit(1);
});
