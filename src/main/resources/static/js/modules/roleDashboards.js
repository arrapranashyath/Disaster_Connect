/**
 * DisasterConnect - Role Dashboards Module
 * Provides specialized consoles for:
 * - VOLUNTEER: Task queue, accept tasks, update task fulfillment status.
 * - NGO: Verification status badge, coordination overview, volunteer assignment, stock management.
 * - AUTHORITY: Broadcast alerts, verify & manage community incidents, register relief shelters.
 * - ADMIN: System KPIs, NGO & Volunteer verification queues, User moderation/suspension, Audit logs.
 */

import { ApiClient, showToast, escapeHtml } from '../api.js';
import { AuthService } from '../auth.js';

export const RoleDashboardsModule = {
    // ========================================================
    // 1. VOLUNTEER DASHBOARD
    // ========================================================
    async renderVolunteer(container) {
        if (!AuthService.hasRole('VOLUNTEER', 'ADMIN')) {
            container.innerHTML = `<div class="empty-state"><h3>Access Denied</h3><p>Volunteer privileges required.</p></div>`;
            return;
        }

        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Volunteer Mission Console</h1>
                    <p class="page-subtitle">Accept community assistance requests, report task progress, and deliver frontline relief.</p>
                </div>
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-vol-tasks">My Assigned Tasks</button>
                <button class="tab-btn" id="tab-vol-open">Open Requests Queue</button>
            </div>

            <div id="volunteer-content">
                <div class="loader"></div>
            </div>
        `;

        const loadMyTasks = async () => {
            const content = document.getElementById('volunteer-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/coordination/my-tasks');
                const tasks = ApiClient.unbox(res);

                if (tasks.length === 0) {
                    content.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">📋</div>
                            <h3>No active tasks assigned</h3>
                            <p>Switch to the "Open Requests Queue" tab to volunteer for pending requests.</p>
                        </div>
                    `;
                    return;
                }

                content.innerHTML = `
                    <div class="grid">
                        ${tasks.map(t => {
                            const reqId = t.requestId || t.id;
                            return `
                                <div class="card">
                                    <div class="card-header">
                                        <span class="status-badge badge-${escapeHtml(t.priority || 'MEDIUM')}">${escapeHtml(t.priority || 'MEDIUM')}</span>
                                        <span class="status-badge badge-${escapeHtml(t.status || 'ASSIGNED')}">${escapeHtml(t.status || 'ASSIGNED')}</span>
                                    </div>
                                    <h3 class="card-title">${escapeHtml(t.assistanceType || 'Help Request')}</h3>
                                    <div class="card-body">
                                        <p>${escapeHtml(t.description)}</p>
                                        <div style="margin-top:0.75rem; font-size:0.85rem; color:var(--text-muted);">
                                            <div>📍 <strong>Location:</strong> ${escapeHtml(t.locationName || 'Location specified')}</div>
                                            <div>👥 <strong>People in Need:</strong> ${t.peopleCount || 1}</div>
                                            ${t.citizen && t.citizen.phone ? `<div>📞 <strong>Contact:</strong> ${escapeHtml(t.citizen.phone)} (${escapeHtml(t.citizen.name)})</div>` : ''}
                                        </div>
                                    </div>
                                    <div class="card-footer" style="flex-wrap:wrap; gap:0.5rem;">
                                        <select class="select-input select-status-update" data-id="${reqId}" style="padding:0.3rem 0.5rem; font-size:0.8rem;">
                                            <option value="">Update Status...</option>
                                            <option value="IN_PROGRESS" ${t.status === 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                                            <option value="COMPLETED">Fulfilled / Completed</option>
                                            <option value="CANCELLED">Unable to fulfill</option>
                                        </select>
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `;

                content.querySelectorAll('.select-status-update').forEach(select => {
                    select.addEventListener('change', async (e) => {
                        const newStatus = e.target.value;
                        const reqId = e.target.getAttribute('data-id');
                        if (!newStatus) return;

                        try {
                            await ApiClient.patch(`/assistance-requests/${reqId}/status`, {
                                status: newStatus,
                                notes: `Status updated by volunteer to ${newStatus}`
                            });
                            showToast('Mission status updated!', 'success');
                            loadMyTasks();
                        } catch (err) {
                            showToast(err.message || 'Status update failed', 'error');
                        }
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadOpenQueue = async () => {
            const content = document.getElementById('volunteer-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/assistance-requests', { status: 'PENDING' });
                const list = ApiClient.unbox(res);

                if (list.length === 0) {
                    content.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">✅</div>
                            <h3>No pending assistance requests</h3>
                            <p>All emergency requests are currently assigned or fulfilled!</p>
                        </div>
                    `;
                    return;
                }

                content.innerHTML = `
                    <div class="grid">
                        ${list.map(t => {
                            const reqId = t.requestId || t.id;
                            return `
                                <div class="card">
                                    <div class="card-header">
                                        <span class="status-badge badge-${escapeHtml(t.priority || 'HIGH')}">${escapeHtml(t.priority || 'HIGH')}</span>
                                        <span class="status-badge badge-PENDING">PENDING</span>
                                    </div>
                                    <h3 class="card-title">${escapeHtml(t.assistanceType || 'Urgent Assistance')}</h3>
                                    <div class="card-body">
                                        <p>${escapeHtml(t.description)}</p>
                                        <div style="margin-top:0.75rem; font-size:0.85rem; color:var(--text-muted);">
                                            <div>📍 <strong>Location:</strong> ${escapeHtml(t.locationName || 'Location specified')}</div>
                                            <div>👥 <strong>Persons:</strong> ${t.peopleCount || 1}</div>
                                        </div>
                                    </div>
                                    <div class="card-footer">
                                        <span>${t.createdAt ? new Date(t.createdAt).toLocaleDateString() : 'Recent'}</span>
                                        <button class="btn btn-primary btn-sm btn-accept-mission" data-id="${reqId}">
                                            Accept Mission
                                        </button>
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `;

                content.querySelectorAll('.btn-accept-mission').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const reqId = e.target.getAttribute('data-id');
                        try {
                            await ApiClient.post(`/assistance-requests/${reqId}/accept`);
                            showToast('You accepted this assistance mission!', 'success');
                            loadMyTasks();
                        } catch (err) {
                            showToast(err.message || 'Failed to accept task', 'error');
                        }
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const tabTasks = document.getElementById('tab-vol-tasks');
        const tabOpen = document.getElementById('tab-vol-open');

        tabTasks.addEventListener('click', () => {
            tabTasks.classList.add('active');
            tabOpen.classList.remove('active');
            loadMyTasks();
        });

        tabOpen.addEventListener('click', () => {
            tabOpen.classList.add('active');
            tabTasks.classList.remove('active');
            loadOpenQueue();
        });

        loadMyTasks();
    },

    // ========================================================
    // 2. NGO DASHBOARD
    // ========================================================
    async renderNgo(container) {
        if (!AuthService.hasRole('NGO', 'ADMIN')) {
            container.innerHTML = `<div class="empty-state"><h3>Access Denied</h3><p>NGO credentials required.</p></div>`;
            return;
        }

        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">NGO Relief Coordination Center</h1>
                    <p class="page-subtitle">Coordinate response efforts, dispatch verified volunteers, and manage resource allocations.</p>
                </div>
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-ngo-dash">Coordination Overview</button>
                <button class="tab-btn" id="tab-ngo-volunteers">Available Volunteers</button>
                <button class="tab-btn" id="tab-ngo-campaigns">Our Relief Campaigns</button>
            </div>

            <div id="ngo-content">
                <div class="loader"></div>
            </div>
        `;

        const loadOverview = async () => {
            const content = document.getElementById('ngo-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/coordination/dashboard');
                const data = (res && res.data) ? res.data : {};

                content.innerHTML = `
                    <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); margin-bottom: 1.5rem;">
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">ACTIVE REQUESTS</span>
                            <strong style="font-size:2rem; color:var(--primary);">${data.pendingRequestsCount ?? data.activeRequestsCount ?? 0}</strong>
                        </div>
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">AVAILABLE VOLUNTEERS</span>
                            <strong style="font-size:2rem; color:var(--secondary);">${data.availableVolunteersCount ?? 0}</strong>
                        </div>
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">COMPLETED MISSIONS</span>
                            <strong style="font-size:2rem; color:var(--success);">${data.completedRequestsCount ?? 0}</strong>
                        </div>
                    </div>

                    <h3 style="margin-bottom:1rem; font-size:1.1rem;">Pending Community Requests Awaiting Assignment</h3>
                    <div id="ngo-unassigned-list" class="grid">
                        <div class="loader"></div>
                    </div>
                `;

                // Fetch unassigned requests
                const reqRes = await ApiClient.get('/assistance-requests', { status: 'PENDING' });
                const requests = ApiClient.unbox(reqRes);
                const reqList = document.getElementById('ngo-unassigned-list');

                if (requests.length === 0) {
                    reqList.innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><p>No pending assistance requests waiting for assignment.</p></div>`;
                    return;
                }

                reqList.innerHTML = requests.map(r => {
                    const reqId = r.requestId || r.id;
                    return `
                        <div class="card">
                            <div class="card-header">
                                <span class="status-badge badge-${escapeHtml(r.priority || 'HIGH')}">${escapeHtml(r.priority || 'HIGH')}</span>
                                <small class="text-muted">${r.assistanceType || 'HELP'}</small>
                            </div>
                            <h4 class="card-title" style="font-size:1rem;">${escapeHtml(r.description)}</h4>
                            <div class="card-body">
                                <div style="font-size:0.85rem; color:var(--text-muted);">
                                    <div>📍 <strong>Location:</strong> ${escapeHtml(r.locationName || 'Unknown')}</div>
                                    <div>👥 <strong>People:</strong> ${r.peopleCount || 1}</div>
                                </div>
                            </div>
                            <div class="card-footer">
                                <button class="btn btn-primary btn-sm btn-assign-dialog" data-id="${reqId}">
                                    Assign Volunteer
                                </button>
                            </div>
                        </div>
                    `;
                }).join('');

                reqList.querySelectorAll('.btn-assign-dialog').forEach(btn => {
                    btn.addEventListener('click', (e) => {
                        const reqId = e.target.getAttribute('data-id');
                        this.showAssignVolunteerDialog(reqId, loadOverview);
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadVolunteers = async () => {
            const content = document.getElementById('ngo-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/coordination/volunteers');
                const list = ApiClient.unbox(res);

                if (list.length === 0) {
                    content.innerHTML = `<div class="empty-state"><p>No verified volunteers currently registered in coordination roster.</p></div>`;
                    return;
                }

                content.innerHTML = `
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Volunteer Name</th>
                                    <th>Phone</th>
                                    <th>Skills</th>
                                    <th>Availability</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${list.map(v => `
                                    <tr>
                                        <td><strong>${escapeHtml(v.name)}</strong></td>
                                        <td>${escapeHtml(v.phone || 'N/A')}</td>
                                        <td>${escapeHtml(v.skills || 'General relief')}</td>
                                        <td>${escapeHtml(v.availability || 'Active')}</td>
                                        <td><span class="status-badge badge-ACTIVE">VERIFIED</span></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `;
            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadMyCampaigns = async () => {
            const content = document.getElementById('ngo-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/campaigns/my');
                const list = ApiClient.unbox(res);

                if (list.length === 0) {
                    content.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">❤️</div>
                            <h3>No campaigns created yet</h3>
                            <p>Launch a relief campaign to gather humanitarian aid and resources.</p>
                            <button class="btn btn-primary" id="btn-ngo-new-camp" style="margin-top:1rem;">Launch Campaign</button>
                        </div>
                    `;
                    const btn = document.getElementById('btn-ngo-new-camp');
                    if (btn) btn.addEventListener('click', () => window.location.hash = '#campaigns');
                    return;
                }

                content.innerHTML = `
                    <div class="grid">
                        ${list.map(c => `
                            <div class="card">
                                <div class="card-header">
                                    <span class="status-badge badge-${escapeHtml(c.verificationStatus || 'PENDING')}">
                                        ${escapeHtml(c.verificationStatus || 'PENDING')}
                                    </span>
                                    <span class="status-badge badge-${escapeHtml(c.status || 'ACTIVE')}">${escapeHtml(c.status || 'ACTIVE')}</span>
                                </div>
                                <h3 class="card-title">${escapeHtml(c.title)}</h3>
                                <div class="card-body">
                                    <p>${escapeHtml(c.description)}</p>
                                    <div style="margin-top:0.75rem; font-size:0.85rem;">
                                        <div>Target: ₹${(c.targetAmount || 0).toLocaleString()}</div>
                                        <div>Raised: ₹${(c.raisedAmount || 0).toLocaleString()}</div>
                                    </div>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                `;
            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const tabDash = document.getElementById('tab-ngo-dash');
        const tabVols = document.getElementById('tab-ngo-volunteers');
        const tabCamps = document.getElementById('tab-ngo-campaigns');

        tabDash.addEventListener('click', () => {
            tabDash.classList.add('active');
            tabVols.classList.remove('active');
            tabCamps.classList.remove('active');
            loadOverview();
        });

        tabVols.addEventListener('click', () => {
            tabVols.classList.add('active');
            tabDash.classList.remove('active');
            tabCamps.classList.remove('active');
            loadVolunteers();
        });

        tabCamps.addEventListener('click', () => {
            tabCamps.classList.add('active');
            tabDash.classList.remove('active');
            tabVols.classList.remove('active');
            loadMyCampaigns();
        });

        loadOverview();
    },

    showAssignVolunteerDialog(requestId, onSuccess) {
        ApiClient.get('/coordination/volunteers').then(res => {
            const volunteers = ApiClient.unbox(res);
            if (volunteers.length === 0) {
                showToast('No verified volunteers available for assignment right now.', 'warning');
                return;
            }

            const modal = document.createElement('div');
            modal.className = 'modal-backdrop';
            modal.innerHTML = `
                <div class="modal-content">
                    <div class="modal-header">
                        <h3 class="modal-title">Assign Volunteer to Request #${requestId}</h3>
                        <button class="modal-close">&times;</button>
                    </div>
                    <form id="assign-volunteer-form">
                        <div class="modal-body">
                            <div class="form-group">
                                <label class="form-label">Select Volunteer *</label>
                                <select name="volunteerId" class="form-control" required>
                                    ${volunteers.map(v => `
                                        <option value="${v.volunteerId || v.id || v.userId}">
                                            ${escapeHtml(v.name)} — ${escapeHtml(v.skills || 'General')} (${escapeHtml(v.phone || 'No phone')})
                                        </option>
                                    `).join('')}
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Assignment Notes / Instructions</label>
                                <textarea name="instructions" rows="2" class="form-control" placeholder="Mission priority, rendezvous point, special instructions..."></textarea>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                            <button type="submit" class="btn btn-primary">Confirm Assignment</button>
                        </div>
                    </form>
                </div>
            `;

            document.body.appendChild(modal);

            const close = () => modal.remove();
            modal.querySelector('.modal-close').addEventListener('click', close);
            modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

            modal.querySelector('#assign-volunteer-form').addEventListener('submit', async (e) => {
                e.preventDefault();
                const fd = new FormData(e.target);
                const payload = {
                    volunteerId: parseInt(fd.get('volunteerId'), 10),
                    instructions: fd.get('instructions')
                };

                try {
                    await ApiClient.post(`/assistance-requests/${requestId}/assign`, payload);
                    showToast('Volunteer successfully assigned to request!', 'success');
                    close();
                    if (onSuccess) onSuccess();
                } catch (err) {
                    showToast(err.message || 'Failed to assign volunteer', 'error');
                }
            });
        }).catch(err => {
            showToast('Unable to fetch volunteer list: ' + err.message, 'error');
        });
    },

    // ========================================================
    // 3. AUTHORITY DASHBOARD
    // ========================================================
    async renderAuthority(container) {
        if (!AuthService.hasRole('AUTHORITY', 'ADMIN')) {
            container.innerHTML = `<div class="empty-state"><h3>Access Denied</h3><p>Civil Protection / Authority credentials required.</p></div>`;
            return;
        }

        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Disaster Authority Operations Console</h1>
                    <p class="page-subtitle">Disaster alert broadcasting, incident validation, shelter operations, and coordination.</p>
                </div>
                <button class="btn btn-primary" id="btn-auth-broadcast">
                    <span>📢</span> Broadcast Emergency Alert
                </button>
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-auth-incidents">Community Incidents</button>
                <button class="tab-btn" id="tab-auth-alerts">Broadcasted Alerts</button>
                <button class="tab-btn" id="tab-auth-shelters">Shelters Management</button>
            </div>

            <div id="auth-content">
                <div class="loader"></div>
            </div>
        `;

        const loadIncidents = async () => {
            const content = document.getElementById('auth-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/incidents');
                const incidents = ApiClient.unbox(res);

                if (incidents.length === 0) {
                    content.innerHTML = `<div class="empty-state"><p>No incident reports submitted by citizens.</p></div>`;
                    return;
                }

                content.innerHTML = `
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Incident</th>
                                    <th>Type</th>
                                    <th>Location</th>
                                    <th>Severity</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${incidents.map(inc => {
                                    const id = inc.incidentId || inc.id;
                                    return `
                                        <tr>
                                            <td>
                                                <strong>${escapeHtml(inc.title || inc.description)}</strong>
                                                <div style="font-size:0.75rem; color:var(--text-muted);">${inc.createdAt ? new Date(inc.createdAt).toLocaleString() : ''}</div>
                                            </td>
                                            <td>${escapeHtml(inc.incidentType || 'N/A')}</td>
                                            <td>${escapeHtml(inc.locationName || 'N/A')}</td>
                                            <td><span class="status-badge badge-${escapeHtml(inc.severity || 'INFO')}">${escapeHtml(inc.severity)}</span></td>
                                            <td><span class="status-badge badge-${escapeHtml(inc.status || 'REPORTED')}">${escapeHtml(inc.status)}</span></td>
                                            <td>
                                                <select class="select-input select-auth-inc-status" data-id="${id}" style="padding:0.25rem; font-size:0.8rem;">
                                                    <option value="">Update Status...</option>
                                                    <option value="VERIFIED">Verify Report</option>
                                                    <option value="INVESTIGATING">Investigating</option>
                                                    <option value="RESOLVED">Resolved</option>
                                                    <option value="REJECTED">Reject (False)</option>
                                                </select>
                                            </td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>
                `;

                content.querySelectorAll('.select-auth-inc-status').forEach(select => {
                    select.addEventListener('change', async (e) => {
                        const newStatus = e.target.value;
                        const incId = e.target.getAttribute('data-id');
                        if (!newStatus) return;

                        try {
                            await ApiClient.patch(`/incidents/${incId}/status`, { status: newStatus });
                            showToast('Incident status updated!', 'success');
                            loadIncidents();
                        } catch (err) {
                            showToast(err.message || 'Status update failed', 'error');
                        }
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadAlerts = async () => {
            const content = document.getElementById('auth-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/alerts');
                const alerts = ApiClient.unbox(res);

                content.innerHTML = `
                    <div class="grid">
                        ${alerts.map(a => {
                            const id = a.alertId || a.id;
                            return `
                                <div class="card">
                                    <div class="card-header">
                                        <span class="status-badge badge-${escapeHtml(a.severity || 'HIGH')}">${escapeHtml(a.severity)}</span>
                                        <span class="status-badge badge-${escapeHtml(a.status || 'ACTIVE')}">${escapeHtml(a.status)}</span>
                                    </div>
                                    <h3 class="card-title">${escapeHtml(a.title)}</h3>
                                    <div class="card-body">
                                        <p>${escapeHtml(a.description)}</p>
                                        <div style="font-size:0.85rem; color:var(--text-muted); margin-top:0.5rem;">
                                            <div>📍 Area: ${escapeHtml(a.affectedArea || 'Global')}</div>
                                            <div>🕒 Issued: ${a.issuedAt ? new Date(a.issuedAt).toLocaleString() : 'N/A'}</div>
                                        </div>
                                    </div>
                                    <div class="card-footer">
                                        ${a.status === 'ACTIVE' ? `
                                            <button class="btn btn-outline btn-sm btn-auth-resolve-alert" data-id="${id}">Mark Resolved</button>
                                        ` : `<span>Resolved</span>`}
                                    </div>
                                </div>
                            `;
                        }).join('')}
                    </div>
                `;

                content.querySelectorAll('.btn-auth-resolve-alert').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const alertId = e.target.getAttribute('data-id');
                        try {
                            await ApiClient.patch(`/alerts/${alertId}/status`, { status: 'RESOLVED' });
                            showToast('Alert resolved!', 'success');
                            loadAlerts();
                        } catch (err) {
                            showToast(err.message || 'Failed to update alert', 'error');
                        }
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const tabIncidents = document.getElementById('tab-auth-incidents');
        const tabAlerts = document.getElementById('tab-auth-alerts');
        const tabShelters = document.getElementById('tab-auth-shelters');

        tabIncidents.addEventListener('click', () => {
            tabIncidents.classList.add('active');
            tabAlerts.classList.remove('active');
            tabShelters.classList.remove('active');
            loadIncidents();
        });

        tabAlerts.addEventListener('click', () => {
            tabAlerts.classList.add('active');
            tabIncidents.classList.remove('active');
            tabShelters.classList.remove('active');
            loadAlerts();
        });

        tabShelters.addEventListener('click', () => {
            window.location.hash = '#shelters';
        });

        document.getElementById('btn-auth-broadcast').addEventListener('click', () => {
            window.location.hash = '#alerts';
        });

        loadIncidents();
    },

    // ========================================================
    // 4. ADMIN DASHBOARD & WORKFLOWS
    // ========================================================
    async renderAdmin(container) {
        if (!AuthService.hasRole('ADMIN')) {
            container.innerHTML = `<div class="empty-state"><h3>Access Denied</h3><p>Administrator credentials required.</p></div>`;
            return;
        }

        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">DisasterConnect Administration</h1>
                    <p class="page-subtitle">Accreditation queues, user moderation, system operational metrics, and governance audit trail.</p>
                </div>
            </div>

            <div class="tabs">
                <button class="tab-btn active" id="tab-adm-kpi">System Metrics</button>
                <button class="tab-btn" id="tab-adm-ngos">NGO Verification Queue</button>
                <button class="tab-btn" id="tab-adm-volunteers">Volunteer Queue</button>
                <button class="tab-btn" id="tab-adm-audit">Audit Logs</button>
            </div>

            <div id="admin-content">
                <div class="loader"></div>
            </div>
        `;

        const loadKpi = async () => {
            const content = document.getElementById('admin-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/admin/summary');
                const kpi = (res && res.data) ? res.data : {};

                content.innerHTML = `
                    <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); margin-bottom: 2rem;">
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">REGISTERED USERS</span>
                            <strong style="font-size:2.2rem; color:var(--dark);">${kpi.totalUsers ?? 0}</strong>
                        </div>
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">PENDING NGOS</span>
                            <strong style="font-size:2.2rem; color:var(--warning);">${kpi.pendingNgos ?? 0}</strong>
                        </div>
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">ACTIVE ALERTS</span>
                            <strong style="font-size:2.2rem; color:var(--primary);">${kpi.activeAlerts ?? 0}</strong>
                        </div>
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">TOTAL INCIDENTS</span>
                            <strong style="font-size:2.2rem; color:var(--secondary);">${kpi.totalIncidents ?? 0}</strong>
                        </div>
                        <div class="card" style="text-align:center;">
                            <span style="font-size:0.8rem; color:var(--text-muted);">ACTIVE CAMPAIGNS</span>
                            <strong style="font-size:2.2rem; color:var(--success);">${kpi.activeCampaigns ?? 0}</strong>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title">User Account Moderation</h3>
                        </div>
                        <div class="card-body">
                            <p style="font-size:0.9rem; margin-bottom:1rem; color:var(--text-muted);">
                                Suspend or unsuspend any user account with administrative justification. Suspended accounts lose active JWT access.
                            </p>
                            <form id="admin-suspend-form" style="display:flex; gap:0.75rem; flex-wrap:wrap; align-items:flex-end;">
                                <div class="form-group" style="margin-bottom:0; min-width:140px;">
                                    <label class="form-label">User ID *</label>
                                    <input type="number" name="userId" class="form-control" placeholder="e.g. 5" required />
                                </div>
                                <div class="form-group" style="margin-bottom:0; min-width:140px;">
                                    <label class="form-label">Action *</label>
                                    <select name="suspended" class="form-control" required>
                                        <option value="true">Suspend User</option>
                                        <option value="false">Unsuspend User</option>
                                    </select>
                                </div>
                                <div class="form-group" style="margin-bottom:0; flex:1; min-width:240px;">
                                    <label class="form-label">Administrative Reason *</label>
                                    <input type="text" name="reason" class="form-control" placeholder="Policy violation or review notes" required />
                                </div>
                                <button type="submit" class="btn btn-danger" style="height:38px;">Apply Action</button>
                            </form>
                        </div>
                    </div>
                `;

                content.querySelector('#admin-suspend-form').addEventListener('submit', async (e) => {
                    e.preventDefault();
                    const fd = new FormData(e.target);
                    const userId = fd.get('userId');
                    const suspended = fd.get('suspended') === 'true';
                    const reason = fd.get('reason');

                    try {
                        await ApiClient.patch(`/admin/users/${userId}/suspend`, { suspended, reason });
                        showToast(`User #${userId} account status updated successfully`, 'success');
                        e.target.reset();
                    } catch (err) {
                        showToast(err.message || 'Moderation action failed', 'error');
                    }
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadPendingNgos = async () => {
            const content = document.getElementById('admin-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/admin/ngos/pending');
                const ngos = ApiClient.unbox(res);

                if (ngos.length === 0) {
                    content.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">✅</div>
                            <h3>No pending NGO applications</h3>
                            <p>All NGO accreditation requests have been reviewed.</p>
                        </div>
                    `;
                    return;
                }

                content.innerHTML = `
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>NGO Organization</th>
                                    <th>Reg Number</th>
                                    <th>Contact</th>
                                    <th>Submitted</th>
                                    <th>Decision</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${ngos.map(n => {
                                    const id = n.ngoId || n.id;
                                    return `
                                        <tr>
                                            <td>
                                                <strong>${escapeHtml(n.orgName || n.name)}</strong>
                                                <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(n.email || '')}</div>
                                            </td>
                                            <td><code>${escapeHtml(n.registrationNumber || 'Pending')}</code></td>
                                            <td>${escapeHtml(n.phone || n.contactPhone || 'N/A')}</td>
                                            <td>${n.createdAt ? new Date(n.createdAt).toLocaleDateString() : 'Recent'}</td>
                                            <td>
                                                <div style="display:flex; gap:0.5rem;">
                                                    <button class="btn btn-success btn-sm btn-verify-ngo" data-id="${id}" data-status="VERIFIED">Approve</button>
                                                    <button class="btn btn-danger btn-sm btn-verify-ngo" data-id="${id}" data-status="REJECTED">Reject</button>
                                                </div>
                                            </td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>
                `;

                content.querySelectorAll('.btn-verify-ngo').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        const status = e.target.getAttribute('data-status');
                        try {
                            await ApiClient.patch(`/admin/ngos/${id}/verify?status=${status}`);
                            showToast(`NGO accreditation ${status.toLowerCase()}!`, 'success');
                            loadPendingNgos();
                        } catch (err) {
                            showToast(err.message || 'Action failed', 'error');
                        }
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadPendingVolunteers = async () => {
            const content = document.getElementById('admin-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/admin/volunteers/pending');
                const volunteers = ApiClient.unbox(res);

                if (volunteers.length === 0) {
                    content.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">✅</div>
                            <h3>No pending volunteer applicants</h3>
                            <p>All volunteer registrations are up to date.</p>
                        </div>
                    `;
                    return;
                }

                content.innerHTML = `
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Volunteer</th>
                                    <th>Contact</th>
                                    <th>Skills</th>
                                    <th>Availability</th>
                                    <th>Decision</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${volunteers.map(v => {
                                    const id = v.volunteerId || v.id;
                                    return `
                                        <tr>
                                            <td><strong>${escapeHtml(v.name)}</strong></td>
                                            <td>${escapeHtml(v.phone || 'N/A')}</td>
                                            <td>${escapeHtml(v.skills || 'General responder')}</td>
                                            <td>${escapeHtml(v.availability || 'Flexible')}</td>
                                            <td>
                                                <div style="display:flex; gap:0.5rem;">
                                                    <button class="btn btn-success btn-sm btn-verify-vol" data-id="${id}" data-status="VERIFIED">Approve</button>
                                                    <button class="btn btn-danger btn-sm btn-verify-vol" data-id="${id}" data-status="REJECTED">Reject</button>
                                                </div>
                                            </td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>
                `;

                content.querySelectorAll('.btn-verify-vol').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        const status = e.target.getAttribute('data-status');
                        try {
                            await ApiClient.patch(`/admin/volunteers/${id}/verify?status=${status}`);
                            showToast(`Volunteer ${status.toLowerCase()}!`, 'success');
                            loadPendingVolunteers();
                        } catch (err) {
                            showToast(err.message || 'Action failed', 'error');
                        }
                    });
                });

            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const loadAuditLogs = async () => {
            const content = document.getElementById('admin-content');
            content.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/admin/audit-logs');
                const logs = ApiClient.unbox(res);

                if (logs.length === 0) {
                    content.innerHTML = `<div class="empty-state"><p>No audit records found.</p></div>`;
                    return;
                }

                content.innerHTML = `
                    <div class="table-wrapper">
                        <table>
                            <thead>
                                <tr>
                                    <th>Timestamp</th>
                                    <th>Admin</th>
                                    <th>Action</th>
                                    <th>Target Resource</th>
                                    <th>Details / Notes</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${logs.map(l => `
                                    <tr>
                                        <td>${l.createdAt ? new Date(l.createdAt).toLocaleString() : 'N/A'}</td>
                                        <td><strong>${escapeHtml(l.adminEmail || l.adminName || 'Admin')}</strong></td>
                                        <td><code>${escapeHtml(l.action)}</code></td>
                                        <td>${escapeHtml(l.targetEntity || '')} #${l.targetId ?? ''}</td>
                                        <td>${escapeHtml(l.details || l.reason || '')}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `;
            } catch (err) {
                content.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        const tabKpi = document.getElementById('tab-adm-kpi');
        const tabNgos = document.getElementById('tab-adm-ngos');
        const tabVols = document.getElementById('tab-adm-volunteers');
        const tabAudit = document.getElementById('tab-adm-audit');

        tabKpi.addEventListener('click', () => {
            tabKpi.classList.add('active');
            tabNgos.classList.remove('active');
            tabVols.classList.remove('active');
            tabAudit.classList.remove('active');
            loadKpi();
        });

        tabNgos.addEventListener('click', () => {
            tabNgos.classList.add('active');
            tabKpi.classList.remove('active');
            tabVols.classList.remove('active');
            tabAudit.classList.remove('active');
            loadPendingNgos();
        });

        tabVols.addEventListener('click', () => {
            tabVols.classList.add('active');
            tabKpi.classList.remove('active');
            tabNgos.classList.remove('active');
            tabAudit.classList.remove('active');
            loadPendingVolunteers();
        });

        tabAudit.addEventListener('click', () => {
            tabAudit.classList.add('active');
            tabKpi.classList.remove('active');
            tabNgos.classList.remove('active');
            tabVols.classList.remove('active');
            loadAuditLogs();
        });

        loadKpi();
    }
};
