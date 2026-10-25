/**
 * DisasterConnect - Citizen Module
 * Handles Alerts, Incident Reporting & My Incidents, Emergency Assistance Requests & Tracker.
 */

import { ApiClient, showToast, escapeHtml } from '../api.js';
import { AuthService } from '../auth.js';

export const CitizenModule = {
    // ---------------- ALERTS ----------------
    async renderAlerts(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Active Disaster Alerts</h1>
                    <p class="page-subtitle">Real-time emergency warnings and advisories broadcast by authorities.</p>
                </div>
                ${AuthService.hasRole('AUTHORITY', 'ADMIN') ? `
                    <button class="btn btn-primary" id="btn-create-alert">
                        <span>📢</span> Issue New Alert
                    </button>
                ` : ''}
            </div>

            <div class="filter-bar">
                <input type="text" id="filter-alert-keyword" class="search-input" placeholder="Search by title, location or keywords..." />
                <select id="filter-alert-severity" class="select-input">
                    <option value="">All Severities</option>
                    <option value="CRITICAL">Critical</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                </select>
                <select id="filter-alert-status" class="select-input">
                    <option value="ACTIVE" selected>Active Only</option>
                    <option value="">All Statuses</option>
                    <option value="RESOLVED">Resolved</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
                <button class="btn btn-outline btn-sm" id="btn-refresh-alerts">Refresh</button>
            </div>

            <div id="alerts-grid" class="grid">
                <div style="grid-column: 1/-1; text-align: center;"><div class="loader"></div></div>
            </div>
        `;

        const loadAlerts = async () => {
            const grid = document.getElementById('alerts-grid');
            const severity = document.getElementById('filter-alert-severity').value;
            const status = document.getElementById('filter-alert-status').value;
            const keyword = document.getElementById('filter-alert-keyword').value.toLowerCase().trim();

            try {
                const params = {};
                if (status) params.status = status;

                const res = await ApiClient.get('/alerts', params);
                let alerts = ApiClient.unbox(res);

                if (severity) {
                    alerts = alerts.filter(a => a.severity === severity);
                }

                if (keyword) {
                    alerts = alerts.filter(a => 
                        (a.title && a.title.toLowerCase().includes(keyword)) ||
                        (a.description && a.description.toLowerCase().includes(keyword)) ||
                        (a.affectedArea && a.affectedArea.toLowerCase().includes(keyword)) ||
                        (a.disasterType && a.disasterType.toLowerCase().includes(keyword))
                    );
                }

                if (alerts.length === 0) {
                    grid.innerHTML = `
                        <div class="empty-state" style="grid-column: 1/-1;">
                            <div class="empty-state-icon">🛡️</div>
                            <h3>No active alerts matching your criteria</h3>
                            <p>Check back later or adjust your filters.</p>
                        </div>
                    `;
                    return;
                }

                grid.innerHTML = alerts.map(alert => {
                    const alertId = alert.alertId || alert.id;
                    return `
                        <div class="card">
                            <div class="card-header">
                                <div>
                                    <span class="status-badge badge-${escapeHtml(alert.severity || 'INFO')}">${escapeHtml(alert.severity)}</span>
                                    <span class="status-badge badge-${escapeHtml(alert.status || 'ACTIVE')}" style="margin-left: 0.35rem;">${escapeHtml(alert.status)}</span>
                                </div>
                                <small class="text-muted">${alert.disasterType || 'DISASTER'}</small>
                            </div>
                            <h3 class="card-title" style="margin-bottom: 0.5rem;">${escapeHtml(alert.title)}</h3>
                            <div class="card-body">
                                <p>${escapeHtml(alert.description)}</p>
                                <div style="margin-top: 0.75rem; font-size: 0.85rem; color: var(--text-muted);">
                                    <div>📍 <strong>Affected Area:</strong> ${escapeHtml(alert.affectedArea || 'Not specified')}</div>
                                    ${alert.safetyInstructions ? `<div>ℹ️ <strong>Instructions:</strong> ${escapeHtml(alert.safetyInstructions)}</div>` : ''}
                                </div>
                            </div>
                            <div class="card-footer">
                                <span>Broadcast: ${alert.issuedAt ? new Date(alert.issuedAt).toLocaleString() : 'Recent'}</span>
                                ${AuthService.hasRole('AUTHORITY', 'ADMIN') && alert.status === 'ACTIVE' ? `
                                    <button class="btn btn-outline btn-sm btn-resolve-alert" data-id="${alertId}">Resolve Alert</button>
                                ` : ''}
                            </div>
                        </div>
                    `;
                }).join('');

                grid.querySelectorAll('.btn-resolve-alert').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const alertId = e.target.getAttribute('data-id');
                        if (confirm('Are you sure you want to mark this alert as resolved?')) {
                            try {
                                await ApiClient.patch(`/alerts/${alertId}/status`, { status: 'RESOLVED' });
                                showToast('Alert marked as resolved', 'success');
                                loadAlerts();
                            } catch (err) {
                                showToast(err.message || 'Failed to update alert', 'error');
                            }
                        }
                    });
                });

            } catch (err) {
                grid.innerHTML = `
                    <div class="empty-state" style="grid-column: 1/-1;">
                        <div class="empty-state-icon">⚠️</div>
                        <h3>Failed to load alerts</h3>
                        <p>${escapeHtml(err.message)}</p>
                    </div>
                `;
            }
        };

        document.getElementById('btn-refresh-alerts').addEventListener('click', loadAlerts);
        document.getElementById('filter-alert-severity').addEventListener('change', loadAlerts);
        document.getElementById('filter-alert-status').addEventListener('change', loadAlerts);
        document.getElementById('filter-alert-keyword').addEventListener('input', () => {
            clearTimeout(window._alertSearchTimeout);
            window._alertSearchTimeout = setTimeout(loadAlerts, 300);
        });

        if (document.getElementById('btn-create-alert')) {
            document.getElementById('btn-create-alert').addEventListener('click', () => {
                this.showCreateAlertDialog(loadAlerts);
            });
        }

        loadAlerts();
    },

    showCreateAlertDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Broadcast Disaster Alert</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="create-alert-form">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label">Alert Title *</label>
                            <input type="text" name="title" class="form-control" required placeholder="e.g. Flash Flood Warning" />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Disaster Type *</label>
                                <select name="disasterType" class="form-control" required>
                                    <option value="FLOOD">Flood</option>
                                    <option value="EARTHQUAKE">Earthquake</option>
                                    <option value="CYCLONE">Cyclone</option>
                                    <option value="FIRE">Fire</option>
                                    <option value="LANDSLIDE">Landslide</option>
                                    <option value="TSUNAMI">Tsunami</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Severity Level *</label>
                                <select name="severity" class="form-control" required>
                                    <option value="CRITICAL">Critical</option>
                                    <option value="HIGH" selected>High</option>
                                    <option value="MEDIUM">Medium</option>
                                    <option value="LOW">Low</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Affected Area / Location *</label>
                            <input type="text" name="affectedArea" class="form-control" required placeholder="e.g. Downtown Metro District" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Safety Instructions & Advisory</label>
                            <input type="text" name="safetyInstructions" class="form-control" placeholder="Evacuate to elevated ground, stay tuned to emergency broadcast..." />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Advisory / Description *</label>
                            <textarea name="description" rows="3" class="form-control" required placeholder="Detailed description of the emergency event..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">Broadcast Alert</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#create-alert-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const data = {
                title: fd.get('title'),
                disasterType: fd.get('disasterType'),
                severity: fd.get('severity'),
                affectedArea: fd.get('affectedArea'),
                safetyInstructions: fd.get('safetyInstructions'),
                description: fd.get('description')
            };

            try {
                await ApiClient.post('/alerts', data);
                showToast('Alert broadcasted successfully!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to broadcast alert', 'error');
            }
        });
    },

    // ---------------- INCIDENT REPORTING ----------------
    async renderIncidents(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Disaster Incidents</h1>
                    <p class="page-subtitle">Crowdsourced incident reports with verification status and geolocation.</p>
                </div>
                ${AuthService.isAuthenticated() ? `
                    <button class="btn btn-primary" id="btn-report-incident">
                        <span>📝</span> Report An Incident
                    </button>
                ` : `
                    <button class="btn btn-outline" id="btn-login-to-report">
                        Sign in to Report Incident
                    </button>
                `}
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-all-incidents">Community Incidents</button>
                ${AuthService.isAuthenticated() ? `<button class="tab-btn" id="tab-my-incidents">My Submitted Reports</button>` : ''}
            </div>

            <div id="incidents-container">
                <div class="loader"></div>
            </div>
        `;

        const showAll = async () => {
            const list = document.getElementById('incidents-container');
            list.innerHTML = `<div class="loader"></div>`;
            try {
                let endpoint = '/incidents';
                const isPrivileged = AuthService.hasRole('AUTHORITY', 'ADMIN', 'NGO');
                
                if (!isPrivileged && AuthService.isAuthenticated()) {
                    endpoint = '/incidents/my';
                }

                const res = await ApiClient.get(endpoint);
                const incidents = ApiClient.unbox(res);
                this.renderIncidentList(list, incidents, isPrivileged, showAll);
            } catch (err) {
                list.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">📋</div>
                        <h3>Unable to load incidents</h3>
                        <p>${escapeHtml(err.message)}</p>
                    </div>
                `;
            }
        };

        const showMy = async () => {
            const list = document.getElementById('incidents-container');
            list.innerHTML = `<div class="loader"></div>`;
            try {
                const res = await ApiClient.get('/incidents/my');
                const incidents = ApiClient.unbox(res);
                this.renderIncidentList(list, incidents, false, showMy);
            } catch (err) {
                list.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <p>${escapeHtml(err.message)}</p>
                    </div>
                `;
            }
        };

        const allTab = document.getElementById('tab-all-incidents');
        const myTab = document.getElementById('tab-my-incidents');

        allTab.addEventListener('click', () => {
            allTab.classList.add('active');
            if (myTab) myTab.classList.remove('active');
            showAll();
        });

        if (myTab) {
            myTab.addEventListener('click', () => {
                myTab.classList.add('active');
                allTab.classList.remove('active');
                showMy();
            });
        }

        if (document.getElementById('btn-report-incident')) {
            document.getElementById('btn-report-incident').addEventListener('click', () => {
                this.showReportIncidentDialog(showAll);
            });
        }

        if (document.getElementById('btn-login-to-report')) {
            document.getElementById('btn-login-to-report').addEventListener('click', () => {
                window.location.hash = '#login';
            });
        }

        showAll();
    },

    renderIncidentList(container, incidents, isPrivileged, reloadFn) {
        if (!incidents || incidents.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">✅</div>
                    <h3>No incidents reported yet</h3>
                    <p>Community reports help emergency services coordinate rapid response.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="grid">
                ${incidents.map(inc => {
                    const id = inc.incidentId || inc.id;
                    return `
                        <div class="card">
                            <div class="card-header">
                                <div>
                                    <span class="status-badge badge-${escapeHtml(inc.severity || 'INFO')}">${escapeHtml(inc.severity)}</span>
                                    <span class="status-badge badge-${escapeHtml(inc.status || 'REPORTED')}" style="margin-left: 0.35rem;">${escapeHtml(inc.status)}</span>
                                </div>
                                <small class="text-muted">${inc.incidentType || 'INCIDENT'}</small>
                            </div>
                            <h3 class="card-title">${escapeHtml(inc.description ? inc.description.substring(0, 60) + '...' : 'Incident #' + id)}</h3>
                            <div class="card-body">
                                <p>${escapeHtml(inc.description)}</p>
                                <div style="margin-top: 0.75rem; font-size: 0.85rem; color: var(--text-muted);">
                                    <div>📍 <strong>Location:</strong> ${escapeHtml(inc.locationName || 'Unknown')}</div>
                                    ${inc.latitude ? `<div>🌐 <strong>Coords:</strong> ${inc.latitude.toFixed(4)}, ${inc.longitude.toFixed(4)}</div>` : ''}
                                </div>
                            </div>
                            <div class="card-footer">
                                <span>Reported: ${inc.createdAt ? new Date(inc.createdAt).toLocaleDateString() : 'Recent'}</span>
                                ${isPrivileged ? `
                                    <div style="display: flex; gap: 0.35rem;">
                                        ${inc.status !== 'VERIFIED' ? `
                                            <button class="btn btn-outline btn-sm btn-verify-incident" data-id="${id}">Verify</button>
                                        ` : ''}
                                        ${inc.status !== 'RESOLVED' ? `
                                            <button class="btn btn-outline btn-sm btn-resolve-incident" data-id="${id}">Resolve</button>
                                        ` : ''}
                                    </div>
                                ` : ''}
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;

        if (isPrivileged) {
            container.querySelectorAll('.btn-verify-incident').forEach(btn => {
                btn.addEventListener('click', async (e) => {
                    const id = e.target.getAttribute('data-id');
                    try {
                        await ApiClient.patch(`/incidents/${id}/status`, { status: 'VERIFIED' });
                        showToast('Incident verified', 'success');
                        reloadFn();
                    } catch (err) {
                        showToast(err.message || 'Failed to verify incident', 'error');
                    }
                });
            });

            container.querySelectorAll('.btn-resolve-incident').forEach(btn => {
                btn.addEventListener('click', async (e) => {
                    const id = e.target.getAttribute('data-id');
                    try {
                        await ApiClient.patch(`/incidents/${id}/status`, { status: 'RESOLVED' });
                        showToast('Incident marked as resolved', 'success');
                        reloadFn();
                    } catch (err) {
                        showToast(err.message || 'Failed to resolve incident', 'error');
                    }
                });
            });
        }
    },

    showReportIncidentDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Submit Incident Report</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="report-incident-form">
                    <div class="modal-body">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Incident Type *</label>
                                <select name="incidentType" class="form-control" required>
                                    <option value="FLOOD">Flood</option>
                                    <option value="ROAD_BLOCK">Road Blockage</option>
                                    <option value="FIRE">Fire</option>
                                    <option value="BUILDING_COLLAPSE">Building Collapse</option>
                                    <option value="MEDICAL_EMERGENCY">Medical Emergency</option>
                                    <option value="POWER_OUTAGE">Power Outage</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Severity Level *</label>
                                <select name="severity" class="form-control" required>
                                    <option value="CRITICAL">Critical</option>
                                    <option value="HIGH">High</option>
                                    <option value="MEDIUM" selected>Medium</option>
                                    <option value="LOW">Low</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Location / Landmark *</label>
                            <input type="text" name="locationName" class="form-control" required placeholder="Street name, landmark, district..." />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Latitude</label>
                                <input type="number" step="any" name="latitude" id="inc-lat" class="form-control" placeholder="13.0827" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Longitude</label>
                                <input type="number" step="any" name="longitude" id="inc-lng" class="form-control" placeholder="80.2707" />
                            </div>
                            <div class="form-group" style="display:flex; align-items:flex-end;">
                                <button type="button" class="btn btn-outline btn-sm" id="btn-detect-loc" style="width:100%; height:38px;">📍 Auto GPS</button>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Description Details *</label>
                            <textarea name="description" rows="3" class="form-control" required placeholder="Provide clear details on damages, dangers, or urgent requirements..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">Submit Report</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#btn-detect-loc').addEventListener('click', () => {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    pos => {
                        modal.querySelector('#inc-lat').value = pos.coords.latitude.toFixed(6);
                        modal.querySelector('#inc-lng').value = pos.coords.longitude.toFixed(6);
                        showToast('Location coordinates detected!', 'success');
                    },
                    err => showToast('Could not fetch GPS coordinates: ' + err.message, 'warning')
                );
            } else {
                showToast('Geolocation is not supported by your browser', 'warning');
            }
        });

        modal.querySelector('#report-incident-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const data = {
                incidentType: fd.get('incidentType'),
                severity: fd.get('severity'),
                locationName: fd.get('locationName'),
                latitude: fd.get('latitude') ? parseFloat(fd.get('latitude')) : null,
                longitude: fd.get('longitude') ? parseFloat(fd.get('longitude')) : null,
                description: fd.get('description')
            };

            try {
                await ApiClient.post('/incidents', data);
                showToast('Incident reported successfully!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to submit incident report', 'error');
            }
        });
    },

    // ---------------- EMERGENCY ASSISTANCE REQUESTS ----------------
    async renderAssistance(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Emergency Assistance</h1>
                    <p class="page-subtitle">Request urgent food, water, medical rescue, or shelter assistance.</p>
                </div>
                ${AuthService.isAuthenticated() ? `
                    <button class="btn btn-danger" id="btn-request-help">
                        <span>🆘</span> Request Immediate Help
                    </button>
                ` : `
                    <button class="btn btn-outline" id="btn-login-to-request">
                        Sign in to Request Assistance
                    </button>
                `}
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-my-requests">My Requests</button>
                ${AuthService.hasRole('VOLUNTEER', 'NGO', 'ADMIN', 'AUTHORITY') ? `
                    <button class="tab-btn" id="tab-available-requests">Community Help Queue</button>
                ` : ''}
            </div>

            <div id="assistance-container">
                <div class="loader"></div>
            </div>
        `;

        const loadMyRequests = async () => {
            const list = document.getElementById('assistance-container');
            list.innerHTML = `<div class="loader"></div>`;
            try {
                const res = await ApiClient.get('/assistance-requests/my');
                const requests = ApiClient.unbox(res);
                
                if (requests.length === 0) {
                    list.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">🤝</div>
                            <h3>No active assistance requests</h3>
                            <p>If you or someone nearby is in danger or needs critical supplies, click "Request Immediate Help".</p>
                        </div>
                    `;
                    return;
                }

                list.innerHTML = `
                    <div class="grid">
                        ${requests.map(req => {
                            const reqId = req.requestId || req.id;
                            return `
                                <div class="card">
                                    <div class="card-header">
                                        <span class="status-badge badge-${escapeHtml(req.priority || 'HIGH')}">${escapeHtml(req.priority || 'HIGH')}</span>
                                        <span class="status-badge badge-${escapeHtml(req.status || 'PENDING')}">${escapeHtml(req.status)}</span>
                                    </div>
                                    <h3 class="card-title">${escapeHtml(req.assistanceType || 'ASSISTANCE')}</h3>
                                    <div class="card-body">
                                        <p>${escapeHtml(req.description)}</p>
                                        <div style="margin-top: 0.75rem; font-size: 0.85rem; color: var(--text-muted);">
                                            <div>📍 <strong>Location:</strong> ${escapeHtml(req.locationName || 'Unknown')}</div>
                                            <div>👥 <strong>Persons in Need:</strong> ${req.peopleCount || 1}</div>
                                            ${req.assignedTo ? `<div>👤 <strong>Assigned Helper:</strong> ${escapeHtml(req.assignedTo.name)}</div>` : ''}
                                        </div>
                                    </div>
                                    <div class="card-footer">
                                        <span>${req.createdAt ? new Date(req.createdAt).toLocaleDateString() : 'Recent'}</span>
                                        ${req.status === 'PENDING' || req.status === 'ASSIGNED' ? `
                                            <button class="btn btn-outline btn-sm btn-cancel-request" data-id="${reqId}">Cancel</button>
                                        ` : ''}
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `;

                list.querySelectorAll('.btn-cancel-request').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        if (confirm('Are you sure you want to cancel this assistance request?')) {
                            try {
                                await ApiClient.patch(`/assistance-requests/${id}/cancel`);
                                showToast('Request cancelled', 'info');
                                loadMyRequests();
                            } catch (err) {
                                showToast(err.message || 'Failed to cancel request', 'error');
                            }
                        }
                    });
                });

            } catch (err) {
                list.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <p>${escapeHtml(err.message)}</p>
                    </div>
                `;
            }
        };

        const loadCommunityRequests = async () => {
            const list = document.getElementById('assistance-container');
            list.innerHTML = `<div class="loader"></div>`;
            try {
                const res = await ApiClient.get('/assistance-requests');
                const requests = ApiClient.unbox(res);
                
                list.innerHTML = `
                    <div class="grid">
                        ${requests.map(req => {
                            const reqId = req.requestId || req.id;
                            return `
                                <div class="card">
                                    <div class="card-header">
                                        <span class="status-badge badge-${escapeHtml(req.priority || 'HIGH')}">${escapeHtml(req.priority || 'HIGH')}</span>
                                        <span class="status-badge badge-${escapeHtml(req.status || 'PENDING')}">${escapeHtml(req.status)}</span>
                                    </div>
                                    <h3 class="card-title">${escapeHtml(req.assistanceType || 'HELP REQUEST')}</h3>
                                    <div class="card-body">
                                        <p>${escapeHtml(req.description)}</p>
                                        <div style="margin-top: 0.75rem; font-size: 0.85rem; color: var(--text-muted);">
                                            <div>📍 <strong>Location:</strong> ${escapeHtml(req.locationName || 'Unknown')}</div>
                                            <div>👥 <strong>People:</strong> ${req.peopleCount || 1}</div>
                                        </div>
                                    </div>
                                    <div class="card-footer">
                                        <span>${req.createdAt ? new Date(req.createdAt).toLocaleDateString() : 'Recent'}</span>
                                        ${req.status === 'PENDING' && AuthService.hasRole('VOLUNTEER', 'NGO', 'ADMIN') ? `
                                            <button class="btn btn-primary btn-sm btn-accept-help" data-id="${reqId}">Accept Task</button>
                                        ` : ''}
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `;

                list.querySelectorAll('.btn-accept-help').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        try {
                            await ApiClient.post(`/assistance-requests/${id}/accept`);
                            showToast('You accepted this assistance task!', 'success');
                            loadCommunityRequests();
                        } catch (err) {
                            showToast(err.message || 'Failed to accept task', 'error');
                        }
                    });
                });

            } catch (err) {
                list.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const myTab = document.getElementById('tab-my-requests');
        const commTab = document.getElementById('tab-available-requests');

        myTab.addEventListener('click', () => {
            myTab.classList.add('active');
            if (commTab) commTab.classList.remove('active');
            loadMyRequests();
        });

        if (commTab) {
            commTab.addEventListener('click', () => {
                commTab.classList.add('active');
                myTab.classList.remove('active');
                loadCommunityRequests();
            });
        }

        if (document.getElementById('btn-request-help')) {
            document.getElementById('btn-request-help').addEventListener('click', () => {
                this.showRequestHelpDialog(loadMyRequests);
            });
        }

        if (document.getElementById('btn-login-to-request')) {
            document.getElementById('btn-login-to-request').addEventListener('click', () => {
                window.location.hash = '#login';
            });
        }

        if (AuthService.isAuthenticated()) {
            loadMyRequests();
        } else {
            document.getElementById('assistance-container').innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🔒</div>
                    <h3>Sign In Required</h3>
                    <p>Please log in or register to submit and manage emergency assistance requests.</p>
                </div>
            `;
        }
    },

    showRequestHelpDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title" style="color:var(--primary);">Request Emergency Assistance</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="assistance-form">
                    <div class="modal-body">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Type of Help *</label>
                                <select name="assistanceType" class="form-control" required>
                                    <option value="RESCUE">Evacuation / Rescue</option>
                                    <option value="MEDICAL">Emergency Medical</option>
                                    <option value="FOOD_WATER">Food & Clean Water</option>
                                    <option value="SHELTER">Temporary Shelter</option>
                                    <option value="OTHER">Other Relief</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Priority *</label>
                                <select name="priority" class="form-control" required>
                                    <option value="CRITICAL">Critical (Life Threatening)</option>
                                    <option value="HIGH" selected>High (Urgent within hours)</option>
                                    <option value="MEDIUM">Medium (Within 24 hours)</option>
                                    <option value="LOW">Low</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Location / Address *</label>
                                <input type="text" name="locationName" class="form-control" required placeholder="Exact building, landmark or street" />
                            </div>
                            <div class="form-group" style="max-width:140px;">
                                <label class="form-label">People in Need *</label>
                                <input type="number" name="peopleCount" class="form-control" value="1" min="1" required />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Latitude</label>
                                <input type="number" step="any" name="latitude" id="help-lat" class="form-control" placeholder="13.0827" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Longitude</label>
                                <input type="number" step="any" name="longitude" id="help-lng" class="form-control" placeholder="80.2707" />
                            </div>
                            <div class="form-group" style="display:flex; align-items:flex-end;">
                                <button type="button" class="btn btn-outline btn-sm" id="btn-detect-help-loc" style="width:100%; height:38px;">📍 GPS</button>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Situation Details *</label>
                            <textarea name="description" rows="3" class="form-control" required placeholder="Describe condition, vulnerable individuals (infants, elderly), hazards..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-danger">Dispatch Request</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#btn-detect-help-loc').addEventListener('click', () => {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    pos => {
                        modal.querySelector('#help-lat').value = pos.coords.latitude.toFixed(6);
                        modal.querySelector('#help-lng').value = pos.coords.longitude.toFixed(6);
                        showToast('Location coordinates detected!', 'success');
                    },
                    err => showToast('Could not fetch GPS coordinates: ' + err.message, 'warning')
                );
            }
        });

        modal.querySelector('#assistance-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const data = {
                assistanceType: fd.get('assistanceType'),
                priority: fd.get('priority'),
                locationName: fd.get('locationName'),
                peopleCount: parseInt(fd.get('peopleCount'), 10),
                latitude: fd.get('latitude') ? parseFloat(fd.get('latitude')) : null,
                longitude: fd.get('longitude') ? parseFloat(fd.get('longitude')) : null,
                description: fd.get('description')
            };

            try {
                await ApiClient.post('/assistance-requests', data);
                showToast('Help request dispatched to response teams!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to dispatch request', 'error');
            }
        });
    }
};
