/**
 * DisasterConnect - Shelters & Resources Module
 * Handles Shelter directory & capacity, Essential Services, and Relief Resources shortage overview.
 */

import { ApiClient, showToast, escapeHtml } from '../api.js';
import { AuthService } from '../auth.js';

export const SheltersModule = {
    async renderShelters(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Shelters & Essential Services</h1>
                    <p class="page-subtitle">Find safe evacuation centers, emergency relief camps, medical clinics and food distribution hubs.</p>
                </div>
                ${AuthService.hasRole('AUTHORITY', 'ADMIN') ? `
                    <button class="btn btn-primary" id="btn-add-shelter">
                        <span>➕</span> Add New Facility
                    </button>
                ` : ''}
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-shelters-all">All Facilities</button>
                <button class="tab-btn" id="tab-shelters-only">Shelters Only</button>
                <button class="tab-btn" id="tab-services-only">Medical & Food Services</button>
            </div>

            <div class="filter-bar">
                <input type="text" id="filter-facility-search" class="search-input" placeholder="Search by name, address or city..." />
                <button class="btn btn-outline btn-sm" id="btn-refresh-facilities">Refresh</button>
            </div>

            <div id="facilities-grid" class="grid">
                <div class="loader"></div>
            </div>
        `;

        let currentTypeFilter = null;

        const loadFacilities = async () => {
            const grid = document.getElementById('facilities-grid');
            grid.innerHTML = `<div class="loader"></div>`;
            const searchKeyword = (document.getElementById('filter-facility-search').value || '').toLowerCase().trim();

            try {
                const params = {};
                if (currentTypeFilter) params.type = currentTypeFilter;

                const res = await ApiClient.get('/shelters', params);
                let facilities = ApiClient.unbox(res);

                if (searchKeyword) {
                    facilities = facilities.filter(f => 
                        (f.name && f.name.toLowerCase().includes(searchKeyword)) ||
                        (f.address && f.address.toLowerCase().includes(searchKeyword)) ||
                        (f.city && f.city.toLowerCase().includes(searchKeyword))
                    );
                }

                if (facilities.length === 0) {
                    grid.innerHTML = `
                        <div class="empty-state" style="grid-column:1/-1;">
                            <div class="empty-state-icon">🏕️</div>
                            <h3>No facilities found</h3>
                            <p>There are no emergency facilities matching your filter.</p>
                        </div>
                    `;
                    return;
                }

                grid.innerHTML = facilities.map(s => {
                    const cap = s.capacity || 0;
                    const avail = s.availableSpace ?? cap;
                    const occ = Math.max(0, cap - avail);
                    const pct = cap > 0 ? Math.min(100, Math.round((occ / cap) * 100)) : 0;
                    const isFull = s.status === 'FULL' || (cap > 0 && avail <= 0);

                    return `
                        <div class="card">
                            <div class="card-header">
                                <span class="status-badge ${isFull ? 'badge-CRITICAL' : 'badge-ACTIVE'}">
                                    ${escapeHtml(s.status || (isFull ? 'FULL' : 'ACTIVE'))}
                                </span>
                                <small class="text-muted">${escapeHtml(s.serviceType || 'SHELTER')}</small>
                            </div>
                            <h3 class="card-title">${escapeHtml(s.name)}</h3>
                            <div class="card-body">
                                <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.75rem;">
                                    <div>📍 <strong>Address:</strong> ${escapeHtml(s.address)} ${s.city ? `(${escapeHtml(s.city)})` : ''}</div>
                                    ${s.contactPhone ? `<div>📞 <strong>Phone:</strong> ${escapeHtml(s.contactPhone)}</div>` : ''}
                                    ${s.contactEmail ? `<div>✉️ <strong>Email:</strong> ${escapeHtml(s.contactEmail)}</div>` : ''}
                                </div>
                                ${cap > 0 ? `
                                    <div style="margin-top: 0.5rem;">
                                        <div style="display:flex; justify-content:space-between; font-size:0.8rem; margin-bottom:0.25rem;">
                                            <span>Available: ${avail} / ${cap}</span>
                                            <span>${pct}% Filled</span>
                                        </div>
                                        <div style="background:var(--border); border-radius:999px; height:8px; overflow:hidden;">
                                            <div style="background:${pct > 85 ? 'var(--primary)' : 'var(--success)'}; width:${pct}%; height:100%;"></div>
                                        </div>
                                    </div>
                                ` : ''}
                            </div>
                            <div class="card-footer">
                                <span>Lat/Lng: ${s.latitude ? `${s.latitude.toFixed(2)}, ${s.longitude.toFixed(2)}` : 'N/A'}</span>
                            </div>
                        </div>
                    `;
                }).join('');

            } catch (err) {
                grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const tabAll = document.getElementById('tab-shelters-all');
        const tabShelters = document.getElementById('tab-shelters-only');
        const tabServices = document.getElementById('tab-services-only');

        tabAll.addEventListener('click', () => {
            tabAll.classList.add('active');
            tabShelters.classList.remove('active');
            tabServices.classList.remove('active');
            currentTypeFilter = null;
            loadFacilities();
        });

        tabShelters.addEventListener('click', () => {
            tabShelters.classList.add('active');
            tabAll.classList.remove('active');
            tabServices.classList.remove('active');
            currentTypeFilter = 'SHELTER';
            loadFacilities();
        });

        tabServices.addEventListener('click', () => {
            tabServices.classList.add('active');
            tabAll.classList.remove('active');
            tabShelters.classList.remove('active');
            currentTypeFilter = 'MEDICAL';
            loadFacilities();
        });

        document.getElementById('btn-refresh-facilities').addEventListener('click', loadFacilities);
        document.getElementById('filter-facility-search').addEventListener('input', () => {
            clearTimeout(window._facTimeout);
            window._facTimeout = setTimeout(loadFacilities, 300);
        });

        if (document.getElementById('btn-add-shelter')) {
            document.getElementById('btn-add-shelter').addEventListener('click', () => {
                this.showAddFacilityDialog(loadFacilities);
            });
        }

        loadFacilities();
    },

    showAddFacilityDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Add Emergency Facility</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="facility-form">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label">Service Type *</label>
                            <select name="serviceType" class="form-control" required>
                                <option value="SHELTER">Emergency Shelter</option>
                                <option value="MEDICAL">Medical Center / Hospital</option>
                                <option value="FOOD_DISTRIBUTION">Food Distribution Hub</option>
                                <option value="WATER_POINT">Clean Water Station</option>
                                <option value="OTHER">Other Essential Service</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Facility Name *</label>
                            <input type="text" name="name" class="form-control" required placeholder="e.g. City Community Center Shelter" />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Address *</label>
                                <input type="text" name="address" class="form-control" required placeholder="Street address or landmark" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">City / District</label>
                                <input type="text" name="city" class="form-control" placeholder="Chennai" />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Total Capacity</label>
                                <input type="number" name="capacity" class="form-control" value="200" min="0" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Available Space</label>
                                <input type="number" name="availableSpace" class="form-control" value="200" min="0" />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Contact Phone</label>
                                <input type="text" name="contactPhone" class="form-control" placeholder="+91 98765 43210" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Latitude *</label>
                                <input type="number" step="any" name="latitude" class="form-control" required placeholder="13.0827" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Longitude *</label>
                                <input type="number" step="any" name="longitude" class="form-control" required placeholder="80.2707" />
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">Save Facility</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#facility-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const payload = {
                name: fd.get('name'),
                serviceType: fd.get('serviceType'),
                address: fd.get('address'),
                city: fd.get('city') || null,
                capacity: fd.get('capacity') ? parseInt(fd.get('capacity'), 10) : 0,
                availableSpace: fd.get('availableSpace') ? parseInt(fd.get('availableSpace'), 10) : 0,
                contactPhone: fd.get('contactPhone') || null,
                latitude: parseFloat(fd.get('latitude')),
                longitude: parseFloat(fd.get('longitude'))
            };

            try {
                await ApiClient.post('/shelters', payload);
                showToast('Emergency facility saved successfully!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to save facility', 'error');
            }
        });
    },

    // ---------------- RELIEF RESOURCES & SHORTAGES ----------------
    async renderResources(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Relief Resources & Shortages</h1>
                    <p class="page-subtitle">Track resource inventory, community supplies, and critical supply shortages.</p>
                </div>
                ${AuthService.hasRole('NGO', 'ADMIN') ? `
                    <button class="btn btn-primary" id="btn-add-resource">
                        <span>📦</span> Record Stock Entry
                    </button>
                ` : ''}
            </div>

            <div class="filter-bar">
                <input type="text" id="filter-res-location" class="search-input" placeholder="Filter by area / location name..." />
                <button class="btn btn-outline btn-sm" id="btn-refresh-resources">Refresh</button>
            </div>

            <div id="resources-grid" class="grid">
                <div class="loader"></div>
            </div>
        `;

        const loadResources = async () => {
            const grid = document.getElementById('resources-grid');
            grid.innerHTML = `<div class="loader"></div>`;
            const location = document.getElementById('filter-res-location').value.trim();

            try {
                const params = {};
                if (location) params.location = location;

                const res = await ApiClient.get('/resources', params);
                const items = ApiClient.unbox(res);

                if (items.length === 0) {
                    grid.innerHTML = `
                        <div class="empty-state" style="grid-column:1/-1;">
                            <div class="empty-state-icon">📦</div>
                            <h3>No resource records found</h3>
                            <p>NGOs and Administrators record inventory to detect and address supply shortages.</p>
                        </div>
                    `;
                    return;
                }

                grid.innerHTML = items.map(r => {
                    const isShortage = r.status === 'LOW' || r.status === 'CRITICAL' || (r.availableQuantity < r.requiredQuantity);
                    return `
                        <div class="card">
                            <div class="card-header">
                                <span class="status-badge ${isShortage ? 'badge-CRITICAL' : 'badge-ACTIVE'}">
                                    ${escapeHtml(r.status || (isShortage ? 'SHORTAGE' : 'ADEQUATE'))}
                                </span>
                                <small class="text-muted">${escapeHtml(r.resourceType || 'SUPPLIES')}</small>
                            </div>
                            <h3 class="card-title">${escapeHtml(r.resourceType)} at ${escapeHtml(r.location)}</h3>
                            <div class="card-body">
                                <div style="font-size: 0.9rem; margin-bottom: 0.5rem;">
                                    <div>📍 <strong>Location / Area:</strong> ${escapeHtml(r.location)}</div>
                                    <div style="margin-top:0.5rem; display:flex; gap:1.5rem;">
                                        <div>
                                            <span style="font-size:0.75rem; color:var(--text-muted); display:block;">AVAILABLE</span>
                                            <strong style="font-size:1.1rem; color:var(--success);">${r.availableQuantity ?? 0}</strong> ${escapeHtml(r.unit || 'units')}
                                        </div>
                                        <div>
                                            <span style="font-size:0.75rem; color:var(--text-muted); display:block;">REQUIRED</span>
                                            <strong style="font-size:1.1rem; color:var(--primary);">${r.requiredQuantity ?? 0}</strong> ${escapeHtml(r.unit || 'units')}
                                        </div>
                                    </div>
                                    ${r.notes ? `<div style="margin-top:0.5rem; font-size:0.8rem; color:var(--text-muted);">${escapeHtml(r.notes)}</div>` : ''}
                                </div>
                            </div>
                            <div class="card-footer">
                                <span>Updated: ${r.updatedAt ? new Date(r.updatedAt).toLocaleDateString() : 'Recent'}</span>
                            </div>
                        </div>
                    `;
                }).join('');

            } catch (err) {
                grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        document.getElementById('btn-refresh-resources').addEventListener('click', loadResources);
        document.getElementById('filter-res-location').addEventListener('input', () => {
            clearTimeout(window._resTimeout);
            window._resTimeout = setTimeout(loadResources, 350);
        });

        if (document.getElementById('btn-add-resource')) {
            document.getElementById('btn-add-resource').addEventListener('click', () => {
                this.showAddResourceDialog(loadResources);
            });
        }

        loadResources();
    },

    showAddResourceDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Record Relief Resource</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="resource-form">
                    <div class="modal-body">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Resource Type *</label>
                                <select name="resourceType" class="form-control" required>
                                    <option value="WATER">Water</option>
                                    <option value="FOOD">Food / Rations</option>
                                    <option value="MEDICAL">Medical Supplies</option>
                                    <option value="CLOTHING">Blankets & Clothing</option>
                                    <option value="SHELTER">Tents & Tarps</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Location / Area *</label>
                                <input type="text" name="location" class="form-control" required placeholder="e.g. South Sector Relief Depot" />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Available Qty *</label>
                                <input type="number" step="any" name="availableQuantity" class="form-control" value="0" min="0" required />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Required Qty *</label>
                                <input type="number" step="any" name="requiredQuantity" class="form-control" value="100" min="0.01" required />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Unit *</label>
                                <input type="text" name="unit" class="form-control" value="kits" required placeholder="kits, boxes, liters" />
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Operational Notes</label>
                            <input type="text" name="notes" class="form-control" placeholder="Storage location or distribution instructions" />
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">Save Resource</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#resource-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const payload = {
                resourceType: fd.get('resourceType'),
                location: fd.get('location'),
                availableQuantity: parseFloat(fd.get('availableQuantity')),
                requiredQuantity: parseFloat(fd.get('requiredQuantity')),
                unit: fd.get('unit'),
                notes: fd.get('notes') || null
            };

            try {
                await ApiClient.post('/resources', payload);
                showToast('Resource stock recorded successfully!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to save resource', 'error');
            }
        });
    }
};
