/**
 * PharmaTech Customer Homepage & Ordering Controller
 * Handles location selection, 10km radius nearby pharmacy search,
 * atomic order checkout (normal & emergency), and live order tracking with stepper.
 */
document.addEventListener('DOMContentLoaded', () => {
  // Elements
  const searchInput = document.getElementById('medicineSearchInput');
  const nearbySearchSection = document.getElementById('nearbySearchSection');
  const nearbySearchResultsContainer = document.getElementById('nearbySearchResultsContainer');
  const searchLocalityName = document.getElementById('searchLocalityName');
  const closeSearchResultsBtn = document.getElementById('closeSearchResultsBtn');

  const productsGrid = document.getElementById('productsGrid');
  const categoriesBar = document.getElementById('categoriesBar');
  const nearbyPharmaciesList = document.getElementById('nearbyPharmaciesList');

  // Location Elements
  const openLocationModalBtn = document.getElementById('openLocationModalBtn');
  const locationModal = document.getElementById('locationModal');
  const closeLocationModalBtn = document.getElementById('closeLocationModalBtn');
  const localitySearchInput = document.getElementById('localitySearchInput');
  const locationSuggestionsList = document.getElementById('locationSuggestionsList');
  const currentLocationDisplay = document.getElementById('currentLocationDisplay');

  // Cart & Checkout Elements
  const cartBtn = document.getElementById('cartBtn');
  const cartBadge = document.getElementById('cartBadge');
  const cartDrawerOverlay = document.getElementById('cartDrawerOverlay');
  const closeCartBtn = document.getElementById('closeCartBtn');
  const cartItemsList = document.getElementById('cartItemsList');
  const cartSubtotal = document.getElementById('cartSubtotal');
  const cartTax = document.getElementById('cartTax');
  const cartGrandTotal = document.getElementById('cartGrandTotal');
  const checkoutBtn = document.getElementById('checkoutBtn');
  const userNavContainer = document.getElementById('userNavContainer');

  const checkoutModal = document.getElementById('checkoutModal');
  const closeCheckoutModalBtn = document.getElementById('closeCheckoutModalBtn');
  const checkoutForm = document.getElementById('checkoutForm');
  const checkoutItemsList = document.getElementById('checkoutItemsList');
  const checkoutGrandTotal = document.getElementById('checkoutGrandTotal');
  const checkoutAddressInput = document.getElementById('checkoutAddressInput');
  const heroEmergencyBtn = document.getElementById('heroEmergencyBtn');

  // Tracking Elements
  const trackOrderHeaderBtn = document.getElementById('trackOrderHeaderBtn');
  const orderTrackingModal = document.getElementById('orderTrackingModal');
  const closeTrackingModalBtn = document.getElementById('closeTrackingModalBtn');
  const trackOrderInput = document.getElementById('trackOrderInput');
  const btnSearchOrderTrack = document.getElementById('btnSearchOrderTrack');
  const trackOrderNumberDisplay = document.getElementById('trackOrderNumberDisplay');
  const trackPriorityBadge = document.getElementById('trackPriorityBadge');
  const trackCurrentStatusText = document.getElementById('trackCurrentStatusText');
  const stepperProgressBar = document.getElementById('stepperProgressBar');
  const trackPharmacyName = document.getElementById('trackPharmacyName');
  const trackPharmacyAddress = document.getElementById('trackPharmacyAddress');
  const trackRiderName = document.getElementById('trackRiderName');
  const trackRiderDetails = document.getElementById('trackRiderDetails');
  const trackDestinationAddress = document.getElementById('trackDestinationAddress');

  let currentCategory = 'All';
  let searchQuery = '';
  let searchTimeout = null;
  let activeTrackingTimer = null;
  let activeTrackingOrderNumber = null;
  let singleCheckoutItem = null;

  // ------------------------------------------------------------
  // PHARMACY CUSTOM DISTANCES & SINGLE LOCATION MAP
  // ------------------------------------------------------------
  function getPharmacyCustomDistance(pharmacyName, apiDist) {
    if (!pharmacyName) return (apiDist && Number(apiDist) > 0) ? Number(apiDist).toFixed(1) : '5.0';
    const n = pharmacyName.toLowerCase();
    // Coimbatore Partner Pharmacies
    if (n.includes('shree lakshmi')) return '7.1';
    if (n.includes('kmch')) return '2.4';
    if (n.includes('gandhipuram') || (n.includes('apollo') && !n.includes('greams'))) return '5.2';
    if (n.includes('peelamedu') || (n.includes('medplus') && !n.includes('anna nagar'))) return '4.8';
    if (n.includes('thulasi') || n.includes('rs puram')) return '6.5';
    if (n.includes('ganga')) return '8.2';
    // Chennai Partner Pharmacies
    if (n.includes('greams') || (n.includes('apollo') && n.includes('chennai'))) return '3.5';
    if (n.includes('anna nagar') || (n.includes('medplus') && n.includes('chennai'))) return '4.2';
    if (apiDist && Number(apiDist) > 0) return Number(apiDist).toFixed(1);
    return '5.0';
  }

  let singleShopMapInstance = null;
  let singleShopMarker = null;

  function showShopLocationOnMap(shop) {
    if (!shop) return;
    const shopName = shop.pharmacyName || shop.name || 'Partner Pharmacy';
    const shopAddress = shop.address || shop.pharmacyAddress || 'City Store';
    const city = shop.city || '';
    const fullAddress = `${shopAddress}${city && !shopAddress.includes(city) ? ', ' + city : ''}`;
    const distKm = getPharmacyCustomDistance(shopName, shop.distanceKm);
    const hours = shop.operatingHours || '08:00 AM – 11:00 PM';
    const phone = shop.phoneNumber || shop.phone || '0422-2571234';
    const upi = shop.merchantUpiId || 'merchant@upi';
    const rating = shop.rating ? Number(shop.rating).toFixed(1) : '4.8';

    // Lat / Lng coordinates (defaults to Coimbatore / Chennai coords for known shops)
    let lat = Number(shop.latitude);
    let lng = Number(shop.longitude);
    if (!lat || isNaN(lat)) {
      const n = shopName.toLowerCase();
      if (n.includes('shree lakshmi')) { lat = 11.0336; lng = 77.0396; }
      else if (n.includes('kmch')) { lat = 11.0583; lng = 77.0872; }
      else if (n.includes('gandhipuram') || (n.includes('apollo') && !n.includes('greams'))) { lat = 11.0168; lng = 76.9558; }
      else if (n.includes('peelamedu') || (n.includes('medplus') && !n.includes('anna nagar'))) { lat = 11.0267; lng = 77.0118; }
      else if (n.includes('thulasi') || n.includes('rs puram')) { lat = 11.0089; lng = 76.9511; }
      else if (n.includes('ganga')) { lat = 11.0289; lng = 76.9467; }
      else if (n.includes('greams')) { lat = 13.0569; lng = 80.2525; }
      else if (n.includes('anna nagar')) { lat = 13.0850; lng = 80.2101; }
      else { lat = 11.0583; lng = 77.0872; }
    }

    // Modal DOM Elements
    const modalTitle = document.getElementById('shopModalTitle');
    const modalSubtitle = document.getElementById('shopModalSubtitle');
    const modalName = document.getElementById('shopModalName');
    const modalAddress = document.getElementById('shopModalAddress');
    const modalDistance = document.getElementById('shopModalDistance');
    const modalRating = document.getElementById('shopModalRating');
    const modalHours = document.getElementById('shopModalHours');
    const modalPhone = document.getElementById('shopModalPhone');
    const modalUpi = document.getElementById('shopModalUpi');
    const externalLink = document.getElementById('shopModalExternalLink');
    const modal = document.getElementById('shopLocationModal');

    if (modalTitle) modalTitle.textContent = shopName;
    if (modalSubtitle) modalSubtitle.textContent = `Exact Store Location • ${distKm} km away`;
    if (modalName) modalName.textContent = shopName;
    if (modalAddress) modalAddress.textContent = fullAddress;
    if (modalDistance) modalDistance.textContent = `📍 ${distKm} km away`;
    if (modalRating) modalRating.textContent = `★ ${rating}`;
    if (modalHours) modalHours.textContent = hours;
    if (modalPhone) modalPhone.textContent = phone;
    if (modalUpi) modalUpi.textContent = upi;

    // Search query showing ONLY the store location in Google Maps (no directions/route)
    if (externalLink) {
      externalLink.href = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(shopName + ', ' + fullAddress)}`;
    }

    if (modal) {
      modal.classList.add('open');
    }

    // Render single shop location map (no route lines, no directions, ONLY shop location)
    const mapContainer = document.getElementById('shopSingleMap');
    if (mapContainer && typeof L !== 'undefined') {
      try {
        if (!singleShopMapInstance) {
          singleShopMapInstance = L.map('shopSingleMap', {
            zoomControl: true,
            attributionControl: false
          }).setView([lat, lng], 16);

          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
          }).addTo(singleShopMapInstance);
        } else {
          singleShopMapInstance.setView([lat, lng], 16);
        }

        if (singleShopMarker) {
          singleShopMapInstance.removeLayer(singleShopMarker);
        }

        const storeIcon = L.divIcon({
          className: 'custom-shop-pin',
          html: `<div style="background: #059669; color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: 0 4px 12px rgba(5,150,105,0.45); border: 2.5px solid white;">🏥</div>`,
          iconSize: [40, 40],
          iconAnchor: [20, 20],
          popupAnchor: [0, -22]
        });

        singleShopMarker = L.marker([lat, lng], { icon: storeIcon })
          .addTo(singleShopMapInstance)
          .bindPopup(`
            <div style="font-family: inherit; font-size: 0.88rem; min-width: 180px;">
              <strong style="color: #065f46; font-size: 0.95rem;">${escapeHtml(shopName)}</strong><br>
              <span style="color: #475569; font-size: 0.8rem;">${escapeHtml(fullAddress)}</span><br>
              <div style="margin-top: 0.4rem; display: flex; gap: 0.4rem;">
                <span style="background: #ecfdf5; color: #065f46; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 700; font-size: 0.75rem;">📍 ${distKm} km</span>
                <span style="background: #fef3c7; color: #92400e; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 700; font-size: 0.75rem;">★ ${rating}</span>
              </div>
            </div>
          `)
          .openPopup();

        setTimeout(() => {
          if (singleShopMapInstance) singleShopMapInstance.invalidateSize();
        }, 250);
      } catch (e) {
        console.warn('Leaflet map error:', e);
      }
    }
  }

  // Shop modal close buttons
  const closeShopModalBtn = document.getElementById('closeShopModalBtn');
  const btnShopModalClose = document.getElementById('btnShopModalClose');
  const shopLocationModal = document.getElementById('shopLocationModal');
  if (closeShopModalBtn && shopLocationModal) {
    closeShopModalBtn.addEventListener('click', () => shopLocationModal.classList.remove('open'));
  }
  if (btnShopModalClose && shopLocationModal) {
    btnShopModalClose.addEventListener('click', () => shopLocationModal.classList.remove('open'));
  }
  if (shopLocationModal) {
    shopLocationModal.addEventListener('click', (e) => {
      if (e.target === shopLocationModal) shopLocationModal.classList.remove('open');
    });
  }

  // Initialize
  initUserSession();
  initLocation();
  loadCategories();
  loadProducts();
  loadNearbyPharmacies();
  updateCartUI();

  // ============================================================
  // 1. LOCATION SELECTION & AUTOCOMPLETE
  // ============================================================
  function initLocation() {
    let loc = API.getUserLocation();
    if (!loc || !loc.city || loc.city.toLowerCase() === 'bangalore') {
      loc = {
        area: 'Neelambur',
        city: 'Coimbatore',
        pincode: '641062',
        latitude: 11.0583,
        longitude: 77.0872
      };
      API.saveUserLocation(loc);
    }
    updateLocationDisplay(loc);
  }

  function updateLocationDisplay(loc) {
    if (currentLocationDisplay) {
      currentLocationDisplay.textContent = `${loc.area}, ${loc.city || 'Coimbatore'}`;
    }
    if (searchLocalityName) {
      searchLocalityName.textContent = loc.area;
    }
  }

  if (openLocationModalBtn) {
    openLocationModalBtn.addEventListener('click', () => {
      locationModal.classList.add('open');
      loadLocationSuggestions('');
    });
  }

  if (closeLocationModalBtn) {
    closeLocationModalBtn.addEventListener('click', () => {
      locationModal.classList.remove('open');
    });
  }

  if (localitySearchInput) {
    localitySearchInput.addEventListener('input', (e) => {
      loadLocationSuggestions(e.target.value.trim());
    });
  }

  async function loadLocationSuggestions(query) {
    if (!locationSuggestionsList) return;
    try {
      const res = await API.getLocationSuggestions(query);
      const list = res && res.data ? res.data : [];

      if (list.length === 0) {
        locationSuggestionsList.innerHTML = `<div style="padding: 1rem; text-align: center; color: var(--text-muted); font-size: 0.85rem;">No matching localities found. Try "Neelambur", "SITRA", or "Anna Nagar".</div>`;
        return;
      }

      locationSuggestionsList.innerHTML = list.map(item => `
        <div class="location-suggestion-item" data-location='${JSON.stringify(item)}'>
          <div>
            <strong style="color: var(--text-main); font-size: 0.92rem;">📍 ${escapeHtml(item.area)}</strong>
            <span style="font-size: 0.78rem; color: var(--text-muted); display: block;">${escapeHtml(item.city)} • Pincode: ${item.pincode}</span>
          </div>
          <span style="font-size: 0.78rem; color: var(--primary); font-weight: 700;">Select →</span>
        </div>
      `).join('');

      locationSuggestionsList.querySelectorAll('.location-suggestion-item').forEach(el => {
        el.addEventListener('click', async () => {
          const loc = JSON.parse(el.getAttribute('data-location'));
          API.saveUserLocation(loc);
          updateLocationDisplay(loc);
          locationModal.classList.remove('open');

          // If logged in customer, sync with backend
          const user = API.getUserSession();
          if (user && user.role === 'ROLE_CUSTOMER') {
            try {
              await API.updateCustomerLocation(user.userId, loc.area, loc.latitude, loc.longitude);
            } catch (err) {
              console.warn('Backend location update notice:', err);
            }
          }

          // Immediately update partner pharmacies and medicine catalog for the selected city!
          loadNearbyPharmacies(loc.city);
          loadProducts(null, null, loc.city);

          // Rerun search if active
          if (searchQuery.trim().length > 0) {
            performNearbyMedicineSearch(searchQuery.trim());
          }
        });
      });
    } catch (err) {
      console.warn('Location suggestions load error:', err);
    }
  }

  // ============================================================
  // 2. MEDICINE SEARCH WITH NEARBY PHARMACIES (10 KM RADIUS)
  // ============================================================
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      clearTimeout(searchTimeout);
      searchQuery = e.target.value;
      searchTimeout = setTimeout(() => {
        if (searchQuery.trim().length > 0) {
          performNearbyMedicineSearch(searchQuery.trim());
        } else {
          nearbySearchSection.style.display = 'none';
        }
        loadProducts();
      }, 300);
    });
  }

  if (closeSearchResultsBtn) {
    closeSearchResultsBtn.addEventListener('click', () => {
      searchInput.value = '';
      searchQuery = '';
      nearbySearchSection.style.display = 'none';
      loadProducts();
    });
  }

  async function performNearbyMedicineSearch(keyword) {
    const loc = API.getUserLocation() || { latitude: 11.0583, longitude: 77.0872, area: 'Neelambur', city: 'Coimbatore' };
    nearbySearchSection.style.display = 'block';
    nearbySearchResultsContainer.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #047857;">
        <span style="font-size: 1.5rem; display: block; margin-bottom: 0.5rem;">🔍</span>
        Searching verified pharmacies within 10 km radius of ${loc.area}...
      </div>
    `;

    try {
      const res = await API.searchNearbyPharmacies(keyword, loc.latitude, loc.longitude, 10.0);
      const results = res && res.data ? res.data : [];

      if (results.length === 0) {
        nearbySearchResultsContainer.innerHTML = `
          <div style="grid-column: 1/-1; background: white; border-radius: var(--radius-lg); border: 2px dashed #fca5a5; padding: 2.5rem; text-align: center;">
            <span style="font-size: 2.5rem; display: block; margin-bottom: 0.5rem;">⚠️</span>
            <h4 style="font-size: 1.15rem; font-weight: 800; color: #991b1b; margin-bottom: 0.5rem;">
              No pharmacies within a 10 km radius of ${escapeHtml(loc.area)} currently have "${escapeHtml(keyword)}" in stock.
            </h4>
            <p style="font-size: 0.88rem; color: var(--text-muted); max-width: 500px; margin: 0 auto 1.25rem;">
              You can try expanding your search, selecting an alternative dosage, or changing your delivery location.
            </p>
            <button class="btn btn-outline-primary" onclick="document.getElementById('openLocationModalBtn').click();">
              📍 Change Delivery Location
            </button>
          </div>
        `;
        return;
      }

      // Sort controls & render function
      let activeSort = 'distance';
      function renderSortedResults(items) {
        let sorted = [...items];
        if (activeSort === 'distance') {
          sorted.sort((a, b) => parseFloat(getPharmacyCustomDistance(a.pharmacyName, a.distanceKm)) - parseFloat(getPharmacyCustomDistance(b.pharmacyName, b.distanceKm)));
        } else if (activeSort === 'time') {
          sorted.sort((a, b) => (a.estimatedDeliveryMinutes || 0) - (b.estimatedDeliveryMinutes || 0));
        } else if (activeSort === 'price') {
          sorted.sort((a, b) => Number(a.price || a.unitPrice || 0) - Number(b.price || b.unitPrice || 0));
        }

        const cardsHtml = sorted.map(item => {
          const availableQty = (item.availableQuantity !== undefined) ? item.availableQuantity : (item.availableStock || 0);
          const inStock = item.inStock && availableQty > 0;
          const distKm = getPharmacyCustomDistance(item.pharmacyName, item.distanceKm);
          const estMin = item.estimatedDeliveryMinutes || Math.max(15, Math.round(10 + (parseFloat(distKm) * 2.5)));
          const unitPrice = Number(item.price !== undefined ? item.price : item.unitPrice || 0).toFixed(2);
          const pharmacyAddress = item.address || item.pharmacyAddress || 'City Store';

          return `
            <div class="nearby-result-card">
              <div>
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                  <span style="background: #eff6ff; color: #1e40af; font-size: 0.75rem; font-weight: 800; padding: 0.2rem 0.6rem; border-radius: 9999px;">
                    📍 ${distKm} km away
                  </span>
                  <span style="background: ${inStock ? '#dcfce7' : '#fee2e2'}; color: ${inStock ? '#166534' : '#b91c1c'}; font-size: 0.75rem; font-weight: 800; padding: 0.2rem 0.6rem; border-radius: 9999px;">
                    ${inStock ? `In Stock (${availableQty} units)` : 'Out of Stock'}
                  </span>
                </div>

                <h4 style="font-size: 1.05rem; font-weight: 800; color: var(--text-main); margin-bottom: 0.15rem;">
                  ${escapeHtml(item.pharmacyName)}
                </h4>
                <p style="font-size: 0.78rem; color: var(--text-muted); margin-bottom: 0.75rem;">
                  ${escapeHtml(pharmacyAddress)}
                </p>

                <div style="background: #f8fafc; border-radius: 8px; padding: 0.75rem; border: 1px solid var(--border-light); margin-bottom: 0.75rem;">
                  <strong style="color: var(--primary-dark); font-size: 0.95rem; display: block;">
                    ${escapeHtml(item.medicineName)}
                  </strong>
                  <span style="font-size: 0.78rem; color: #475569;">
                    ${escapeHtml(item.brand || 'Care')} • ${escapeHtml(item.strength || '')}
                  </span>
                  <div style="display: flex; justify-content: space-between; align-items: baseline; margin-top: 0.4rem;">
                    <span style="font-size: 0.8rem; color: #64748b;">Unit Price:</span>
                    <strong style="font-size: 1.15rem; color: #059669;">₹${unitPrice}</strong>
                  </div>
                </div>

                <div style="display: flex; align-items: center; gap: 0.35rem; font-size: 0.8rem; color: #0284c7; margin-bottom: 0.85rem; font-weight: 600;">
                  <span>⚡ Estimated Delivery:</span>
                  <strong>${estMin} minutes</strong>
                </div>
              </div>

              <div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem; margin-bottom: 0.45rem;">
                  <button class="btn btn-outline-primary btn-add-nearby-cart" data-item='${JSON.stringify(item)}' ${!inStock ? 'disabled' : ''} style="font-size: 0.8rem; padding: 0.55rem 0.5rem; font-weight: 700;">
                    + Add Cart
                  </button>
                  <button class="btn btn-primary btn-order-nearby-now" data-item='${JSON.stringify(item)}' ${!inStock ? 'disabled' : ''} style="font-size: 0.8rem; padding: 0.55rem 0.5rem; font-weight: 800;">
                    Order Now →
                  </button>
                </div>
                <button class="btn btn-view-pharmacy-map" data-item='${JSON.stringify(item)}' style="width: 100%; background: #f0fdf4; border: 1px solid #86efac; color: #166534; font-size: 0.78rem; font-weight: 700; padding: 0.45rem; border-radius: 6px; cursor: pointer;">
                  🗺️ View on Map (${distKm} km)
                </button>
              </div>
            </div>
          `;
        }).join('');

        nearbySearchResultsContainer.innerHTML = `
          <div style="grid-column: 1/-1; display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; background: white; padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--border-light);">
            <span style="font-size: 0.85rem; color: var(--text-main); font-weight: 700;">
              Found ${items.length} pharmacies within 10 km:
            </span>
            <div style="display: flex; gap: 0.5rem; align-items: center; font-size: 0.8rem;">
              <span style="color: var(--text-muted);">Sort by:</span>
              <button class="btn-sort-toggle ${activeSort === 'distance' ? 'active' : ''}" data-sort="distance" style="padding: 0.25rem 0.6rem; border-radius: 4px; border: 1px solid ${activeSort === 'distance' ? '#059669' : '#cbd5e1'}; background: ${activeSort === 'distance' ? '#ecfdf5' : 'white'}; color: ${activeSort === 'distance' ? '#059669' : '#475569'}; font-weight: 700; cursor: pointer;">Nearest</button>
              <button class="btn-sort-toggle ${activeSort === 'time' ? 'active' : ''}" data-sort="time" style="padding: 0.25rem 0.6rem; border-radius: 4px; border: 1px solid ${activeSort === 'time' ? '#059669' : '#cbd5e1'}; background: ${activeSort === 'time' ? '#ecfdf5' : 'white'}; color: ${activeSort === 'time' ? '#059669' : '#475569'}; font-weight: 700; cursor: pointer;">Fastest</button>
              <button class="btn-sort-toggle ${activeSort === 'price' ? 'active' : ''}" data-sort="price" style="padding: 0.25rem 0.6rem; border-radius: 4px; border: 1px solid ${activeSort === 'price' ? '#059669' : '#cbd5e1'}; background: ${activeSort === 'price' ? '#ecfdf5' : 'white'}; color: ${activeSort === 'price' ? '#059669' : '#475569'}; font-weight: 700; cursor: pointer;">Lowest Price</button>
            </div>
          </div>
          ${cardsHtml}
        `;

        // Attach sort buttons
        nearbySearchResultsContainer.querySelectorAll('.btn-sort-toggle').forEach(btn => {
          btn.addEventListener('click', () => {
            activeSort = btn.getAttribute('data-sort');
            renderSortedResults(items);
          });
        });

        // Attach button clicks
        nearbySearchResultsContainer.querySelectorAll('.btn-add-nearby-cart').forEach(btn => {
          btn.addEventListener('click', () => {
            const item = JSON.parse(btn.getAttribute('data-item'));
            API.addToCart({
              inventoryId: item.inventoryId,
              medicineId: item.medicineId,
              name: item.medicineName,
              brand: item.brand,
              strength: item.strength,
              price: Number(item.price !== undefined ? item.price : item.unitPrice),
              pharmacyId: item.pharmacyId,
              pharmacyName: item.pharmacyName,
              quantity: 1
            });
            updateCartUI();
            btn.textContent = '✓ Added';
            btn.style.background = '#059669';
            btn.style.color = '#fff';
            setTimeout(() => {
              btn.textContent = '+ Add Cart';
              btn.style.background = '';
              btn.style.color = '';
            }, 1200);
          });
        });

        nearbySearchResultsContainer.querySelectorAll('.btn-order-nearby-now').forEach(btn => {
          btn.addEventListener('click', () => {
            const item = JSON.parse(btn.getAttribute('data-item'));
            API.addToCart({
              inventoryId: item.inventoryId,
              medicineId: item.medicineId,
              name: item.medicineName,
              brand: item.brand,
              strength: item.strength,
              price: Number(item.price !== undefined ? item.price : item.unitPrice),
              pharmacyId: item.pharmacyId,
              pharmacyName: item.pharmacyName,
              quantity: 1
            });
            const user = API.getUserSession();
            if (!user || user.role !== 'ROLE_CUSTOMER') {
              window.location.href = 'customer-login.html?returnUrl=checkout.html';
              return;
            }
            window.location.href = 'checkout.html';
          });
        });

        nearbySearchResultsContainer.querySelectorAll('.btn-view-pharmacy-map').forEach(btn => {
          btn.addEventListener('click', () => {
            const item = JSON.parse(btn.getAttribute('data-item'));
            showShopLocationOnMap(item);
          });
        });
      }

      renderSortedResults(results);

    } catch (err) {
      nearbySearchResultsContainer.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: #ef4444;">
          Failed to complete nearby search: ${err.message}
        </div>
      `;
    }
  }

  // ============================================================
  // 3. CHECKOUT & ORDER ROUTING (TO DEDICATED CHECKOUT PAGE)
  // ============================================================
  if (checkoutBtn) {
    checkoutBtn.addEventListener('click', () => {
      const cart = API.getCart();
      if (!cart || cart.length === 0) {
        alert('Your shopping cart is empty. Please add medicines first.');
        return;
      }
      const user = API.getUserSession();
      if (!user || user.role !== 'ROLE_CUSTOMER') {
        window.location.href = 'customer-login.html?returnUrl=checkout.html';
        return;
      }
      window.location.href = 'checkout.html';
    });
  }

  if (closeCheckoutModalBtn) {
    closeCheckoutModalBtn.addEventListener('click', () => {
      checkoutModal.classList.remove('open');
    });
  }

  if (heroEmergencyBtn) {
    heroEmergencyBtn.addEventListener('click', () => {
      // Pre-select emergency radio and open search or first available product
      searchInput.focus();
      searchInput.value = 'Dolo 650';
      performNearbyMedicineSearch('Dolo 650');
      window.scrollTo({ top: 150, behavior: 'smooth' });
    });
  }

  function renderCheckoutModal(items) {
    const user = API.getUserSession();
    if (!user || user.role !== 'ROLE_CUSTOMER') {
      alert('Please sign in to your customer account to place medicine orders.');
      window.location.href = 'customer-login.html';
      return;
    }

    const loc = API.getUserLocation() || { area: 'Indiranagar', city: 'Bangalore' };
    if (checkoutAddressInput) {
      checkoutAddressInput.value = `${loc.area}, ${loc.city || 'Bangalore'} - 560038`;
    }

    checkoutItemsList.innerHTML = items.map(i => `
      <div style="display: flex; justify-content: space-between; padding: 0.4rem 0; border-bottom: 1px dashed #e2e8f0;">
        <div>
          <strong>${escapeHtml(i.name)}</strong> <small>(${escapeHtml(i.strength || '')})</small>
          <div style="font-size: 0.72rem; color: #059669;">From: ${escapeHtml(i.pharmacyName || 'Partner Pharmacy')}</div>
        </div>
        <div style="text-align: right;">
          <span>x${i.quantity}</span>
          <strong style="margin-left: 0.5rem;">₹${(i.price * i.quantity).toFixed(2)}</strong>
        </div>
      </div>
    `).join('');

    const subtotal = items.reduce((sum, i) => sum + (i.price * i.quantity), 0);
    const tax = subtotal * 0.05;
    const grand = subtotal + tax;

    checkoutGrandTotal.textContent = `₹${grand.toFixed(2)}`;
    checkoutModal.classList.add('open');
  }

  if (checkoutForm) {
    checkoutForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const user = API.getUserSession();
      if (!user) {
        alert('Please login as a customer first.');
        window.location.href = 'customer-login.html';
        return;
      }

      const items = singleCheckoutItem ? [singleCheckoutItem] : API.getCart();
      if (!items || items.length === 0) {
        alert('No items to order.');
        return;
      }

      const loc = API.getUserLocation() || { latitude: 12.9716, longitude: 77.6412, area: 'Indiranagar' };
      const priorityRadio = document.querySelector('input[name="orderPriority"]:checked');
      const paymentRadio = document.querySelector('input[name="orderPayment"]:checked');

      const priority = priorityRadio ? priorityRadio.value : 'NORMAL';
      const paymentMethod = paymentRadio ? paymentRadio.value : 'PAY_ON_DELIVERY';
      const deliveryAddress = checkoutAddressInput.value.trim();

      const pharmacyId = items[0].pharmacyId || 1;

      const orderPayload = {
        customerId: user.userId,
        pharmacyId: pharmacyId,
        customerName: user.fullName || 'Customer',
        customerPhone: user.phoneNumber || '9876543210',
        deliveryAddress: deliveryAddress,
        customerLatitude: loc.latitude,
        customerLongitude: loc.longitude,
        paymentMethod: paymentMethod,
        priority: priority,
        items: items.map(i => ({
          inventoryId: i.inventoryId || 1,
          quantity: i.quantity
        }))
      };

      const submitBtn = document.getElementById('confirmPlaceOrderBtn');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Reserving Stock & Placing Order...';

      try {
        const res = await API.createCustomerOrder(orderPayload);
        if (res && res.data) {
          const placedOrder = res.data;

          // Clear cart if multi-item checkout
          if (!singleCheckoutItem) {
            API.clearCart();
            updateCartUI();
          }

          checkoutModal.classList.remove('open');
          alert(`🎉 Order Placed Successfully!\n\nOrder Number: ${placedOrder.orderNumber}\nPriority: ${placedOrder.priority}\nStock deducted automatically from ${placedOrder.pharmacyName}.`);

          // Open Live Tracking Modal
          openOrderTracker(placedOrder.orderNumber);
        }
      } catch (err) {
        alert('Order placement failed: ' + err.message);
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Place Order & Assign Rider →';
      }
    });
  }

  // ============================================================
  // 4. LIVE ORDER TRACKING (DEDICATED PAGE WITH INTERACTIVE MAP)
  // ============================================================
  if (trackOrderHeaderBtn) {
    trackOrderHeaderBtn.addEventListener('click', () => {
      if (activeTrackingOrderNumber) {
        window.location.href = `order-tracking.html?orderNumber=${encodeURIComponent(activeTrackingOrderNumber)}`;
      } else {
        window.location.href = 'order-tracking.html';
      }
    });
  }

  if (closeTrackingModalBtn) {
    closeTrackingModalBtn.addEventListener('click', () => {
      orderTrackingModal.classList.remove('open');
      clearInterval(activeTrackingTimer);
    });
  }

  if (btnSearchOrderTrack) {
    btnSearchOrderTrack.addEventListener('click', () => {
      const num = trackOrderInput.value.trim();
      if (!num) {
        alert('Please enter an order number.');
        return;
      }
      openOrderTracker(num);
    });
  }

  function openOrderTracker(orderNumber) {
    activeTrackingOrderNumber = orderNumber;
    trackOrderInput.value = orderNumber;
    orderTrackingModal.classList.add('open');

    // Initial fetch and poll every 4s
    pollOrderStatus(orderNumber);
    clearInterval(activeTrackingTimer);
    activeTrackingTimer = setInterval(() => {
      pollOrderStatus(orderNumber);
    }, 4000);
  }

  async function pollOrderStatus(orderNumber) {
    try {
      const res = await API.trackOrderByNumber(orderNumber);
      if (res && res.data) {
        updateTrackingUI(res.data);
      }
    } catch (err) {
      console.warn('Tracking poll notice:', err);
    }
  }

  function updateTrackingUI(order) {
    trackOrderNumberDisplay.textContent = order.orderNumber;

    if (trackPriorityBadge) {
      if (order.priority === 'EMERGENCY') {
        trackPriorityBadge.className = 'badge-emergency-pulse';
        trackPriorityBadge.innerHTML = '🚨 EMERGENCY PRIORITY DISPATCH';
      } else {
        trackPriorityBadge.className = 'badge-normal-priority';
        trackPriorityBadge.innerHTML = 'Standard Delivery';
      }
    }

    trackCurrentStatusText.textContent = formatStatusLabel(order.status);
    trackPharmacyName.textContent = order.pharmacyName || 'Partner Pharmacy';
    trackPharmacyAddress.textContent = order.pharmacyAddress || 'Bangalore Store';

    if (order.deliveryPartnerName) {
      trackRiderName.textContent = order.deliveryPartnerName;
      trackRiderDetails.textContent = `Vehicle: ${order.vehicleNumber || '-'} • Ph: ${order.deliveryPartnerPhone || '-'}`;
    } else {
      trackRiderName.textContent = 'Awaiting partner assignment';
      trackRiderDetails.textContent = (order.status === 'READY_FOR_PICKUP')
        ? 'Package ready • Waiting for delivery partner'
        : 'Order with pharmacy for verification';
    }

    trackDestinationAddress.textContent = order.deliveryAddress || 'Address on file';

    // Update Stepper
    updateStepper(order.status);
  }

  function updateStepper(status) {
    const steps = ['step1', 'step2', 'step3', 'step4', 'step5', 'step6'];
    steps.forEach(s => {
      const el = document.getElementById(s);
      if (el) {
        el.classList.remove('active', 'completed');
      }
    });

    let activeIndex = 0;
    let progressPercent = 0;

    switch (status) {
      case 'ORDER_PLACED':
        activeIndex = 0;
        progressPercent = 5;
        break;
      case 'PREPARING':
        activeIndex = 1;
        progressPercent = 25;
        break;
      case 'READY_FOR_PICKUP':
        activeIndex = 2;
        progressPercent = 45;
        break;
      case 'PARTNER_ASSIGNED':
        activeIndex = 3;
        progressPercent = 65;
        break;
      case 'PICKED_UP':
      case 'OUT_FOR_DELIVERY':
        activeIndex = 4;
        progressPercent = 85;
        break;
      case 'DELIVERED':
        activeIndex = 5;
        progressPercent = 100;
        break;
      default:
        activeIndex = 0;
        progressPercent = 5;
    }

    for (let i = 0; i < activeIndex; i++) {
      const el = document.getElementById(steps[i]);
      if (el) el.classList.add('completed');
    }
    const currentEl = document.getElementById(steps[activeIndex]);
    if (currentEl) {
      currentEl.classList.add('active');
    }

    if (stepperProgressBar) {
      stepperProgressBar.style.width = `${progressPercent}%`;
    }
  }

  function formatStatusLabel(st) {
    switch (st) {
      case 'ORDER_PLACED': return 'Order Placed • Waiting for pharmacy confirmation';
      case 'PHARMACY_ACCEPTED': return 'Pharmacy Accepted • Preparing medicines';
      case 'PREPARING': return 'Pharmacy is Preparing Medicines';
      case 'READY_FOR_PICKUP': return 'Medicines Packed & Ready for Pickup';
      case 'PARTNER_ASSIGNED': return 'Delivery Partner Assigned';
      case 'PICKED_UP': return 'Package Picked Up by Rider';
      case 'OUT_FOR_DELIVERY': return 'Out for Delivery to Your Doorstep';
      case 'DELIVERED': return 'Delivered Successfully ✓';
      default: return st || 'Processing';
    }
  }

  // ============================================================
  // 5. MASTER CATALOG & CATEGORIES & CART
  // ============================================================
  async function loadCategories() {
    try {
      const res = await API.getCategories();
      if (res && res.data && categoriesBar) {
        const categories = ['All', ...res.data];
        categoriesBar.innerHTML = categories.map(cat => `
          <button class="category-chip ${cat === currentCategory ? 'active' : ''}" data-category="${cat}">
            <span class="category-chip-icon">${getCategoryIcon(cat)}</span>
            <span>${cat}</span>
          </button>
        `).join('');

        categoriesBar.querySelectorAll('.category-chip').forEach(chip => {
          chip.addEventListener('click', () => {
            categoriesBar.querySelectorAll('.category-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            currentCategory = chip.getAttribute('data-category');
            loadProducts();
          });
        });
      }
    } catch (err) {
      console.warn('Categories load error:', err);
    }
  }

  async function loadProducts(query = null, category = null, city = null) {
    if (!productsGrid) return;
    try {
      const q = query !== null ? query : searchQuery;
      const cat = category !== null ? category : currentCategory;
      const loc = API.getUserLocation();
      const targetCity = city || (loc && loc.city ? loc.city : 'Coimbatore');
      const res = await API.getProducts(q, cat, targetCity);
      const items = res && res.data ? res.data : [];

      if (items.length === 0) {
        productsGrid.innerHTML = `
          <div style="grid-column: 1/-1; text-align: center; padding: 3rem; background: white; border-radius: 16px; border: 1px dashed #cbd5e1;">
            <h4 style="font-size: 1.15rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.5rem;">No medicines found matching "${escapeHtml(searchQuery || '')}" in ${escapeHtml(targetCity)}</h4>
            <p style="font-size: 0.88rem; color: var(--text-muted); margin-bottom: 1rem;">Try another search term or change delivery location.</p>
            <button class="btn btn-outline-primary" onclick="document.getElementById('medicineSearchInput').value=''; loadProducts();">Clear Filter</button>
          </div>
        `;
        return;
      }

      productsGrid.innerHTML = items.map(item => `
        <div class="product-card">
          <div>
            <div class="product-badge-group">
              <span class="badge ${item.prescriptionRequired ? 'badge-prescription' : 'badge-otc'}">
                ${item.prescriptionRequired ? 'Rx Required' : 'OTC'}
              </span>
              <span class="badge-stock">
                ${item.quantity > 0 ? `In Stock (${item.quantity})` : 'Out of Stock'}
              </span>
            </div>

            <div class="product-image-container">
              ${getProductSvg(item.category)}
            </div>

            <span class="product-category-text">${escapeHtml(item.category || 'General')}</span>
            <h4 class="product-name">${escapeHtml(item.medicineName)}</h4>
            <p class="product-strength">${escapeHtml(item.strength || '')}</p>
            <span class="product-brand">${escapeHtml(item.brand || 'Pharma')}</span>
            <p style="font-size: 0.72rem; color: #047857; margin-bottom: 0.5rem; display: flex; align-items: center; justify-content: space-between;">
              <span>🏪 ${escapeHtml(item.pharmacyName || 'Verified Store')}</span>
              <span style="background: #ecfdf5; color: #065f46; padding: 0.1rem 0.4rem; border-radius: 4px; font-weight: 700;">📍 ${getPharmacyCustomDistance(item.pharmacyName, item.distanceKm)} km</span>
            </p>
          </div>

          <div class="product-bottom-row">
            <div class="product-price-box">
              <span class="product-price-label">Price</span>
              <span class="product-price">₹${Number(item.price).toFixed(2)}</span>
            </div>
            <button class="btn-add-cart" data-inventory='${JSON.stringify(item)}'>
              + Add
            </button>
          </div>
        </div>
      `).join('');

      productsGrid.querySelectorAll('.btn-add-cart').forEach(btn => {
        btn.addEventListener('click', () => {
          const item = JSON.parse(btn.getAttribute('data-inventory'));
          API.addToCart(item);
          updateCartUI();
          btn.textContent = '✓ Added';
          btn.style.background = '#059669';
          btn.style.color = '#fff';
          setTimeout(() => {
            btn.textContent = '+ Add';
            btn.style.background = '';
            btn.style.color = '';
          }, 1200);
        });
      });
    } catch (err) {
      productsGrid.innerHTML = `<div style="grid-column: 1/-1; padding: 2rem; color: #ef4444; text-align: center;">Error loading products: ${err.message}</div>`;
    }
  }

  async function loadNearbyPharmacies(city = null) {
    if (!nearbyPharmaciesList) return;
    const loc = API.getUserLocation();
    const targetCity = city || (loc && loc.city ? loc.city : 'Coimbatore');
    try {
      const res = await API.getNearbyPharmacies(targetCity);
      const list = res && res.data ? res.data : [];

      if (list.length === 0) {
        nearbyPharmaciesList.innerHTML = `<p style="color: var(--text-muted); font-size: 0.88rem;">No registered partner pharmacies found.</p>`;
        return;
      }

      nearbyPharmaciesList.innerHTML = list.map(pharm => {
        const distKm = getPharmacyCustomDistance(pharm.name, pharm.distanceKm);
        const rating = pharm.rating ? Number(pharm.rating).toFixed(1) : '4.8';
        return `
          <div class="pharmacy-card">
            <div>
              <div class="pharmacy-card-header">
                <h4 class="pharmacy-store-name">${escapeHtml(pharm.name)}</h4>
                <span class="rating-pill">★ ${rating}</span>
              </div>
              <p class="pharmacy-address">${escapeHtml(pharm.address)}, ${escapeHtml(pharm.city)} - ${escapeHtml(pharm.pincode)}</p>
              <p style="font-size: 0.78rem; color: var(--text-muted); margin-bottom: 0.35rem;">
                Hours: ${escapeHtml(pharm.operatingHours || '8:00 AM – 11:00 PM')}
              </p>
              <p style="font-size: 0.75rem; color: #047857; margin-bottom: 0.5rem;">
                UPI: <code>${escapeHtml(pharm.merchantUpiId || 'merchant@upi')}</code>
              </p>
            </div>
            <div class="pharmacy-badges" style="margin-bottom: 0.75rem;">
              <span>✓ Verified Store</span>
              <span style="background: #ecfdf5; border-color: #a7f3d0; color: #065f46; font-weight: 800;">📍 ${distKm} km</span>
              <span>⚡ Fast Delivery</span>
            </div>
            <button class="btn btn-view-pharmacy-single-map" data-pharm='${JSON.stringify(pharm)}' style="width: 100%; background: #f0fdf4; border: 1px solid #86efac; color: #166534; font-size: 0.8rem; font-weight: 700; padding: 0.45rem; border-radius: 6px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 0.35rem;">
              🗺️ View on Map (${distKm} km)
            </button>
          </div>
        `;
      }).join('');

      nearbyPharmaciesList.querySelectorAll('.btn-view-pharmacy-single-map').forEach(btn => {
        btn.addEventListener('click', () => {
          const pharm = JSON.parse(btn.getAttribute('data-pharm'));
          showShopLocationOnMap(pharm);
        });
      });
    } catch (err) {
      console.warn('Pharmacies load warning:', err);
    }
  }

  // Cart Drawer
  if (cartBtn) {
    cartBtn.addEventListener('click', () => {
      cartDrawerOverlay.classList.add('open');
      renderCartDrawer();
    });
  }

  if (closeCartBtn) {
    closeCartBtn.addEventListener('click', () => {
      cartDrawerOverlay.classList.remove('open');
    });
  }

  if (cartDrawerOverlay) {
    cartDrawerOverlay.addEventListener('click', (e) => {
      if (e.target === cartDrawerOverlay) cartDrawerOverlay.classList.remove('open');
    });
  }

  function updateCartUI() {
    const cart = API.getCart();
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    if (cartBadge) {
      cartBadge.textContent = totalItems;
      cartBadge.style.display = totalItems > 0 ? 'inline-block' : 'none';
    }
  }

  function renderCartDrawer() {
    const cart = API.getCart();
    if (!cartItemsList) return;

    if (cart.length === 0) {
      cartItemsList.innerHTML = `
        <div style="text-align: center; padding: 3rem 1rem; color: var(--text-muted);">
          <span style="font-size: 2.5rem; display: block; margin-bottom: 0.5rem;">🛒</span>
          <h4 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main); margin-bottom: 0.35rem;">Your Cart is Empty</h4>
          <p style="font-size: 0.85rem;">Browse medicines and tap + Add to order.</p>
        </div>
      `;
      cartSubtotal.textContent = '₹0.00';
      cartTax.textContent = '₹0.00';
      cartGrandTotal.textContent = '₹0.00';
      return;
    }

    cartItemsList.innerHTML = cart.map(item => `
      <div class="cart-item">
        <div class="cart-item-info">
          <h5>${escapeHtml(item.name)}</h5>
          <p>${escapeHtml(item.strength || '')} • ₹${item.price.toFixed(2)}</p>
          <p style="font-size: 0.7rem; color: #059669;">Store: ${escapeHtml(item.pharmacyName || 'Partner Pharmacy')}</p>
        </div>
        <div class="cart-qty-ctrls">
          <button class="cart-qty-btn btn-minus" data-id="${item.inventoryId}">-</button>
          <span style="font-size: 0.88rem; font-weight: 700;">${item.quantity}</span>
          <button class="cart-qty-btn btn-plus" data-id="${item.inventoryId}">+</button>
        </div>
      </div>
    `).join('');

    cartItemsList.querySelectorAll('.btn-minus').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-id'));
        API.updateCartQty(id, -1);
        updateCartUI();
        renderCartDrawer();
      });
    });

    cartItemsList.querySelectorAll('.btn-plus').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = Number(btn.getAttribute('data-id'));
        API.updateCartQty(id, 1);
        updateCartUI();
        renderCartDrawer();
      });
    });

    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = subtotal * 0.05;
    const grand = subtotal + tax;

    cartSubtotal.textContent = `₹${subtotal.toFixed(2)}`;
    cartTax.textContent = `₹${tax.toFixed(2)}`;
    cartGrandTotal.textContent = `₹${grand.toFixed(2)}`;
  }

  // Session handler
  function initUserSession() {
    const myOrdersHeaderBtn = document.getElementById('myOrdersHeaderBtn');
    if (myOrdersHeaderBtn) {
      myOrdersHeaderBtn.onclick = (e) => {
        const currentUser = API.getUserSession();
        if (!currentUser || currentUser.role !== 'ROLE_CUSTOMER' || !(currentUser.userId || currentUser.id)) {
          e.preventDefault();
          window.location.href = 'customer-login.html?returnUrl=my-orders.html';
        }
      };
    }

    if (!userNavContainer) return;
    const user = API.getUserSession();
    if (user && user.role === 'ROLE_CUSTOMER') {
      userNavContainer.innerHTML = `
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <div style="display: flex; flex-direction: column; text-align: right;">
            <span style="font-size: 0.82rem; font-weight: 700; color: var(--text-main);">Hi, ${escapeHtml(user.fullName.split(' ')[0])}</span>
            <span style="font-size: 0.7rem; color: #059669; font-weight: 600;">Customer</span>
          </div>
          <a href="my-orders.html" class="btn btn-outline-primary" style="padding: 0.35rem 0.65rem; font-size: 0.75rem;">📦 My Orders</a>
          <button class="btn btn-outline-primary" style="padding: 0.35rem 0.65rem; font-size: 0.75rem; color: #dc2626; border-color: #fca5a5;" id="logoutCustomerBtn">Logout</button>
        </div>
      `;
      document.getElementById('logoutCustomerBtn').addEventListener('click', () => {
        API.clearSession();
        window.location.reload();
      });
    } else {
      userNavContainer.innerHTML = `
        <a href="customer-login.html" class="btn btn-primary" style="font-size: 0.8rem; padding: 0.45rem 0.8rem; font-weight: 700;">
          👤 Sign In
        </a>
      `;
    }
  }

  function getCategoryIcon(cat) {
    switch (cat.toLowerCase()) {
      case 'tablets': return '💊';
      case 'syrups': return '🧪';
      case 'first aid': return '🩹';
      case 'vitamins': return '🍊';
      case 'personal care': return '🧴';
      case 'healthcare devices': return '🩺';
      default: return '🏥';
    }
  }

  function getProductSvg(category) {
    const c = (category || '').toLowerCase();
    if (c.includes('tablet')) {
      return `<svg class="product-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="6" width="18" height="12" rx="6"></rect><line x1="12" y1="6" x2="12" y2="18"></line></svg>`;
    } else if (c.includes('syrup')) {
      return `<svg class="product-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M8 2h8v4H8zM6 6h12v14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2z"></path><line x1="6" y1="11" x2="18" y2="11"></line></svg>`;
    } else if (c.includes('first aid')) {
      return `<svg class="product-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="3" y="4" width="18" height="16" rx="4"></rect><line x1="12" y1="8" x2="12" y2="16"></line><line x1="8" y1="12" x2="16" y2="12"></line></svg>`;
    } else if (c.includes('device')) {
      return `<svg class="product-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><path d="M6 12h3l2-4 3 8 2-4h2"></path></svg>`;
    } else {
      return `<svg class="product-svg-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"></path></svg>`;
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }
});
