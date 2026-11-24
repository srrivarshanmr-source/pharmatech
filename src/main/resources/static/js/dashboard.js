/**
 * Pharmacy Owner Dashboard & Inventory Controller
 * Handles real-time metrics, isolated inventory CRUD operations, profile viewing, and section toggles.
 */
document.addEventListener('DOMContentLoaded', () => {
  let user = API.getUserSession();

  // Strict route protection: Only authenticated pharmacy owners allowed
  if (!user || user.role !== 'ROLE_PHARMACY_OWNER') {
    window.location.replace('pharmacy-login.html');
    return;
  }

  // Guard: If subscription is not active, direct to subscription activation
  if (!user.subscriptionActive) {
    window.location.href = 'pharmacy-subscription.html';
    return;
  }

  // Populate Header & Sidebar Owner info
  document.querySelectorAll('.owner-name-display').forEach(el => el.textContent = user.fullName);
  document.querySelectorAll('.pharmacy-name-display').forEach(el => el.textContent = user.pharmacyName || 'Partner Pharmacy');
  document.querySelectorAll('.owner-email-display').forEach(el => el.textContent = user.email);

  // Dashboard Metrics Elements
  const totalMedVal = document.getElementById('metricTotalMed');
  const availMedVal = document.getElementById('metricAvailMed');
  const lowStockVal = document.getElementById('metricLowStock');
  const pendingOrdersVal = document.getElementById('metricPendingOrders');

  // Inventory Elements
  const inventoryTableBody = document.getElementById('inventoryTableBody');
  const inventorySearchInput = document.getElementById('inventorySearchInput');
  const openAddModalBtn = document.getElementById('openAddModalBtn');
  const addMedicineModal = document.getElementById('addMedicineModal');
  const closeAddModalBtn = document.getElementById('closeAddModalBtn');
  const addMedicineForm = document.getElementById('addMedicineForm');
  const catalogSelect = document.getElementById('medCatalogSelect');

  // Edit Modal Elements
  const editMedicineModal = document.getElementById('editMedicineModal');
  const closeEditModalBtn = document.getElementById('closeEditModalBtn');
  const editMedicineForm = document.getElementById('editMedicineForm');

  // Nav Items & Panels
  const navLinks = document.querySelectorAll('.sidebar-nav .nav-item-link');
  const viewPanels = document.querySelectorAll('.dashboard-view-panel');
  const comingSoonTitle = document.getElementById('comingSoonFeatureTitle');
  const logoutBtn = document.getElementById('logoutOwnerBtn');

  let currentPharmacyId = user.pharmacyId;
  let catalogMedicines = [];

  // Initialize
  initDashboard();

  async function initDashboard() {
    await loadProfileAndEnsurePharmacyId();
    loadDashboardMetrics();
    loadInventory();
    loadMasterCatalog();
    loadPharmacyOrders();
    startNotificationPolling();
  }

  async function loadProfileAndEnsurePharmacyId() {
    try {
      const res = await API.getPharmacyProfile(user.userId);
      if (res && res.data) {
        const profile = res.data;
        if (profile.pharmacy) {
          currentPharmacyId = profile.pharmacy.id;
          user.pharmacyId = profile.pharmacy.id;
          user.pharmacyName = profile.pharmacy.name;
          API.saveUserSession(user);

          // Populate Profile Tab
          document.getElementById('profStoreName').textContent = profile.pharmacy.name;
          document.getElementById('profOwnerName').textContent = profile.fullName;
          document.getElementById('profEmail').textContent = profile.email;
          document.getElementById('profPhone').textContent = profile.phoneNumber;
          document.getElementById('profLicense').textContent = profile.pharmacy.licenseNumber;
          document.getElementById('profAddress').textContent = profile.pharmacy.address;
          document.getElementById('profCity').textContent = profile.pharmacy.city;
          document.getElementById('profPincode').textContent = profile.pharmacy.pincode;
          document.getElementById('profRating').textContent = `★ ${Number(profile.pharmacy.rating || 4.8).toFixed(1)}`;
          document.getElementById('profSubStatus').textContent = profile.subscriptionActive ? 'Active (₹200/month Partner Plan)' : 'Inactive';
        }
      }
    } catch (err) {
      console.warn('Profile fetch warning:', err);
    }
  }

  // Load Dashboard Metrics
  async function loadDashboardMetrics() {
    try {
      const res = await API.getDashboardStats(user.userId);
      if (res && res.data) {
        const stats = res.data;
        if (totalMedVal) totalMedVal.textContent = stats.totalMedicines;
        if (availMedVal) availMedVal.textContent = stats.availableMedicines;
        if (lowStockVal) lowStockVal.textContent = stats.lowStockMedicines;
        if (pendingOrdersVal) pendingOrdersVal.textContent = stats.pendingOrders;
      }
    } catch (err) {
      console.error('Failed to load metrics', err);
    }
  }

  // Load Inventory Table
  async function loadInventory(query = '') {
    if (!inventoryTableBody) return;
    if (!currentPharmacyId) return;

    inventoryTableBody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-muted);">
          Loading store inventory...
        </td>
      </tr>
    `;

    try {
      let res;
      if (query && query.trim()) {
        res = await API.searchPharmacyInventory(currentPharmacyId, query.trim());
      } else {
        res = await API.getPharmacyInventory(currentPharmacyId);
      }

      const items = res && res.data ? res.data : [];

      if (items.length === 0) {
        inventoryTableBody.innerHTML = `
          <tr>
            <td colspan="7" style="text-align: center; padding: 3rem; color: var(--text-muted);">
              <p style="font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.25rem;">No medicines in inventory yet</p>
              <p style="font-size: 0.85rem; margin-bottom: 1rem;">Click "Add New Medicine" above to stock your pharmacy catalog.</p>
            </td>
          </tr>
        `;
        return;
      }

      inventoryTableBody.innerHTML = items.map(item => {
        let stockTagClass = 'stock-in';
        let stockLabel = `${item.quantity} Units`;
        if (item.quantity <= 0) {
          stockTagClass = 'stock-out';
          stockLabel = 'Out of Stock (0)';
        } else if (item.lowStock) {
          stockTagClass = 'stock-low';
          stockLabel = `Low Stock (${item.quantity})`;
        }

        return `
          <tr>
            <td>
              <div class="table-med-title">${escapeHtml(item.medicineName)}</div>
              <div class="table-med-sub">${escapeHtml(item.brand || '')} • ${escapeHtml(item.strength || '')}</div>
            </td>
            <td>
              <span style="background: #f1f5f9; padding: 0.2rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600;">
                ${escapeHtml(item.category || 'General')}
              </span>
            </td>
            <td>
              <span class="stock-tag ${stockTagClass}">${stockLabel}</span>
            </td>
            <td style="font-weight: 700;">₹${Number(item.price).toFixed(2)}</td>
            <td>
              <span style="font-size: 0.82rem; color: var(--text-muted);">${item.expiryDate || 'N/A'}</span>
            </td>
            <td>
              <code style="font-size: 0.78rem; background: #f8fafc; padding: 0.2rem 0.4rem; border-radius: 4px; border: 1px solid #e2e8f0;">
                ${escapeHtml(item.batchNumber || 'BAT-01')}
              </code>
            </td>
            <td>
              <div class="action-buttons-group">
                <button class="btn-icon-action btn-edit-med" title="Edit Medicine" data-item='${JSON.stringify(item)}'>
                  ✏️
                </button>
                <button class="btn-icon-action btn-icon-delete btn-delete-med" title="Delete Medicine" data-id="${item.inventoryId}" data-name="${escapeHtml(item.medicineName)}">
                  🗑️
                </button>
              </div>
            </td>
          </tr>
        `;
      }).join('');

      // Attach Edit & Delete Events
      inventoryTableBody.querySelectorAll('.btn-edit-med').forEach(btn => {
        btn.addEventListener('click', () => {
          const item = JSON.parse(btn.getAttribute('data-item'));
          openEditModal(item);
        });
      });

      inventoryTableBody.querySelectorAll('.btn-delete-med').forEach(btn => {
        btn.addEventListener('click', async () => {
          const id = btn.getAttribute('data-id');
          const name = btn.getAttribute('data-name');
          if (confirm(`Are you sure you want to delete "${name}" from your store inventory?`)) {
            try {
              await API.deleteMedicine(id);
              loadInventory(inventorySearchInput ? inventorySearchInput.value : '');
              loadDashboardMetrics();
            } catch (err) {
              alert('Error deleting medicine: ' + err.message);
            }
          }
        });
      });

    } catch (err) {
      inventoryTableBody.innerHTML = `
        <tr><td colspan="7" style="color: #ef4444; text-align: center; padding: 2rem;">Error loading inventory: ${err.message}</td></tr>
      `;
    }
  }

  // Load Master Catalog for Quick Select
  async function loadMasterCatalog() {
    try {
      const res = await API.getMasterCatalog();
      if (res && res.data && catalogSelect) {
        catalogMedicines = res.data;
        catalogSelect.innerHTML = `<option value="">-- Or Choose Existing Known Medicine --</option>` +
          catalogMedicines.map(m => `
            <option value="${m.id}">${escapeHtml(m.name)} (${escapeHtml(m.strength)}) - ${escapeHtml(m.brand)}</option>
          `).join('');

        catalogSelect.addEventListener('change', () => {
          const selectedId = catalogSelect.value;
          if (!selectedId) return;
          const selected = catalogMedicines.find(m => m.id == selectedId);
          if (selected) {
            document.getElementById('addMedName').value = selected.name;
            document.getElementById('addMedBrand').value = selected.brand;
            document.getElementById('addMedStrength').value = selected.strength;
            document.getElementById('addMedCategory').value = selected.category;
            document.getElementById('addMedPrice').value = selected.basePrice;
            document.getElementById('addMedDesc').value = selected.description || '';
            document.getElementById('addMedRx').checked = selected.prescriptionRequired;
          }
        });
      }
    } catch (err) {
      console.warn('Could not load master catalog dropdown', err);
    }
  }

  // Search Inventory Filter
  if (inventorySearchInput) {
    let timer;
    inventorySearchInput.addEventListener('input', (e) => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        loadInventory(e.target.value);
      }, 300);
    });
  }

  // Modal Controls: Add Medicine
  if (openAddModalBtn && addMedicineModal) {
    openAddModalBtn.addEventListener('click', () => {
      addMedicineForm.reset();
      addMedicineModal.classList.add('open');
    });
  }

  if (closeAddModalBtn && addMedicineModal) {
    closeAddModalBtn.addEventListener('click', () => {
      addMedicineModal.classList.remove('open');
    });
  }

  // Submit Add Medicine
  if (addMedicineForm) {
    addMedicineForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const payload = {
        name: document.getElementById('addMedName').value.trim(),
        brand: document.getElementById('addMedBrand').value.trim(),
        strength: document.getElementById('addMedStrength').value.trim(),
        category: document.getElementById('addMedCategory').value,
        quantity: parseInt(document.getElementById('addMedQty').value, 10),
        price: parseFloat(document.getElementById('addMedPrice').value),
        expiryDate: document.getElementById('addMedExpiry').value || null,
        batchNumber: document.getElementById('addMedBatch').value.trim() || null,
        description: document.getElementById('addMedDesc').value.trim(),
        prescriptionRequired: document.getElementById('addMedRx').checked
      };

      const submitBtn = document.getElementById('saveMedBtn');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Saving...';

      try {
        await API.addMedicine(currentPharmacyId, payload);
        addMedicineModal.classList.remove('open');
        addMedicineForm.reset();
        loadInventory();
        loadDashboardMetrics();
      } catch (err) {
        alert('Failed to add medicine: ' + err.message);
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Add to Inventory';
      }
    });
  }

  // Modal Controls: Edit Medicine
  function openEditModal(item) {
    document.getElementById('editInvId').value = item.inventoryId;
    document.getElementById('editMedName').value = item.medicineName;
    document.getElementById('editMedBrand').value = item.brand;
    document.getElementById('editMedStrength').value = item.strength;
    document.getElementById('editMedCategory').value = item.category;
    document.getElementById('editMedQty').value = item.quantity;
    document.getElementById('editMedPrice').value = item.price;
    document.getElementById('editMedExpiry').value = item.expiryDate || '';
    document.getElementById('editMedBatch').value = item.batchNumber || '';
    editMedicineModal.classList.add('open');
  }

  if (closeEditModalBtn && editMedicineModal) {
    closeEditModalBtn.addEventListener('click', () => {
      editMedicineModal.classList.remove('open');
    });
  }

  if (editMedicineForm) {
    editMedicineForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const invId = document.getElementById('editInvId').value;
      const payload = {
        name: document.getElementById('editMedName').value.trim(),
        brand: document.getElementById('editMedBrand').value.trim(),
        strength: document.getElementById('editMedStrength').value.trim(),
        category: document.getElementById('editMedCategory').value,
        quantity: parseInt(document.getElementById('editMedQty').value, 10),
        price: parseFloat(document.getElementById('editMedPrice').value),
        expiryDate: document.getElementById('editMedExpiry').value || null,
        batchNumber: document.getElementById('editMedBatch').value.trim() || null
      };

      const submitBtn = document.getElementById('updateMedBtn');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Updating...';

      try {
        await API.updateMedicine(invId, payload);
        editMedicineModal.classList.remove('open');
        loadInventory(inventorySearchInput ? inventorySearchInput.value : '');
        loadDashboardMetrics();
      } catch (err) {
        alert('Failed to update medicine: ' + err.message);
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Changes';
      }
    });
  }

  // Sidebar Tab Navigation
  navLinks.forEach(link => {
    link.addEventListener('click', () => {
      const targetView = link.getAttribute('data-view');
      if (!targetView) return;

      navLinks.forEach(l => l.classList.remove('active'));
      link.classList.add('active');

      viewPanels.forEach(panel => {
        panel.style.display = 'none';
      });

      if (targetView === 'dashboard') {
        document.getElementById('viewDashboard').style.display = 'block';
        loadDashboardMetrics();
      } else if (targetView === 'inventory') {
        document.getElementById('viewInventory').style.display = 'block';
        loadInventory();
      } else if (targetView === 'invoice') {
        document.getElementById('viewInvoice').style.display = 'block';
      } else if (targetView === 'purchase-history') {
        document.getElementById('viewPurchaseHistory').style.display = 'block';
        loadPurchaseHistory();
      } else if (targetView === 'billing') {
        document.getElementById('viewBilling').style.display = 'block';
        setupBillingInventoryDropdowns();
        loadRecentBills();
      } else if (targetView === 'sales-history') {
        document.getElementById('viewSalesHistory').style.display = 'block';
        loadSalesHistory();
      } else if (targetView === 'orders' || targetView === 'delivery') {
        document.getElementById('viewOrders').style.display = 'block';
        loadPharmacyOrders();
      } else if (targetView === 'earnings') {
        document.getElementById('viewEarnings').style.display = 'block';
        loadPharmacyEarnings();
      } else if (targetView === 'reports') {
        document.getElementById('viewReports').style.display = 'block';
        loadReports();
      } else if (targetView === 'profile') {
        document.getElementById('viewProfile').style.display = 'block';
      } else if (targetView === 'subscription') {
        window.location.href = 'pharmacy-subscription.html';
      } else {
        document.getElementById('viewComingSoon').style.display = 'block';
        if (comingSoonTitle) {
          comingSoonTitle.textContent = link.querySelector('.nav-label').textContent;
        }
      }
    });
  });

  // =============================================================
  // MODULE 1: INVOICE UPLOAD & 2-STAGE STOCK CONFIRMATION
  // =============================================================
  const invoiceDropzone = document.getElementById('invoiceDropzone');
  const invoiceFileInput = document.getElementById('invoiceFileInput');
  const selectedFileName = document.getElementById('selectedFileName');
  const invoiceRawTextArea = document.getElementById('invoiceRawTextArea');
  const loadSampleInvoiceBtn = document.getElementById('loadSampleInvoiceBtn');
  const extractInvoiceBtn = document.getElementById('extractInvoiceBtn');
  const invoicePreviewCard = document.getElementById('invoicePreviewCard');
  const previewInvoiceNum = document.getElementById('previewInvoiceNum');
  const previewSupplierName = document.getElementById('previewSupplierName');
  const duplicateInvoiceAlert = document.getElementById('duplicateInvoiceAlert');
  const invoicePreviewTableBody = document.getElementById('invoicePreviewTableBody');
  const addInvoicePreviewRowBtn = document.getElementById('addInvoicePreviewRowBtn');
  const previewTotalItemsCount = document.getElementById('previewTotalItemsCount');
  const previewTotalQtyCount = document.getElementById('previewTotalQtyCount');
  const previewTotalValDisplay = document.getElementById('previewTotalValDisplay');
  const confirmInvoiceBtn = document.getElementById('confirmInvoiceBtn');
  const cancelInvoicePreviewBtn = document.getElementById('cancelInvoicePreviewBtn');
  const invoiceResultCard = document.getElementById('invoiceResultCard');
  const invoiceResultMessage = document.getElementById('invoiceResultMessage');
  const invoiceSummaryBadge = document.getElementById('invoiceSummaryBadge');
  const invoiceParsedTableBody = document.getElementById('invoiceParsedTableBody');

  let currentExtractedData = null;

  if (invoiceDropzone && invoiceFileInput) {
    invoiceDropzone.addEventListener('click', () => invoiceFileInput.click());

    invoiceFileInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (file) {
        selectedFileName.textContent = `Selected: ${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
        if (file.name.endsWith('.txt') || file.name.endsWith('.csv')) {
          const reader = new FileReader();
          reader.onload = (event) => {
            invoiceRawTextArea.value = event.target.result;
          };
          reader.readAsText(file);
        } else {
          invoiceRawTextArea.value = `[Binary file loaded: ${file.name}] Click 'Extract & Preview Invoice Items' below to parse.`;
        }
      }
    });

    invoiceDropzone.addEventListener('dragover', (e) => {
      e.preventDefault();
      invoiceDropzone.style.borderColor = 'var(--primary)';
    });
    invoiceDropzone.addEventListener('dragleave', () => {
      invoiceDropzone.style.borderColor = 'var(--border-light)';
    });
    invoiceDropzone.addEventListener('drop', (e) => {
      e.preventDefault();
      invoiceDropzone.style.borderColor = 'var(--border-light)';
      if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        invoiceFileInput.files = e.dataTransfer.files;
        invoiceFileInput.dispatchEvent(new Event('change'));
      }
    });
  }

  if (loadSampleInvoiceBtn) {
    loadSampleInvoiceBtn.addEventListener('click', async () => {
      try {
        const res = await API.getSampleInvoiceTemplate();
        if (res && res.data) {
          invoiceRawTextArea.value = res.data;
          selectedFileName.textContent = 'Loaded: DEMO-INV-005.txt';
        }
      } catch (err) {
        invoiceRawTextArea.value = `Invoice number: DEMO-INV-005\nSupplier: Sample Medical Distributors\nDate: 20-Nov-2026\n\nMedicine 1:\n- Name: Dolo 650\n- Brand: Micro Labs\n- Strength: 650 mg\n- Quantity: 150\n- Price: 30.50\n- Batch: BAT-DOLO-01\n- Expiry: 19-May-2028\n\nMedicine 2:\n- Name: Augmentin 625 Duo\n- Brand: GSK\n- Strength: 625 mg\n- Quantity: 45\n- Price: 204.00\n- Batch: BAT-AUG-04\n- Expiry: 17-Nov-2027\n\nMedicine 3:\n- Name: Pan 40\n- Brand: Alkem\n- Strength: 40 mg\n- Quantity: 60\n- Price: 155.00\n- Batch: BAT-PAN-09\n- Expiry: 17-Nov-2028\n\nMedicine 4:\n- Name: Benadryl Cough Syrup\n- Brand: Johnson & Johnson\n- Form: Syrup / 150 ml\n- Quantity: 8\n- Price: 138.00\n- Batch: BAT-BEN-03\n- Expiry: 17-Feb-2028\n\nMedicine 5:\n- Name: Ascoril D Plus Syrup\n- Brand: Glenmark\n- Form: Syrup / 100 ml\n- Quantity: 30\n- Price: 115.00\n- Batch: BAT-ASC-11\n- Expiry: 17-Jul-2028\n`;
      }
    });
  }

  // STAGE 1: EXTRACT & PREVIEW (DOES NOT MODIFY DATABASE)
  if (extractInvoiceBtn) {
    extractInvoiceBtn.addEventListener('click', async () => {
      const hasFile = invoiceFileInput && invoiceFileInput.files && invoiceFileInput.files.length > 0;
      const text = invoiceRawTextArea ? invoiceRawTextArea.value.trim() : '';

      if (!hasFile && (!text || text.startsWith('[Binary file'))) {
        alert('Please choose an invoice file (PDF, image, text) or paste invoice content first.');
        return;
      }

      extractInvoiceBtn.disabled = true;
      extractInvoiceBtn.textContent = 'Extracting Medicine Details...';

      try {
        let res;
        if (hasFile && !text.includes('|') && !text.includes(',')) {
          const formData = new FormData();
          formData.append('file', invoiceFileInput.files[0]);
          res = await API.extractInvoiceFile(currentPharmacyId, formData);
        } else {
          const fileName = hasFile ? invoiceFileInput.files[0].name : 'purchase_invoice.txt';
          res = await API.extractInvoiceRaw(currentPharmacyId, text, fileName);
        }

        if (res && res.data) {
          currentExtractedData = res.data;
          renderInvoicePreview(currentExtractedData);
        }
      } catch (err) {
        alert('Extraction failed: ' + err.message);
      } finally {
        extractInvoiceBtn.disabled = false;
        extractInvoiceBtn.textContent = '🔍 Extract & Preview Invoice Items →';
      }
    });
  }

  function renderInvoicePreview(data) {
    invoicePreviewCard.style.display = 'block';
    invoiceResultCard.style.display = 'none';

    previewInvoiceNum.value = data.invoiceNumber || ('INV-' + Math.floor(Math.random() * 1000000));
    previewSupplierName.value = data.supplierName || 'SunPharma Healthcare Wholesale Hub';

    if (data.duplicateDetected) {
      duplicateInvoiceAlert.style.display = 'block';
      duplicateInvoiceAlert.textContent = data.duplicateMessage || '⚠️ Warning: This invoice number was already confirmed for this pharmacy!';
    } else {
      duplicateInvoiceAlert.style.display = 'none';
    }

    renderInvoicePreviewRows(data.items || []);
    recalculateInvoicePreviewTotals();
    invoicePreviewCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  function renderInvoicePreviewRows(items) {
    invoicePreviewTableBody.innerHTML = '';
    if (items.length === 0) {
      addInvoicePreviewRow();
      return;
    }
    items.forEach(item => addInvoicePreviewRow(item));
  }

  function addInvoicePreviewRow(item = {}) {
    const tr = document.createElement('tr');
    tr.className = 'invoice-preview-row';
    const curStock = (typeof item.currentStock === 'number') ? item.currentStock : 0;
    const qty = item.quantity || 50;

    tr.innerHTML = `
      <td>
        <input type="text" class="form-input inp-med-name" value="${escapeHtml(item.medicineName || '')}" placeholder="Medicine name" required style="font-weight: 700; font-size: 0.85rem;">
      </td>
      <td>
        <input type="text" class="form-input inp-brand" value="${escapeHtml(item.brand || 'Generic Care')}" placeholder="Manufacturer" style="font-size: 0.82rem;">
      </td>
      <td>
        <input type="text" class="form-input inp-strength" value="${escapeHtml(item.strength || 'Standard')}" placeholder="Strength" style="font-size: 0.82rem;">
      </td>
      <td>
        <input type="number" class="form-input inp-qty" min="1" max="9999" value="${qty}" style="font-weight: 800; color: #059669; font-size: 0.88rem;">
      </td>
      <td>
        <input type="number" class="form-input inp-price" step="0.5" min="1" value="${item.price || 35.00}" style="font-size: 0.85rem;">
      </td>
      <td>
        <input type="text" class="form-input inp-batch" value="${escapeHtml(item.batchNumber || ('BAT-' + Math.floor(Math.random() * 10000)))}" style="font-family: monospace; font-size: 0.82rem;">
      </td>
      <td>
        <input type="date" class="form-input inp-expiry" value="${item.expiryDate || '2028-06-30'}" style="font-size: 0.82rem;">
      </td>
      <td>
        <div style="font-size: 0.75rem; line-height: 1.4;">
          <span style="color: var(--text-muted);">Current: <strong class="lbl-current-stock">${curStock}</strong></span><br>
          <span style="color: #059669; font-weight: 800;">New: <strong class="lbl-new-stock">${curStock + qty}</strong></span>
        </div>
      </td>
      <td style="text-align: center;">
        <button type="button" class="btn-del-preview-row" style="background: none; border: none; color: #ef4444; font-size: 1.2rem; cursor: pointer;" title="Remove Row">&times;</button>
      </td>
    `;

    invoicePreviewTableBody.appendChild(tr);

    const qtyInp = tr.querySelector('.inp-qty');
    const priceInp = tr.querySelector('.inp-price');
    const newStockLbl = tr.querySelector('.lbl-new-stock');
    const delBtn = tr.querySelector('.btn-del-preview-row');

    qtyInp.addEventListener('input', () => {
      const q = parseInt(qtyInp.value, 10) || 0;
      newStockLbl.textContent = (curStock + q);
      recalculateInvoicePreviewTotals();
    });

    priceInp.addEventListener('input', () => {
      recalculateInvoicePreviewTotals();
    });

    delBtn.addEventListener('click', () => {
      if (invoicePreviewTableBody.children.length > 1) {
        tr.remove();
        recalculateInvoicePreviewTotals();
      } else {
        alert('Invoice must contain at least one medicine row.');
      }
    });
  }

  function recalculateInvoicePreviewTotals() {
    let totalItems = 0;
    let totalQty = 0;
    let totalVal = 0;

    invoicePreviewTableBody.querySelectorAll('tr').forEach(tr => {
      const name = tr.querySelector('.inp-med-name').value.trim();
      const q = parseInt(tr.querySelector('.inp-qty').value, 10) || 0;
      const p = parseFloat(tr.querySelector('.inp-price').value) || 0;

      if (name) {
        totalItems++;
        totalQty += q;
        totalVal += (q * p);
      }
    });

    if (previewTotalItemsCount) previewTotalItemsCount.textContent = totalItems;
    if (previewTotalQtyCount) previewTotalQtyCount.textContent = totalQty;
    if (previewTotalValDisplay) previewTotalValDisplay.textContent = `₹${totalVal.toFixed(2)}`;
  }

  if (addInvoicePreviewRowBtn) {
    addInvoicePreviewRowBtn.addEventListener('click', () => {
      addInvoicePreviewRow({ medicineName: '', quantity: 50, price: 40.00 });
      recalculateInvoicePreviewTotals();
    });
  }

  if (cancelInvoicePreviewBtn) {
    cancelInvoicePreviewBtn.addEventListener('click', () => {
      if (confirm('Discard preview and cancel invoice import?')) {
        invoicePreviewCard.style.display = 'none';
        currentExtractedData = null;
      }
    });
  }

  // STAGE 2: CONFIRM & UPDATE STOCK (COMMITS TO DATABASE TRANSACTIONALLY)
  if (confirmInvoiceBtn) {
    confirmInvoiceBtn.addEventListener('click', async () => {
      const rows = invoicePreviewTableBody.querySelectorAll('tr');
      const items = [];

      for (const tr of rows) {
        const name = tr.querySelector('.inp-med-name').value.trim();
        if (!name) continue;

        const brand = tr.querySelector('.inp-brand').value.trim() || 'Generic Care';
        const strength = tr.querySelector('.inp-strength').value.trim() || 'Standard';
        const qty = parseInt(tr.querySelector('.inp-qty').value, 10) || 10;
        const price = parseFloat(tr.querySelector('.inp-price').value) || 30.00;
        const batch = tr.querySelector('.inp-batch').value.trim() || ('BAT-' + Math.floor(Math.random() * 10000));
        const expiry = tr.querySelector('.inp-expiry').value || '2028-06-30';

        items.push({
          medicineName: name,
          brand: brand,
          category: 'Tablets',
          strength: strength,
          quantity: qty,
          price: price,
          batchNumber: batch,
          expiryDate: expiry
        });
      }

      if (items.length === 0) {
        alert('Please ensure at least one valid medicine row exists.');
        return;
      }

      const invoiceNum = previewInvoiceNum.value.trim() || ('INV-' + Math.floor(Math.random() * 1000000));
      const supplier = previewSupplierName.value.trim() || 'SunPharma Healthcare Wholesale Hub';
      const fileName = (invoiceFileInput && invoiceFileInput.files[0]) ? invoiceFileInput.files[0].name : 'purchase_invoice.pdf';

      confirmInvoiceBtn.disabled = true;
      confirmInvoiceBtn.textContent = 'Updating Stock in Database...';

      try {
        const payload = {
          invoiceNumber: invoiceNum,
          supplierName: supplier,
          fileName: fileName,
          items: items
        };

        const res = await API.confirmInvoice(currentPharmacyId, payload);
        if (res && res.data) {
          const result = res.data;
          invoicePreviewCard.style.display = 'none';
          invoiceResultCard.style.display = 'block';

          invoiceResultMessage.textContent = result.message;
          invoiceSummaryBadge.textContent = `Invoice: ${result.invoiceNumber} • +${result.totalStockAdded} Units Added`;

          invoiceParsedTableBody.innerHTML = (result.items || []).map(item => `
            <tr>
              <td style="font-weight: 700; color: var(--text-main);">${escapeHtml(item.medicineName)}</td>
              <td><code>${escapeHtml(item.batchNumber || '-')}</code></td>
              <td style="color: var(--text-muted); font-weight: 600;">${item.currentStock} units</td>
              <td style="font-weight: 800; color: #059669;">+${item.quantity}</td>
              <td style="font-weight: 800; color: #059669; font-size: 1.05rem;">${item.newStock} units</td>
              <td>₹${Number(item.price).toFixed(2)}</td>
              <td><span class="status-pill status-available" style="font-size: 0.75rem;">${escapeHtml(item.actionTaken)}</span></td>
            </tr>
          `).join('');

          // Refresh inventory, dashboard counters, and purchase history
          loadInventory();
          loadDashboardMetrics();
          loadPurchaseHistory();

          alert('✓ Stock Successfully Updated!\n' + result.message);
          invoiceResultCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      } catch (err) {
        alert('Failed to update stock: ' + err.message);
      } finally {
        confirmInvoiceBtn.disabled = false;
        confirmInvoiceBtn.textContent = '✓ Confirm & Update Stock';
      }
    });
  }

  // Load Purchase History Table
  async function loadPurchaseHistory() {
    const tbody = document.getElementById('purchaseHistoryTableBody');
    if (!tbody) return;

    try {
      const res = await API.getInvoiceHistory(currentPharmacyId);
      if (res && res.data && res.data.length > 0) {
        tbody.innerHTML = res.data.map(rec => {
          const isReverted = rec.status === 'REVERTED';
          return `
          <tr>
            <td style="font-weight: 800; color: #2563eb;">${escapeHtml(rec.invoiceNumber)}</td>
            <td><strong>${escapeHtml(rec.supplierName || 'SunPharma Wholesale')}</strong></td>
            <td><code style="background: #f1f5f9; padding: 2px 6px; border-radius: 4px;">${escapeHtml(rec.fileName || 'invoice.pdf')}</code></td>
            <td>${rec.itemsCount} SKUs</td>
            <td style="font-weight: 800; color: ${isReverted ? '#94a3b8' : '#059669'};">${isReverted ? '0' : '+' + rec.totalStockAdded} units</td>
            <td style="font-weight: 700;">₹${Number(rec.totalValue || 0).toFixed(2)}</td>
            <td style="font-size: 0.82rem; color: var(--text-muted);">${rec.uploadedAt ? new Date(rec.uploadedAt).toLocaleString() : '-'}</td>
            <td><span class="status-pill ${isReverted ? 'status-cancelled' : 'status-available'}">${escapeHtml(rec.status || 'CONFIRMED')}</span></td>
            <td>
              ${isReverted 
                ? '<span style="font-size: 0.78rem; color: var(--text-muted); font-weight: 600;">Reverted</span>'
                : `<button type="button" class="btn-revert-invoice" data-invoice="${escapeHtml(rec.invoiceNumber)}" style="padding: 0.3rem 0.65rem; font-size: 0.78rem; font-weight: 700; color: #dc2626; border: 1px solid #fca5a5; background: #fff5f5; border-radius: 6px; cursor: pointer; transition: all 0.2s;">↩ Revert</button>`
              }
            </td>
          </tr>
        `;
        }).join('');

        // Wire up Revert buttons
        tbody.querySelectorAll('.btn-revert-invoice').forEach(btn => {
          btn.addEventListener('click', async () => {
            const invNum = btn.getAttribute('data-invoice');
            btn.disabled = true;
            btn.textContent = 'Checking...';
            try {
              const previewRes = await API.previewInvoiceReversal(currentPharmacyId, invNum);
              const p = previewRes && previewRes.data ? previewRes.data : {};
              let prompt = `⚠️ SAFE STOCK REVERSAL FOR INVOICE #${invNum}\n\n`;
              prompt += `${p.message || 'Ready for reversal.'}\n`;
              prompt += `• Total Stock to Deduct: ${p.totalStockToRevert || 0} units\n`;
              prompt += `• Unique SKUs Affected: ${p.itemsCount || 0}\n\n`;
              if (p.affectedItems && p.affectedItems.length > 0) {
                prompt += `Item Breakdown:\n`;
                p.affectedItems.forEach(it => {
                  prompt += ` - ${it.medicineName}: Deduct ${it.quantityToDeduct} (Stock: ${it.currentStock} -> ${it.projectedStock})\n`;
                });
              }
              prompt += `\nConfirm deducting these units and creating an immutable reversal audit record?`;

              if (confirm(prompt)) {
                btn.textContent = 'Reverting...';
                const confRes = await API.confirmInvoiceReversal(currentPharmacyId, invNum);
                alert(`✓ Reversal Successful!\n${confRes && confRes.message ? confRes.message : 'Stock successfully reverted.'}`);
                loadPurchaseHistory();
                loadInventory();
                loadDashboardMetrics();
              }
            } catch (err) {
              alert('Reversal failed: ' + err.message);
            } finally {
              btn.disabled = false;
              btn.textContent = '↩ Revert';
            }
          });
        });

      } else {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No purchase invoices uploaded yet. Upload an invoice above to populate history.</td></tr>`;
      }
    } catch (err) {
      console.warn('Purchase history fetch warning:', err);
    }
  }

  const refreshInvoiceHistoryBtn = document.getElementById('refreshInvoiceHistoryBtn');
  if (refreshInvoiceHistoryBtn) {
    refreshInvoiceHistoryBtn.addEventListener('click', loadPurchaseHistory);
  }

  // =============================================================
  // MODULE 2: PHARMACY POS COUNTER BILLING (STOCK DEDUCTED ON SAVE)
  // =============================================================
  let currentStoreInventory = [];
  let lastSavedBill = null;

  const billCustName = document.getElementById('billCustName');
  const billCustPhone = document.getElementById('billCustPhone');
  const billItemsTableBody = document.getElementById('billItemsTableBody');
  const addBillRowBtn = document.getElementById('addBillRowBtn');
  const billSubtotalDisplay = document.getElementById('billSubtotalDisplay');
  const billTaxDisplay = document.getElementById('billTaxDisplay');
  const billDiscountInput = document.getElementById('billDiscountInput');
  const billPaymentModeSelect = document.getElementById('billPaymentModeSelect');
  const billGrandTotalDisplay = document.getElementById('billGrandTotalDisplay');
  const saveBillBtn = document.getElementById('saveBillBtn');
  const printBillBtn = document.getElementById('printBillBtn');
  const recentBillsTableBody = document.getElementById('recentBillsTableBody');

  async function setupBillingInventoryDropdowns() {
    try {
      const res = await API.getPharmacyInventory(currentPharmacyId);
      if (res && res.data) {
        currentStoreInventory = res.data;
        if (billItemsTableBody && billItemsTableBody.children.length === 0) {
          addBillLineRow();
        } else {
          refreshAllBillRowDropdowns();
        }
      }
    } catch (err) {
      console.warn('Could not load inventory for billing', err);
    }
  }

  function addBillLineRow() {
    if (!billItemsTableBody) return;
    const rowId = 'billRow_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4);
    const tr = document.createElement('tr');
    tr.id = rowId;

    const optionsHtml = currentStoreInventory.map(item => {
      const isOut = (item.quantity <= 0);
      return `<option value="${item.inventoryId}" data-price="${item.price}" data-stock="${item.quantity}" data-name="${escapeHtml(item.medicineName)}" data-batch="${escapeHtml(item.batchNumber || '')}" ${isOut ? 'disabled' : ''}>
        ${escapeHtml(item.medicineName)} (In Stock: ${item.quantity} | ₹${item.price}) ${isOut ? '[OUT OF STOCK]' : ''}
      </option>`;
    }).join('');

    tr.innerHTML = `
      <td>
        <select class="form-select bill-item-select" style="font-size: 0.88rem;">
          <option value="">-- Choose Medicine from Stock --</option>
          ${optionsHtml}
        </select>
      </td>
      <td>
        <input type="number" class="form-input bill-item-qty" min="1" max="999" value="1" style="font-size: 0.88rem;">
        <span class="stock-hint" style="font-size: 0.72rem; color: var(--text-muted); display: block; margin-top: 2px;"></span>
      </td>
      <td>
        <input type="number" class="form-input bill-item-price" step="0.01" value="0.00" readonly style="background: #f8fafc; font-size: 0.88rem;">
      </td>
      <td style="font-weight: 700; color: var(--text-main); font-size: 0.92rem;">
        <span class="bill-item-row-total">₹0.00</span>
      </td>
      <td style="text-align: center;">
        <button type="button" class="btn-delete-row" style="background: none; border: none; color: #ef4444; font-size: 1.1rem; cursor: pointer;" title="Remove Item">&times;</button>
      </td>
    `;

    billItemsTableBody.appendChild(tr);

    const select = tr.querySelector('.bill-item-select');
    const qtyInput = tr.querySelector('.bill-item-qty');
    const priceInput = tr.querySelector('.bill-item-price');
    const stockHint = tr.querySelector('.stock-hint');
    const totalSpan = tr.querySelector('.bill-item-row-total');
    const delBtn = tr.querySelector('.btn-delete-row');

    select.addEventListener('change', () => {
      const opt = select.options[select.selectedIndex];
      if (opt && opt.value) {
        const price = parseFloat(opt.getAttribute('data-price')) || 0;
        const stock = parseInt(opt.getAttribute('data-stock'), 10) || 0;
        priceInput.value = price.toFixed(2);
        qtyInput.max = stock;
        stockHint.textContent = `Max: ${stock} in stock`;
        recalcRowTotal();
      } else {
        priceInput.value = '0.00';
        stockHint.textContent = '';
        totalSpan.textContent = '₹0.00';
      }
      recalculateBillTotal();
    });

    qtyInput.addEventListener('input', () => {
      const opt = select.options[select.selectedIndex];
      const maxStock = opt ? (parseInt(opt.getAttribute('data-stock'), 10) || 0) : 999;
      if (parseInt(qtyInput.value, 10) > maxStock) {
        qtyInput.value = maxStock;
        alert(`Cannot exceed available stock of ${maxStock} units.`);
      }
      recalcRowTotal();
      recalculateBillTotal();
    });

    function recalcRowTotal() {
      const q = parseInt(qtyInput.value, 10) || 0;
      const p = parseFloat(priceInput.value) || 0;
      totalSpan.textContent = `₹${(q * p).toFixed(2)}`;
    }

    delBtn.addEventListener('click', () => {
      if (billItemsTableBody.children.length > 1) {
        tr.remove();
        recalculateBillTotal();
      } else {
        alert('Bill must contain at least one medicine item.');
      }
    });

    // Auto-select first available medicine if default
    if (select.options.length > 1) {
      select.selectedIndex = 1;
      select.dispatchEvent(new Event('change'));
    }
  }

  function refreshAllBillRowDropdowns() {
    if (!billItemsTableBody) return;
    billItemsTableBody.querySelectorAll('tr').forEach(tr => {
      const select = tr.querySelector('.bill-item-select');
      const curVal = select.value;
      const optionsHtml = currentStoreInventory.map(item => {
        const isOut = (item.quantity <= 0);
        return `<option value="${item.inventoryId}" data-price="${item.price}" data-stock="${item.quantity}" data-name="${escapeHtml(item.medicineName)}" data-batch="${escapeHtml(item.batchNumber || '')}" ${isOut ? 'disabled' : ''}>
          ${escapeHtml(item.medicineName)} (In Stock: ${item.quantity} | ₹${item.price}) ${isOut ? '[OUT OF STOCK]' : ''}
        </option>`;
      }).join('');
      select.innerHTML = `<option value="">-- Choose Medicine from Stock --</option>` + optionsHtml;
      select.value = curVal;
    });
  }

  function recalculateBillTotal() {
    if (!billItemsTableBody) return;
    let subtotal = 0;

    billItemsTableBody.querySelectorAll('tr').forEach(tr => {
      const qtyInput = tr.querySelector('.bill-item-qty');
      const priceInput = tr.querySelector('.bill-item-price');
      const q = parseInt(qtyInput ? qtyInput.value : 0, 10) || 0;
      const p = parseFloat(priceInput ? priceInput.value : 0) || 0;
      subtotal += (q * p);
    });

    const tax = subtotal * 0.05; // 5% Healthcare GST
    const discount = parseFloat(billDiscountInput ? billDiscountInput.value : 0) || 0;
    const grand = Math.max(0, (subtotal + tax - discount));

    if (billSubtotalDisplay) billSubtotalDisplay.textContent = `₹${subtotal.toFixed(2)}`;
    if (billTaxDisplay) billTaxDisplay.textContent = `₹${tax.toFixed(2)}`;
    if (billGrandTotalDisplay) billGrandTotalDisplay.textContent = `₹${grand.toFixed(2)}`;
  }

  if (addBillRowBtn) {
    addBillRowBtn.addEventListener('click', () => {
      addBillLineRow();
    });
  }

  if (billDiscountInput) {
    billDiscountInput.addEventListener('input', () => {
      recalculateBillTotal();
    });
  }

  // SAVE BILL: ATOMICALLY DEDUCTS MEDICINE STOCK IN DATABASE
  if (saveBillBtn) {
    saveBillBtn.addEventListener('click', async () => {
      const rows = billItemsTableBody.querySelectorAll('tr');
      const items = [];

      for (const tr of rows) {
        const select = tr.querySelector('.bill-item-select');
        const opt = select.options[select.selectedIndex];
        if (!opt || !opt.value) continue;

        const inventoryId = parseInt(opt.value, 10);
        const name = opt.getAttribute('data-name');
        const batch = opt.getAttribute('data-batch') || '';
        const qty = parseInt(tr.querySelector('.bill-item-qty').value, 10) || 1;
        const price = parseFloat(tr.querySelector('.bill-item-price').value) || 0;

        items.push({
          inventoryId: inventoryId,
          medicineName: name,
          batchNumber: batch,
          quantity: qty,
          unitPrice: price
        });
      }

      if (items.length === 0) {
        alert('Please choose at least one valid medicine to generate bill.');
        return;
      }

      const payload = {
        pharmacyId: currentPharmacyId,
        customerName: billCustName.value.trim() || 'Walk-in Customer',
        customerPhone: billCustPhone.value.trim() || 'N/A',
        discountAmount: parseFloat(billDiscountInput.value) || 0,
        paymentMode: billPaymentModeSelect.value,
        items: items
      };

      saveBillBtn.disabled = true;
      saveBillBtn.textContent = 'Saving Bill & Deducting Stock...';

      try {
        const res = await API.createBill(payload);
        if (res && res.data) {
          lastSavedBill = res.data;
          alert(`✓ Bill ${lastSavedBill.billNumber} Saved Successfully!\nMedicine stock has been deducted from your inventory.`);

          if (printBillBtn) {
            printBillBtn.disabled = false;
            printBillBtn.classList.remove('btn-outline-primary');
            printBillBtn.classList.add('btn-primary');
          }
          if (downloadPdfBillBtn) {
            downloadPdfBillBtn.disabled = false;
            downloadPdfBillBtn.classList.remove('btn-outline-primary');
            downloadPdfBillBtn.classList.add('btn-primary');
          }

          // Refresh inventory and dashboard metrics!
          await setupBillingInventoryDropdowns();
          loadInventory();
          loadDashboardMetrics();
          loadRecentBills();
        }
      } catch (err) {
        alert('Error saving bill: ' + err.message);
      } finally {
        saveBillBtn.disabled = false;
        saveBillBtn.textContent = '💾 Save Bill & Deduct Stock';
      }
    });
  }

  // PRINT BILL RECEIPT
  if (printBillBtn) {
    printBillBtn.addEventListener('click', () => {
      if (!lastSavedBill) {
        alert('Please save the bill first before printing.');
        return;
      }
      renderAndPrintBillReceipt(lastSavedBill);
    });
  }

  const downloadPdfBillBtn = document.getElementById('downloadPdfBillBtn');
  if (downloadPdfBillBtn) {
    downloadPdfBillBtn.addEventListener('click', () => {
      if (!lastSavedBill) {
        alert('Please save the bill first.');
        return;
      }
      const originalTitle = document.title;
      document.title = `${lastSavedBill.billNumber}_Invoice.pdf`;
      renderAndPrintBillReceipt(lastSavedBill);
      setTimeout(() => { document.title = originalTitle; }, 1000);
    });
  }

  function renderAndPrintBillReceipt(bill) {
    const printSection = document.getElementById('billPrintSection');
    if (!printSection) return;

    document.getElementById('printPharmacyName').textContent = bill.pharmacyName || 'PharmaTech Partner Pharmacy';
    document.getElementById('printPharmacyAddress').textContent = bill.pharmacyAddress || 'Bangalore';
    document.getElementById('printPharmacyLicense').textContent = bill.pharmacyLicense || 'DL-KA-2024-984321';
    document.getElementById('printBillNumber').textContent = bill.billNumber;
    document.getElementById('printBillDate').textContent = bill.createdAt ? new Date(bill.createdAt).toLocaleString() : new Date().toLocaleString();
    document.getElementById('printCustName').textContent = bill.customerName || 'Walk-in Customer';
    document.getElementById('printCustPhone').textContent = bill.customerPhone || '-';

    const tbody = document.getElementById('printBillItemsTbody');
    tbody.innerHTML = (bill.items || []).map(item => `
      <tr>
        <td style="padding: 4px 0;">${escapeHtml(item.medicineName)}</td>
        <td style="text-align: center; padding: 4px 0;">${escapeHtml(item.batchNumber || '-')}</td>
        <td style="text-align: right; padding: 4px 0;">${item.quantity}</td>
        <td style="text-align: right; padding: 4px 0;">₹${Number(item.unitPrice).toFixed(2)}</td>
        <td style="text-align: right; padding: 4px 0;">₹${Number(item.totalPrice).toFixed(2)}</td>
      </tr>
    `).join('');

    document.getElementById('printSubtotal').textContent = `₹${Number(bill.subtotal).toFixed(2)}`;
    document.getElementById('printTax').textContent = `₹${Number(bill.taxAmount).toFixed(2)}`;
    document.getElementById('printGrandTotal').textContent = `₹${Number(bill.grandTotal).toFixed(2)}`;
    document.getElementById('printPaymentMode').textContent = bill.paymentMode || 'CASH';

    printSection.style.display = 'block';
    window.print();
  }

  async function loadRecentBills() {
    if (!recentBillsTableBody) return;
    try {
      const res = await API.getBillingHistory(currentPharmacyId);
      if (res && res.data && res.data.length > 0) {
        recentBillsTableBody.innerHTML = res.data.slice(0, 10).map(bill => `
          <tr>
            <td style="font-weight: 800; color: var(--primary-dark);">${bill.billNumber}</td>
            <td>${escapeHtml(bill.customerName)}<br><span style="font-size: 0.75rem; color: var(--text-muted);">${escapeHtml(bill.customerPhone)}</span></td>
            <td>${(bill.items || []).length} items</td>
            <td style="font-weight: 800; color: var(--text-main);">₹${Number(bill.grandTotal).toFixed(2)}</td>
            <td><span class="status-pill status-available" style="font-size: 0.75rem;">${bill.paymentMode}</span></td>
            <td>
              <span class="sidebar-badge" style="background: ${bill.status === 'CANCELLED' ? '#fee2e2' : '#dcfce7'}; color: ${bill.status === 'CANCELLED' ? '#b91c1c' : '#166534'};">
                ${bill.status || 'PAID'}
              </span>
            </td>
            <td style="font-size: 0.82rem; color: var(--text-muted);">${bill.createdAt ? new Date(bill.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : '-'}</td>
            <td>
              <button class="btn btn-outline-primary btn-reprint-bill" data-id="${bill.id}" style="font-size: 0.75rem; padding: 0.25rem 0.65rem;">
                🖨️ Print
              </button>
            </td>
          </tr>
        `).join('');

        recentBillsTableBody.querySelectorAll('.btn-reprint-bill').forEach(btn => {
          btn.addEventListener('click', async () => {
            const billId = btn.getAttribute('data-id');
            try {
              const bRes = await API.getBillDetails(billId);
              if (bRes && bRes.data) {
                lastSavedBill = bRes.data;
                renderAndPrintBillReceipt(bRes.data);
              }
            } catch (e) {
              alert('Could not fetch bill details: ' + e.message);
            }
          });
        });
      } else {
        recentBillsTableBody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No bills recorded yet. Generate your first counter bill above!</td></tr>`;
      }
    } catch (err) {
      console.warn('Recent bills fetch warning', err);
    }
  }

  // Dedicated Sales History Section with Cancel/Refund Stock Reversal
  async function loadSalesHistory() {
    const tbody = document.getElementById('salesHistoryTableBody');
    if (!tbody) return;

    try {
      const res = await API.getBillingHistory(currentPharmacyId);
      if (res && res.data && res.data.length > 0) {
        tbody.innerHTML = res.data.map(bill => {
          const isCancelled = (bill.status === 'CANCELLED');
          return `
            <tr>
              <td style="font-weight: 800; color: var(--primary-dark);">${bill.billNumber}</td>
              <td><strong>${escapeHtml(bill.customerName)}</strong></td>
              <td>${escapeHtml(bill.customerPhone)}</td>
              <td>₹${Number(bill.subtotal).toFixed(2)}</td>
              <td>₹${Number(bill.taxAmount).toFixed(2)}</td>
              <td style="font-weight: 800; color: var(--text-main);">₹${Number(bill.grandTotal).toFixed(2)}</td>
              <td>
                <span class="sidebar-badge" style="background: ${isCancelled ? '#fee2e2' : '#dcfce7'}; color: ${isCancelled ? '#b91c1c' : '#166534'};">
                  ${isCancelled ? 'CANCELLED / REFUNDED' : 'PAID & DEDUCTED'}
                </span>
              </td>
              <td style="font-size: 0.82rem; color: var(--text-muted);">${bill.createdAt ? new Date(bill.createdAt).toLocaleString() : '-'}</td>
              <td style="display: flex; gap: 0.4rem;">
                <button class="btn btn-outline-primary btn-reprint-sale" data-id="${bill.id}" style="font-size: 0.75rem; padding: 0.25rem 0.65rem;" title="Reprint Bill">
                  🖨️ Reprint
                </button>
                ${!isCancelled ? `
                  <button class="btn btn-outline-primary btn-cancel-sale" data-id="${bill.id}" data-num="${bill.billNumber}" style="font-size: 0.75rem; padding: 0.25rem 0.65rem; color: #dc2626; border-color: #fca5a5;" title="Cancel bill and restore inventory stock">
                    ↩ Cancel & Refund
                  </button>
                ` : `
                  <span style="font-size: 0.72rem; color: var(--text-muted); padding: 0.25rem;">Stock Restored</span>
                `}
              </td>
            </tr>
          `;
        }).join('');

        tbody.querySelectorAll('.btn-reprint-sale').forEach(btn => {
          btn.addEventListener('click', async () => {
            const billId = btn.getAttribute('data-id');
            try {
              const bRes = await API.getBillDetails(billId);
              if (bRes && bRes.data) {
                lastSavedBill = bRes.data;
                renderAndPrintBillReceipt(bRes.data);
              }
            } catch (e) {
              alert('Could not fetch bill details: ' + e.message);
            }
          });
        });

        tbody.querySelectorAll('.btn-cancel-sale').forEach(btn => {
          btn.addEventListener('click', async () => {
            const billId = btn.getAttribute('data-id');
            const billNum = btn.getAttribute('data-num');
            if (confirm(`Are you sure you want to cancel Bill #${billNum}?\n\nThis will reverse the transaction and restore all billed medicine quantities back to your inventory.`)) {
              try {
                const cRes = await API.cancelBill(billId);
                alert(`✓ Bill #${billNum} has been cancelled!\nMedicine quantities have been restored to your inventory.`);
                loadSalesHistory();
                loadRecentBills();
                loadInventory();
                loadDashboardMetrics();
                setupBillingInventoryDropdowns();
              } catch (e) {
                alert('Cancellation failed: ' + e.message);
              }
            }
          });
        });
      } else {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--text-muted); padding: 1.5rem;">No sales records found in history.</td></tr>`;
      }
    } catch (err) {
      console.warn('Sales history fetch warning:', err);
    }
  }

  const refreshSalesHistoryBtn = document.getElementById('refreshSalesHistoryBtn');
  if (refreshSalesHistoryBtn) {
    refreshSalesHistoryBtn.addEventListener('click', loadSalesHistory);
  }

  // Load Reports & Stock Analytics
  async function loadReports() {
    try {
      const invRes = await API.getPharmacyInventory(currentPharmacyId);
      const items = invRes && invRes.data ? invRes.data : [];

      let totalUnits = 0;
      let totalValuation = 0;
      const lowStockItems = [];

      items.forEach(item => {
        const q = item.quantity || 0;
        const p = item.price || 0;
        totalUnits += q;
        totalValuation += (q * p);
        if (q <= 15) {
          lowStockItems.push(item);
        }
      });

      document.getElementById('reportTotalUnits').textContent = totalUnits;
      document.getElementById('reportInventoryValuation').textContent = `₹${totalValuation.toFixed(2)}`;
      document.getElementById('reportLowStockCount').textContent = lowStockItems.length;

      // Sales total
      const billRes = await API.getBillingHistory(currentPharmacyId);
      const bills = billRes && billRes.data ? billRes.data : [];
      const totalSales = bills
        .filter(b => b.status !== 'CANCELLED')
        .reduce((sum, b) => sum + (parseFloat(b.grandTotal) || 0), 0);
      document.getElementById('reportTotalSales').textContent = `₹${totalSales.toFixed(2)}`;

      // Render low-stock table
      const lowStockTbody = document.getElementById('lowStockAlertTableBody');
      if (lowStockTbody) {
        if (lowStockItems.length === 0) {
          lowStockTbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #166534; padding: 1.5rem; background: #f0fdf4;">✓ Excellent! All medicines in inventory have healthy stock levels (> 15 units).</td></tr>`;
        } else {
          lowStockTbody.innerHTML = lowStockItems.map(item => `
            <tr>
              <td><strong>${escapeHtml(item.medicineName)}</strong></td>
              <td>${escapeHtml(item.category || 'Tablets')}</td>
              <td><strong style="color: #ef4444; font-size: 1.05rem;">${item.quantity} units</strong></td>
              <td><code>${escapeHtml(item.batchNumber || '-')}</code></td>
              <td>₹${Number(item.price).toFixed(2)}</td>
              <td>
                <span class="sidebar-badge" style="background: #fee2e2; color: #b91c1c;">
                  ⚠️ Reorder Required
                </span>
              </td>
            </tr>
          `).join('');
        }
      }
    } catch (err) {
      console.warn('Reports load warning:', err);
    }
  }

  // =============================================================
  // MODULE 3: PHARMACY ORDERS, NOTIFICATIONS & FLEET DISPATCH
  // =============================================================
  let lastAlertedOrderId = null;
  const notifBellBtn = document.getElementById('notifBellBtn');
  const notifDropdown = document.getElementById('pharmacyNotifDropdown');
  const notifBadge = document.getElementById('pharmacyNotifBadge');
  const notifList = document.getElementById('pharmacyNotifList');
  const notifDropdownCount = document.getElementById('notifDropdownCount');
  const newOrderAlertModal = document.getElementById('newOrderAlertModal');
  const closeAlertModalBtn = document.getElementById('closeNewOrderAlertModalBtn');
  const dismissAlertModalBtn = document.getElementById('dismissAlertModalBtn');
  const modalAlertPrepareBtn = document.getElementById('modalAlertPrepareBtn');
  let currentModalOrderId = null;

  if (notifBellBtn && notifDropdown) {
    notifBellBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      const isOpen = notifDropdown.style.display === 'block';
      notifDropdown.style.display = isOpen ? 'none' : 'block';
    });
    document.addEventListener('click', (e) => {
      if (notifDropdown && !notifDropdown.contains(e.target) && e.target !== notifBellBtn) {
        notifDropdown.style.display = 'none';
      }
    });
  }

  if (closeAlertModalBtn && newOrderAlertModal) {
    closeAlertModalBtn.addEventListener('click', () => {
      newOrderAlertModal.classList.remove('open');
    });
  }
  if (dismissAlertModalBtn && newOrderAlertModal) {
    dismissAlertModalBtn.addEventListener('click', () => {
      newOrderAlertModal.classList.remove('open');
    });
  }

  if (modalAlertPrepareBtn) {
    modalAlertPrepareBtn.addEventListener('click', async () => {
      if (!currentModalOrderId) return;
      try {
        modalAlertPrepareBtn.disabled = true;
        modalAlertPrepareBtn.textContent = 'Accepting order...';
        await API.updatePharmacyOrderStatus(currentModalOrderId, 'PHARMACY_ACCEPTED');
        newOrderAlertModal.classList.remove('open');
        loadPharmacyOrders();
      } catch (err) {
        alert('Could not update status: ' + err.message);
      } finally {
        modalAlertPrepareBtn.disabled = false;
        modalAlertPrepareBtn.textContent = '✓ Accept & Start Preparing →';
      }
    });
  }

  function startNotificationPolling() {
    // Initial fetch and poll every 5s
    pollNotifications();
    setInterval(pollNotifications, 5000);
  }

  async function pollNotifications() {
    if (!currentPharmacyId) return;
    try {
      const res = await API.getPharmacyNotifications(currentPharmacyId);
      if (res && res.data) {
        const data = res.data;
        const unreadCount = data.unreadCount || 0;

        if (notifBadge) {
          notifBadge.textContent = unreadCount;
          notifBadge.style.display = unreadCount > 0 ? 'inline-block' : 'none';
        }
        if (notifDropdownCount) {
          notifDropdownCount.textContent = `${unreadCount} unread`;
        }

        const newOrders = data.newOrders || [];
        if (notifList) {
          if (newOrders.length === 0) {
            notifList.innerHTML = `<p style="font-size: 0.85rem; color: #94a3b8; text-align: center; margin: 1rem 0;">No pending order alerts</p>`;
          } else {
            notifList.innerHTML = newOrders.map(o => `
              <div style="padding: 0.75rem; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; ${o.priority === 'EMERGENCY' ? 'background: #fef2f2; border-left: 3px solid #ef4444;' : ''}">
                <div>
                  <div style="display: flex; align-items: center; gap: 0.35rem;">
                    ${o.priority === 'EMERGENCY' ? '<span class="badge-emergency-pulse" style="font-size: 0.65rem; padding: 2px 6px;">EMERGENCY</span>' : ''}
                    <strong style="font-size: 0.85rem; color: #1e3a8a;">${o.orderNumber}</strong>
                  </div>
                  <p style="font-size: 0.78rem; color: #475569; margin: 2px 0;">${escapeHtml(o.customerName)} • ₹${Number(o.totalAmount).toFixed(2)}</p>
                  <small style="color: #94a3b8; font-size: 0.72rem;">${(o.items || []).length} items • ${o.status}</small>
                </div>
                <button class="btn btn-sm btn-primary btn-quick-view-order" data-id="${o.id}" style="font-size: 0.72rem; padding: 0.25rem 0.5rem;">
                  View
                </button>
              </div>
            `).join('');

            notifList.querySelectorAll('.btn-quick-view-order').forEach(btn => {
              btn.addEventListener('click', () => {
                const orderId = btn.getAttribute('data-id');
                const ord = newOrders.find(x => String(x.id) === String(orderId));
                if (ord) showNewOrderAlert(ord);
              });
            });
          }
        }

        // Trigger Instant Alert Modal for the latest unnotified order
        if (newOrders.length > 0) {
          const latest = newOrders[0];
          if (latest.id !== lastAlertedOrderId) {
            lastAlertedOrderId = latest.id;
            showNewOrderAlert(latest);
            loadPharmacyOrders(); // Refresh table too
          }
        }
      }
    } catch (err) {
      console.warn('Notification poll issue:', err);
    }
  }

  function showNewOrderAlert(order) {
    if (!newOrderAlertModal) return;
    currentModalOrderId = order.id;

    const priorityBadge = document.getElementById('modalOrderPriorityBadge');
    if (priorityBadge) {
      if (order.priority === 'EMERGENCY') {
        priorityBadge.className = 'badge-emergency-pulse';
        priorityBadge.innerHTML = '🚨 EMERGENCY MEDICINE PURCHASE';
        priorityBadge.style.display = 'inline-flex';
      } else {
        priorityBadge.className = 'badge-normal-priority';
        priorityBadge.innerHTML = 'Standard Delivery';
        priorityBadge.style.display = 'inline-block';
      }
    }

    const isDemoAlert = order.demo || order.paymentMode === 'DEMO' || order.paymentStatus === 'DEMO_PAID';
    document.getElementById('modalAlertOrderNumber').innerHTML = `${escapeHtml(order.orderNumber)} ${isDemoAlert ? '<span style="background: #fef3c7; color: #92400e; font-size: 0.68rem; font-weight: 800; padding: 2px 6px; border-radius: 4px; border: 1px solid #fde68a;">🎓 DEMO ORDER</span>' : ''}`;
    document.getElementById('modalAlertCustomerName').textContent = order.customerName || 'Customer';
    document.getElementById('modalAlertCustomerPhone').textContent = order.customerPhone || '-';
    document.getElementById('modalAlertDeliveryAddress').textContent = order.deliveryAddress || 'Address on file';
    document.getElementById('modalAlertTotalAmount').textContent = `₹${Number(order.totalAmount).toFixed(2)} (${order.paymentMethod || 'PAY_ON_DELIVERY'})`;

    const itemsContainer = document.getElementById('modalAlertItemsList');
    if (itemsContainer) {
      const items = order.items || [];
      if (items.length === 0) {
        itemsContainer.innerHTML = `<p style="color: #94a3b8; font-size: 0.85rem;">Prescription medicine package</p>`;
      } else {
        itemsContainer.innerHTML = items.map(i => `
          <div style="display: flex; justify-content: space-between; padding: 0.35rem 0; border-bottom: 1px dashed #f1f5f9; font-size: 0.85rem;">
            <span><strong>${escapeHtml(i.medicineName)}</strong> <span style="color: #64748b;">(${escapeHtml(i.strength || '')})</span></span>
            <strong>x${i.quantity}</strong>
          </div>
        `).join('');
      }
    }

    newOrderAlertModal.classList.add('open');
  }

  async function loadPharmacyOrders() {
    const tbody = document.getElementById('pharmacyOrdersTableBody');
    if (!tbody || !currentPharmacyId) return;

    try {
      const res = await API.getPharmacyOrders(currentPharmacyId);
      if (res && res.data && res.data.length > 0) {
        tbody.innerHTML = res.data.map(order => {
          const isEmergency = (order.priority === 'EMERGENCY');
          const priorityCell = isEmergency
            ? `<span class="badge-emergency-pulse">🚨 EMERGENCY</span>`
            : `<span class="badge-normal-priority">Normal</span>`;

          const itemsText = (order.items || []).map(i => `• ${escapeHtml(i.medicineName)} (x${i.quantity})`).join('<br>') || 'Medicines';

          const riderCell = order.deliveryPartnerName
            ? `<strong>${escapeHtml(order.deliveryPartnerName)}</strong><br><span style="font-size: 0.75rem; color: #2563eb;">🛵 ${escapeHtml(order.vehicleNumber || '')}</span><br><span style="font-size: 0.72rem; color: #64748b;">📞 ${escapeHtml(order.deliveryPartnerPhone || '')}</span>`
            : `<span style="color: #94a3b8; font-size: 0.78rem;">Looking for nearest partner...</span>`;

          let statusBadgeClass = 'status-available';
          let statusLabel = order.status;
          if (order.status === 'ORDER_PLACED') { statusLabel = 'New Order Placed'; statusBadgeClass = 'status-low'; }
          else if (order.status === 'PHARMACY_ACCEPTED') { statusLabel = 'Pharmacy Accepted'; statusBadgeClass = 'status-available'; }
          else if (order.status === 'PREPARING') { statusLabel = 'Preparing Medicines'; statusBadgeClass = 'status-low'; }
          else if (order.status === 'READY_FOR_PICKUP') { statusLabel = 'Ready for Pickup'; statusBadgeClass = 'status-available'; }
          else if (order.status === 'PARTNER_ASSIGNED') { statusLabel = 'Partner Assigned'; statusBadgeClass = 'status-available'; }
          else if (order.status === 'PICKED_UP' || order.status === 'OUT_FOR_DELIVERY') { statusLabel = 'Out with Rider'; statusBadgeClass = 'status-available'; }
          else if (order.status === 'DELIVERED') { statusLabel = 'Delivered ✓'; statusBadgeClass = 'status-available'; }
          else if (order.status === 'CANCELLED') { statusLabel = 'Cancelled'; statusBadgeClass = 'status-low'; }

          let actionHtml = '';
          if (order.status === 'ORDER_PLACED') {
            actionHtml = `
              <div style="display:flex; gap:5px; flex-direction:column;">
                <button class="btn btn-primary btn-accept-order" data-id="${order.id}" style="font-size: 0.75rem; padding: 0.35rem 0.65rem; background: #059669; border: none; color: white; border-radius: 6px; font-weight: 700; cursor: pointer;">✓ Accept Order</button>
                <button class="btn btn-reject-order" data-id="${order.id}" style="font-size: 0.72rem; padding: 0.3rem 0.5rem; background: #fee2e2; color: #b91c1c; border: 1px solid #fecaca; border-radius: 6px; font-weight: 600; cursor: pointer;">✕ Reject Order</button>
              </div>`;
          } else if (order.status === 'PHARMACY_ACCEPTED' || order.status === 'PREPARING') {
            actionHtml = `<button class="btn btn-mark-ready" data-id="${order.id}" style="background: #2563eb; color: white; border: none; font-size: 0.75rem; padding: 0.4rem 0.75rem; border-radius: 6px; font-weight: 700; cursor: pointer;">📦 Ready for Pickup</button>`;
          } else if (order.status === 'READY_FOR_PICKUP') {
            actionHtml = `<span style="color: #0284c7; font-size: 0.78rem; font-weight: 700;">Waiting for Rider</span>`;
          } else if (order.status === 'PARTNER_ASSIGNED') {
            actionHtml = `<span style="color: #10b981; font-size: 0.78rem; font-weight: 700;">Rider Assigned</span>`;
          } else if (order.status === 'PICKED_UP' || order.status === 'OUT_FOR_DELIVERY') {
            actionHtml = `<span style="color: #2563eb; font-size: 0.78rem; font-weight: 700;">Out with Rider</span>`;
          } else if (order.status === 'DELIVERED') {
            actionHtml = `<span style="color: #166534; font-size: 0.78rem; font-weight: 700;">Delivered ✓</span>`;
          } else if (order.status === 'CANCELLED') {
            actionHtml = `<span style="color: #dc2626; font-size: 0.78rem; font-weight: 700;">Cancelled</span>`;
          } else {
            actionHtml = `<span style="color: #64748b; font-size: 0.75rem;">${order.status}</span>`;
          }

          return `
            <tr style="${isEmergency ? 'background: #fff5f5;' : ''}">
              <td>${priorityCell}</td>
              <td>
                <strong style="color: #2563eb; font-size: 0.9rem;">${order.orderNumber}</strong><br>
                ${(order.demo || order.paymentMode === 'DEMO' || order.paymentStatus === 'DEMO_PAID') ? '<span style="background: #fef3c7; color: #92400e; font-size: 0.68rem; font-weight: 800; padding: 2px 5px; border-radius: 4px; border: 1px solid #fde68a; display: inline-block; margin-top: 2px;">🎓 DEMO ORDER</span><br>' : ''}
                <small style="color: #64748b;">${order.createdAt ? new Date(order.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) : '-'}</small>
              </td>
              <td>
                <strong>${escapeHtml(order.customerName)}</strong><br>
                <small style="color: #64748b;">📞 ${escapeHtml(order.customerPhone)}</small><br>
                <span style="font-size: 0.78rem; color: #475569;">📍 ${escapeHtml(order.deliveryAddress)}</span>
              </td>
              <td style="font-size: 0.82rem; line-height: 1.4;">${itemsText}</td>
              <td>
                <strong style="font-size: 0.95rem; color: #0f172a;">₹${Number(order.totalAmount).toFixed(2)}</strong><br>
                <span class="sidebar-badge" style="background: ${(order.paymentStatus === 'DEMO_PAID' || order.demo || order.paymentMode === 'DEMO') ? '#fef3c7; color: #92400e; border: 1px solid #fde68a;' : '#f1f5f9; color: #334155;'} font-size: 0.7rem; padding: 2px 6px;">
                  ${order.paymentMethod || 'PAY_ON_DELIVERY'} • ${order.paymentStatus || 'PENDING'}
                </span>
              </td>
              <td>${riderCell}</td>
              <td><span class="status-pill ${statusBadgeClass}" style="font-size: 0.75rem;">${statusLabel}</span></td>
              <td>${actionHtml}</td>
            </tr>
          `;
        }).join('');

        tbody.querySelectorAll('.btn-accept-order').forEach(btn => {
          btn.addEventListener('click', async () => {
            const orderId = btn.getAttribute('data-id');
            try {
              btn.disabled = true;
              btn.textContent = 'Accepting...';
              await API.updatePharmacyOrderStatus(orderId, 'PHARMACY_ACCEPTED');
              loadPharmacyOrders();
            } catch (e) {
              alert('Could not accept order: ' + e.message);
              btn.disabled = false;
            }
          });
        });

        tbody.querySelectorAll('.btn-reject-order').forEach(btn => {
          btn.addEventListener('click', async () => {
            const orderId = btn.getAttribute('data-id');
            if (!confirm('Reject this medicine order? Inventory stock will be restored immediately.')) return;
            try {
              btn.disabled = true;
              btn.textContent = 'Rejecting...';
              await API.updatePharmacyOrderStatus(orderId, 'CANCELLED');
              loadPharmacyOrders();
            } catch (e) {
              alert('Could not reject order: ' + e.message);
              btn.disabled = false;
            }
          });
        });

        tbody.querySelectorAll('.btn-mark-ready').forEach(btn => {
          btn.addEventListener('click', async () => {
            const orderId = btn.getAttribute('data-id');
            try {
              btn.disabled = true;
              btn.textContent = 'Updating...';
              await API.updatePharmacyOrderStatus(orderId, 'READY_FOR_PICKUP');
              loadPharmacyOrders();
            } catch (e) {
              alert('Could not update order status: ' + e.message);
              btn.disabled = false;
            }
          });
        });
      } else {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">No customer orders currently in queue. Incoming orders will appear here automatically!</td></tr>`;
      }
    } catch (err) {
      console.warn('Pharmacy orders fetch warning', err);
    }
  }

  const refreshOrdersBtn = document.getElementById('refreshPharmacyOrdersBtn');
  if (refreshOrdersBtn) {
    refreshOrdersBtn.addEventListener('click', loadPharmacyOrders);
  }

  // =============================================================
  // MODULE: PHARMACY EARNINGS & SETTLEMENTS
  // =============================================================
  let currentPharmacySettlementsData = [];

  async function loadPharmacyEarnings() {
    if (!currentPharmacyId) return;
    const tbody = document.getElementById('pharmacySettlementsTableBody');
    if (tbody) {
      tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">Loading payments & earnings data...</td></tr>';
    }

    try {
      const res = await API.getPharmacyEarnings(currentPharmacyId);
      if (res && res.data) {
        const d = res.data;
        const totalSales = d.totalSales != null ? d.totalSales : (d.totalDemoRevenue || 0);
        const totalReceived = d.totalReceived != null ? d.totalReceived : (d.totalSettlementsReceived || 0);
        const pending = d.pendingSettlements != null ? d.pendingSettlements : 0;
        const compCount = d.completedSettlementsCount != null ? d.completedSettlementsCount : 0;
        const commission = d.platformCommissionPaid != null ? d.platformCommissionPaid : (d.totalAdminCommissionDeducted || 0);
        const available = d.availablePayoutBalance != null ? d.availablePayoutBalance : totalReceived;

        const revEl = document.getElementById('pharmEtrTotalRevenue');
        const netEl = document.getElementById('pharmEtrNetSettled');
        const pendEl = document.getElementById('pharmEtrPendingSettlements');
        const countEl = document.getElementById('pharmEtrSettlementsCount');
        const commEl = document.getElementById('pharmEtrCommDeducted');
        const availEl = document.getElementById('pharmEtrAvailableBalance');

        if (revEl) revEl.textContent = `₹${Number(totalSales).toFixed(2)}`;
        if (netEl) netEl.textContent = `₹${Number(totalReceived).toFixed(2)}`;
        if (pendEl) pendEl.textContent = `₹${Number(pending).toFixed(2)}`;
        if (countEl) countEl.textContent = compCount;
        if (commEl) commEl.textContent = `₹${Number(commission).toFixed(2)}`;
        if (availEl) availEl.textContent = `₹${Number(available).toFixed(2)}`;

        // Payout account banner
        if (d.payoutAccount) {
          const bankEl = document.getElementById('pharmPayoutBankName');
          const accIdEl = document.getElementById('pharmPayoutAccountId');
          const statusEl = document.getElementById('pharmPayoutStatus');
          if (bankEl) bankEl.textContent = `${d.payoutAccount.bankName || 'Verified Bank'} ${d.payoutAccount.accountNumberMasked || '•••• 9821'}`;
          if (accIdEl) accIdEl.textContent = d.payoutAccount.gatewayLinkedAccountId || 'acc_route_01';
          if (statusEl) statusEl.textContent = d.payoutAccount.settlementStatus || 'ACTIVE';
        }

        if (!tbody) return;
        const settlements = d.settlements || [];
        currentPharmacySettlementsData = settlements;

        if (settlements.length === 0) {
          tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-muted); padding: 2rem;">No settlement records yet. Settled customer orders will appear here automatically.</td></tr>';
          return;
        }

        tbody.innerHTML = settlements.map(s => {
          let statusBadgeBg = '#dcfce7';
          let statusBadgeColor = '#166534';
          let displayStatus = s.settlementStatus || 'SETTLED';

          if (displayStatus === 'REVERSED') {
            statusBadgeBg = '#fee2e2';
            statusBadgeColor = '#991b1b';
          } else if (displayStatus === 'PENDING' || displayStatus === 'SETTLEMENT_PENDING') {
            statusBadgeBg = '#fef3c7';
            statusBadgeColor = '#92400e';
            displayStatus = 'PENDING';
          } else {
            statusBadgeBg = '#dcfce7';
            statusBadgeColor = '#166534';
            displayStatus = 'SETTLED';
          }

          const orderIdDisplay = s.orderNumber || ('ORD-' + (s.orderId || s.id));
          const customerDisplay = s.customerName || 'Customer';
          const orderAmt = Number(s.grossAmount != null ? s.grossAmount : (s.orderTotal || 0)).toFixed(2);
          const commAmt = Number(s.commissionDeducted != null ? s.commissionDeducted : (s.commissionAmount || 0)).toFixed(2);
          const ownerShare = Number(s.netAmount != null ? s.netAmount : (s.netSettlementAmount || 0)).toFixed(2);
          const payStatus = s.paymentMethod ? s.paymentMethod.toUpperCase() : 'PAID';
          const dateStr = s.createdAt ? new Date(s.createdAt).toLocaleDateString() : 'Today';

          return `
            <tr>
              <td><strong style="color: var(--primary-dark); font-size: 0.9rem;">${escapeHtml(orderIdDisplay)}</strong></td>
              <td>${escapeHtml(customerDisplay)}</td>
              <td><strong style="color: var(--text-main);">₹${orderAmt}</strong></td>
              <td><span style="color: #ef4444; font-weight: 700;">₹${commAmt}</span></td>
              <td><strong style="color: #059669; font-size: 0.95rem;">₹${ownerShare}</strong></td>
              <td><span class="sidebar-badge" style="background: #eff6ff; color: #1e40af; font-size: 0.75rem;">${escapeHtml(payStatus)}</span></td>
              <td>
                <span class="sidebar-badge" style="background: ${statusBadgeBg}; color: ${statusBadgeColor}; font-size: 0.75rem;">
                  ${escapeHtml(displayStatus)}
                </span>
              </td>
              <td style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(dateStr)}</td>
            </tr>
          `;
        }).join('');
      }
    } catch (err) {
      console.warn('Failed to load pharmacy earnings:', err);
      if (tbody) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: #ef4444; padding: 2rem;">Failed to load settlements: ${escapeHtml(err.message)}</td></tr>`;
      }
    }
  }

  const downloadEarningsCsvBtn = document.getElementById('downloadEarningsCsvBtn');
  if (downloadEarningsCsvBtn) {
    downloadEarningsCsvBtn.addEventListener('click', () => {
      if (!currentPharmacySettlementsData || currentPharmacySettlementsData.length === 0) {
        alert('No settlement records to export.');
        return;
      }
      let csv = 'Order ID,Customer,Order Amount,Commission,Owner Share,Payment Status,Settlement Status,Date\n';
      currentPharmacySettlementsData.forEach(s => {
        const orderId = s.orderNumber || ('ORD-' + (s.orderId || s.id));
        const cust = (s.customerName || 'Customer').replace(/,/g, ' ');
        const amt = Number(s.grossAmount != null ? s.grossAmount : (s.orderTotal || 0)).toFixed(2);
        const comm = Number(s.commissionDeducted != null ? s.commissionDeducted : (s.commissionAmount || 0)).toFixed(2);
        const share = Number(s.netAmount != null ? s.netAmount : (s.netSettlementAmount || 0)).toFixed(2);
        const payStatus = s.paymentMethod ? s.paymentMethod.toUpperCase() : 'PAID';
        const stlStatus = s.settlementStatus || 'SETTLED';
        const dt = s.createdAt ? new Date(s.createdAt).toLocaleDateString() : 'Today';
        csv += `"${orderId}","${cust}",${amt},${comm},${share},"${payStatus}","${stlStatus}","${dt}"\n`;
      });
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);
      link.setAttribute('download', `pharmacy_earnings_${currentPharmacyId || 'report'}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    });
  }

  const refreshEarningsBtn = document.getElementById('refreshEarningsBtn');
  if (refreshEarningsBtn) {
    refreshEarningsBtn.addEventListener('click', loadPharmacyEarnings);
  }

  // Logout
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      if (confirm('Are you sure you want to logout from the pharmacy portal?')) {
        API.clearSession();
        window.location.href = 'pharmacy-login.html';
      }
    });
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }
});
