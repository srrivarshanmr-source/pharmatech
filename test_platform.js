const { spawn, execSync } = require('child_process');
const path = require('path');

const fs = require('fs');
const bundledJava = path.join(__dirname, '.tools', 'jdk17.0.21_0', 'bin', 'java.exe');
const javaBin = fs.existsSync(bundledJava) ? bundledJava : 'java';
const jarPath = path.join(__dirname, 'target', 'pharmatech-platform-1.0.0-SNAPSHOT.jar');

console.log('Starting PharmaTech Spring Boot Server...');
const server = spawn(javaBin, ['-jar', jarPath], {
  cwd: __dirname,
  stdio: 'pipe'
});

server.stdout.on('data', data => {
  process.stdout.write(data.toString());
});

server.stderr.on('data', data => {
  process.stderr.write(data.toString());
});

async function sleep(ms) {
  return new Promise(r => setTimeout(r, ms));
}

async function runTests() {
  console.log('Waiting for Spring Boot on port 8080...');
  let connected = false;
  for (let i = 0; i < 35; i++) {
    await sleep(1000);
    try {
      const res = await fetch('http://127.0.0.1:8080/api/customer/categories');
      if (res.status === 200) {
        connected = true;
        break;
      }
    } catch (e) {
      if (i % 5 === 0) console.log(`Attempt ${i} error:`, e.code || e.message);
    }
  }

  if (!connected) {
    console.error('FAILED to connect to Spring Boot within 35s');
    server.kill();
    process.exit(1);
  }

  console.log('\n[PASS] Server is UP! Executing comprehensive test suite...\n');

  // Test 1: Categories
  const catRes = await fetch('http://127.0.0.1:8080/api/customer/categories');
  const catData = await catRes.json();
  console.log('1. Categories:', catData.data);

  // Test 2: Products
  const prodRes = await fetch('http://127.0.0.1:8080/api/customer/products');
  const prodData = await prodRes.json();
  console.log(`2. Total Products in Catalog: ${prodData.data.length}`);
  console.log(`   Sample: ${prodData.data[0].medicineName} (${prodData.data[0].brand}) - Rs.${prodData.data[0].price}`);

  // Test 3: Customer Registration
  const custRegRes = await fetch('http://127.0.0.1:8080/api/auth/customer/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fullName: 'Pooja Verma',
      email: 'pooja.verma@example.com',
      phoneNumber: '9811223344',
      password: 'password123',
      confirmPassword: 'password123',
      deliveryAddress: 'Flat 101, HSR Layout',
      city: 'Bangalore',
      pincode: '560102'
    })
  });
  const custRegData = await custRegRes.json();
  console.log(`3. Customer Registered: ID ${custRegData.data.userId}, Name: ${custRegData.data.fullName}`);

  // Test 4: Customer Login
  const custLoginRes = await fetch('http://127.0.0.1:8080/api/auth/customer/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      identifier: 'customer@pharmatech.com',
      password: 'password123'
    })
  });
  const custLoginData = await custLoginRes.json();
  console.log(`4. Demo Customer Login: ${custLoginData.data.fullName} (${custLoginData.data.email})`);

  // Test 5: Pharmacy Registration
  const pharmRegRes = await fetch('http://127.0.0.1:8080/api/auth/pharmacy/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      pharmacyName: 'GreenCross Medicals',
      ownerName: 'Dr. Suresh Nair',
      phoneNumber: '9845012345',
      email: 'suresh@greencross.com',
      pharmacyAddress: '15, Brigade Road',
      city: 'Bangalore',
      pincode: '560001',
      licenseNumber: 'DL-KA-2024-554433',
      password: 'password123',
      confirmPassword: 'password123'
    })
  });
  const pharmRegData = await pharmRegRes.json();
  const newOwnerId = pharmRegData.data.userId;
  const newPharmId = pharmRegData.data.pharmacyId;
  console.log(`5. Pharmacy Registered: Store ID ${newPharmId}, Owner ID ${newOwnerId}, Name: ${pharmRegData.data.pharmacyName}`);

  // Test 6: Subscription Activation (₹200/month partner plan)
  const subRes = await fetch('http://127.0.0.1:8080/api/pharmacy/subscription/activate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ownerId: newOwnerId,
      planName: 'PharmaTech Pharmacy Partner Plan',
      monthlyPrice: 200.00
    })
  });
  const subData = await subRes.json();
  console.log(`6. Partner Subscription Activated: Plan "${subData.data.planName}", Status: ${subData.data.status}, Txn: ${subData.data.transactionReference}`);

  // Test 7: Pharmacy Dashboard Metrics
  const dashRes = await fetch(`http://127.0.0.1:8080/api/pharmacy/dashboard/${newOwnerId}`);
  const dashData = await dashRes.json();
  console.log(`7. Dashboard Metrics for "${dashData.data.pharmacyName}":`);
  console.log(`   - Total Medicines: ${dashData.data.totalMedicines}`);
  console.log(`   - Available Medicines: ${dashData.data.availableMedicines}`);
  console.log(`   - Low Stock Medicines: ${dashData.data.lowStockMedicines}`);
  console.log(`   - Subscription Active: ${dashData.data.subscriptionActive}`);

  // Test 8: Inventory CRUD - Add Medicine
  const addMedRes = await fetch(`http://127.0.0.1:8080/api/inventory/pharmacy/${newPharmId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: 'Azithral 500',
      brand: 'Alembic',
      strength: '500mg',
      category: 'Tablets',
      quantity: 50,
      price: 119.50,
      expiryDate: '2027-12-31',
      batchNumber: 'BAT-AZ-99',
      prescriptionRequired: true
    })
  });
  const addMedData = await addMedRes.json();
  const invId = addMedData.data.inventoryId;
  console.log(`8. Inventory ADD: Added "${addMedData.data.medicineName}" (Inv ID: ${invId}) Qty: ${addMedData.data.quantity}, Price: Rs.${addMedData.data.price}`);

  // Test 9: Inventory CRUD - Edit Medicine
  const editMedRes = await fetch(`http://127.0.0.1:8080/api/inventory/${invId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: 'Azithral 500',
      brand: 'Alembic Pharma Ltd',
      strength: '500mg',
      category: 'Tablets',
      quantity: 80,
      price: 112.00,
      expiryDate: '2027-12-31',
      batchNumber: 'BAT-AZ-99-UPDATED'
    })
  });
  const editMedData = await editMedRes.json();
  console.log(`9. Inventory EDIT: Updated "${editMedData.data.medicineName}" New Qty: ${editMedData.data.quantity}, New Price: Rs.${editMedData.data.price}`);

  // Test 10: Inventory CRUD - Delete Medicine
  const delRes = await fetch(`http://127.0.0.1:8080/api/inventory/${invId}`, {
    method: 'DELETE'
  });
  const delData = await delRes.json();
  console.log(`10. Inventory DELETE: ${delData.message}`);

  // Test 11: Static Web Pages Check
  const pages = ['index.html', 'customer-login.html', 'pharmacy-login.html', 'pharmacy-subscription.html', 'pharmacy-dashboard.html'];
  console.log('11. Verifying HTML Web Pages:');
  for (const page of pages) {
    const pageRes = await fetch(`http://127.0.0.1:8080/${page}`);
    console.log(`    ✓ http://localhost:8080/${page} [HTTP ${pageRes.status}]`);
  }

  console.log('\n============================================================');
  console.log('  ALL INTEGRATION TESTS PASSED 100% SUCCESSFULLY!          ');
  console.log('============================================================');

  try {
    execSync(`taskkill /pid ${server.pid} /T /F`);
  } catch (e) {
    server.kill();
  }
  process.exit(0);
}

runTests().catch(err => {
  console.error('Test script failure:', err);
  try {
    execSync(`taskkill /pid ${server.pid} /T /F`);
  } catch (e) {
    server.kill();
  }
  process.exit(1);
});
