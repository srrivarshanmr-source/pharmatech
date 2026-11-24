/**
 * Pharmacy Partner Subscription Controller
 * Simulates ₹200/month partner subscription activation with database synchronization.
 */
document.addEventListener('DOMContentLoaded', () => {
  let user = API.getUserSession();

  // Demonstration Fallback:
  // If not logged in as a pharmacy owner, initialize Apollo Pharmacy demo session
  if (!user || user.role !== 'ROLE_PHARMACY_OWNER') {
    console.info('No pharmacy owner session found. Initializing demo session for subscription test...');
    user = {
      userId: 2,
      fullName: 'Dr. Vikram Patel',
      email: 'owner@apollo.com',
      phoneNumber: '9888877771',
      role: 'ROLE_PHARMACY_OWNER',
      pharmacyId: 1,
      pharmacyName: 'Apollo Pharmacy - Metro Express',
      subscriptionActive: false, // allow user to test the activation button!
      token: 'demo-token-apollo'
    };
    API.saveUserSession(user);
  }

  // Populate dynamic store & owner info
  const ownerNameEl = document.getElementById('subOwnerName');
  const storeNameEl = document.getElementById('subStoreName');
  const activateSubBtn = document.getElementById('activateSubBtn');
  const subStatusAlert = document.getElementById('subStatusAlert');
  const activationModal = document.getElementById('activationSuccessModal');
  const txnIdEl = document.getElementById('modalTxnId');
  const goToDashboardBtn = document.getElementById('goToDashboardBtn');

  if (ownerNameEl) ownerNameEl.textContent = user.fullName;
  if (storeNameEl) storeNameEl.textContent = user.pharmacyName || 'Partner Pharmacy Store';

  // Check if already active
  if (user.subscriptionActive) {
    if (subStatusAlert) {
      subStatusAlert.style.display = 'block';
      subStatusAlert.innerHTML = `✓ Your <strong>PharmaTech Pharmacy Partner Plan</strong> is currently active! You can proceed to your dashboard anytime.`;
    }
    if (activateSubBtn) {
      activateSubBtn.textContent = 'Go to Pharmacy Dashboard';
      activateSubBtn.onclick = () => window.location.href = 'pharmacy-dashboard.html';
    }
    return;
  }

  // Activate Button Click
  if (activateSubBtn) {
    activateSubBtn.addEventListener('click', async () => {
      activateSubBtn.disabled = true;
      activateSubBtn.innerHTML = `
        <svg class="animate-spin" style="width: 18px; height: 18px; display: inline-block; vertical-align: middle; margin-right: 8px;" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
        Simulating Secure Payment Activation...
      `;

      try {
        const res = await API.activateSubscription(user.userId);
        if (res && res.data) {
          const sub = res.data;

          // Update local session
          user.subscriptionActive = true;
          API.saveUserSession(user);

          // Show Success Modal
          if (txnIdEl) txnIdEl.textContent = sub.transactionReference || 'SIM-TXN-' + Date.now().toString(36).toUpperCase();
          if (activationModal) activationModal.classList.add('open');

          if (goToDashboardBtn) {
            goToDashboardBtn.addEventListener('click', () => {
              window.location.href = 'pharmacy-dashboard.html';
            });
          }
        }
      } catch (err) {
        alert('Activation failed: ' + err.message);
        activateSubBtn.disabled = false;
        activateSubBtn.textContent = 'Activate Subscription (₹200/month)';
      }
    });
  }
});
