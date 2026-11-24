/**
 * Authentication Controller
 * Handles Customer and Pharmacy Owner Login & Registration flows.
 * Strictly verifies credentials, handles route protection, and avoids prefilled credentials.
 */
document.addEventListener('DOMContentLoaded', () => {

  // Global Password Visibility Toggles
  document.querySelectorAll('.password-toggle-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const targetId = btn.getAttribute('data-target');
      const input = document.getElementById(targetId);
      if (!input) return;

      if (input.type === 'password') {
        input.type = 'text';
        btn.innerHTML = `<svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18"></path></svg>`;
      } else {
        input.type = 'password';
        btn.innerHTML = `<svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>`;
      }
    });
  });

  // Tab Switching (Sign In <-> Register)
  const tabBtns = document.querySelectorAll('.auth-tab-btn');
  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const tabTarget = btn.getAttribute('data-tab');
      tabBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');

      document.querySelectorAll('.auth-form-panel').forEach(panel => {
        panel.style.display = 'none';
      });

      const activePanel = document.getElementById(tabTarget);
      if (activePanel) {
        activePanel.style.display = 'block';
      }
    });
  });

  // -------------------------------------------------------------
  // CUSTOMER LOGIN & REGISTRATION
  // -------------------------------------------------------------
  const customerLoginForm = document.getElementById('customerLoginForm');
  const customerRegisterForm = document.getElementById('customerRegisterForm');

  if (customerLoginForm) {
    customerLoginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAlert('customerLoginAlert');

      const identifier = document.getElementById('custLoginEmail').value.trim();
      const password = document.getElementById('custLoginPassword').value;

      if (!identifier || !password) {
        showAlert('customerLoginAlert', 'Please enter your email address and password.', 'error');
        return;
      }

      setBtnLoading('custLoginBtn', true, 'Signing In...');

      try {
        const res = await API.loginCustomer({ identifier, password });
        if (res && res.data) {
          if (res.data.role !== 'ROLE_CUSTOMER') {
            throw new Error('Access denied. Please use the designated staff portal for your account role.');
          }
          API.saveUserSession(res.data);
          showAlert('customerLoginAlert', 'Sign in successful! Redirecting...', 'success');
          const returnUrl = new URLSearchParams(window.location.search).get('returnUrl') || 'index.html';
          setTimeout(() => {
            window.location.href = returnUrl;
          }, 600);
        }
      } catch (err) {
        showAlert('customerLoginAlert', err.message || 'Invalid email address or password.', 'error');
      } finally {
        setBtnLoading('custLoginBtn', false, 'Sign In →');
      }
    });
  }

  if (customerRegisterForm) {
    customerRegisterForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAlert('customerRegisterAlert');

      const fullName = document.getElementById('custRegName').value.trim();
      const email = document.getElementById('custRegEmail').value.trim();
      const phoneNumber = document.getElementById('custRegPhone').value.trim();
      const password = document.getElementById('custRegPassword').value;
      const confirmPassword = document.getElementById('custRegConfirm').value;
      const deliveryAddress = document.getElementById('custRegAddress').value.trim();
      const city = document.getElementById('custRegCity').value.trim();
      const pincode = document.getElementById('custRegPincode').value.trim();

      if (password !== confirmPassword) {
        showAlert('customerRegisterAlert', 'Passwords do not match.', 'error');
        return;
      }

      if (password.length < 6) {
        showAlert('customerRegisterAlert', 'Password must have at least 6 characters.', 'error');
        return;
      }

      setBtnLoading('custRegBtn', true, 'Creating Account...');

      try {
        const res = await API.registerCustomer({
          fullName, email, phoneNumber, password, confirmPassword,
          deliveryAddress, city, pincode
        });

        if (res && res.data) {
          API.saveUserSession(res.data);
          showAlert('customerRegisterAlert', 'Account registered successfully! Redirecting...', 'success');
          const returnUrl = new URLSearchParams(window.location.search).get('returnUrl') || 'index.html';
          setTimeout(() => {
            window.location.href = returnUrl;
          }, 800);
        }
      } catch (err) {
        showAlert('customerRegisterAlert', err.message || 'Registration failed. Please check your information.', 'error');
      } finally {
        setBtnLoading('custRegBtn', false, 'Create Account');
      }
    });
  }

  // -------------------------------------------------------------
  // PHARMACY OWNER LOGIN & REGISTRATION
  // -------------------------------------------------------------
  const pharmacyLoginForm = document.getElementById('pharmacyLoginForm');
  const pharmacyRegisterForm = document.getElementById('pharmacyRegisterForm');

  if (pharmacyLoginForm) {
    pharmacyLoginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAlert('pharmacyLoginAlert');

      const identifier = document.getElementById('pharmLoginEmail').value.trim();
      const password = document.getElementById('pharmLoginPassword').value;

      if (!identifier || !password) {
        showAlert('pharmacyLoginAlert', 'Please enter your registered email address and password.', 'error');
        return;
      }

      setBtnLoading('pharmLoginBtn', true, 'Authenticating...');

      try {
        const res = await API.loginPharmacy({ identifier, password });
        if (res && res.data) {
          if (res.data.role !== 'ROLE_PHARMACY_OWNER') {
            throw new Error('Access denied. This account is not registered as a Pharmacy Owner.');
          }

          const user = res.data;
          API.saveUserSession(user);

          if (!user.subscriptionActive) {
            showAlert('pharmacyLoginAlert', 'Authentication successful! Directing to partner subscription...', 'success');
            setTimeout(() => {
              window.location.href = 'pharmacy-subscription.html';
            }, 600);
          } else {
            showAlert('pharmacyLoginAlert', 'Authentication successful! Opening dashboard...', 'success');
            setTimeout(() => {
              window.location.href = 'pharmacy-dashboard.html';
            }, 600);
          }
        }
      } catch (err) {
        showAlert('pharmacyLoginAlert', err.message || 'Invalid email address or password.', 'error');
      } finally {
        setBtnLoading('pharmLoginBtn', false, 'Sign In to Pharmacy Dashboard →');
      }
    });
  }

  if (pharmacyRegisterForm) {
    pharmacyRegisterForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAlert('pharmacyRegisterAlert');

      const pharmacyName = document.getElementById('pharmRegStoreName').value.trim();
      const ownerName = document.getElementById('pharmRegOwnerName').value.trim();
      const phoneNumber = document.getElementById('pharmRegPhone').value.trim();
      const email = document.getElementById('pharmRegEmail').value.trim();
      const pharmacyAddress = document.getElementById('pharmRegAddress').value.trim();
      const city = document.getElementById('pharmRegCity').value.trim();
      const pincode = document.getElementById('pharmRegPin').value.trim();
      const licenseNumber = document.getElementById('pharmRegLicense').value.trim();
      const password = document.getElementById('pharmRegPassword').value;
      const confirmPassword = document.getElementById('pharmRegConfirm').value;

      if (password !== confirmPassword) {
        showAlert('pharmacyRegisterAlert', 'Passwords do not match.', 'error');
        return;
      }

      if (password.length < 6) {
        showAlert('pharmacyRegisterAlert', 'Password must have at least 6 characters.', 'error');
        return;
      }

      setBtnLoading('pharmRegBtn', true, 'Registering Store...');

      try {
        const res = await API.registerPharmacy({
          pharmacyName, ownerName, phoneNumber, email, pharmacyAddress,
          city, pincode, licenseNumber, password, confirmPassword
        });

        if (res && res.data) {
          API.saveUserSession(res.data);
          showAlert('pharmacyRegisterAlert', 'Pharmacy registered! Proceeding to partner subscription plan...', 'success');
          setTimeout(() => {
            window.location.href = 'pharmacy-subscription.html';
          }, 800);
        }
      } catch (err) {
        showAlert('pharmacyRegisterAlert', err.message || 'Registration failed. Please check required fields.', 'error');
      } finally {
        setBtnLoading('pharmRegBtn', false, 'Register Pharmacy & Continue →');
      }
    });
  }

  // UI Helpers
  function showAlert(elemId, message, type) {
    const el = document.getElementById(elemId);
    if (!el) return;
    el.style.display = 'block';
    el.textContent = message;
    if (type === 'error') {
      el.style.background = '#fef2f2';
      el.style.color = '#991b1b';
      el.style.border = '1px solid #fecdd3';
    } else {
      el.style.background = '#ecfdf5';
      el.style.color = '#065f46';
      el.style.border = '1px solid #a7f3d0';
    }
  }

  function clearAlert(elemId) {
    const el = document.getElementById(elemId);
    if (el) el.style.display = 'none';
  }

  function setBtnLoading(btnId, isLoading, text) {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    btn.disabled = isLoading;
    btn.textContent = text;
    btn.style.opacity = isLoading ? '0.7' : '1';
  }
});
