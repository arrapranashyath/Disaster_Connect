/**
 * DisasterConnect - Missing Persons Module
 * Allows family members to report missing persons, search missing registries, and update tracking status.
 */

import { ApiClient, showToast, escapeHtml } from '../api.js';
import { AuthService } from '../auth.js';

export const MissingPersonsModule = {
    async render(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Missing Persons & Family Safety</h1>
                    <p class="page-subtitle">Report missing loved ones, search registry by name and last seen location, and notify search teams.</p>
                </div>
                ${AuthService.isAuthenticated() ? `
                    <button class="btn btn-primary" id="btn-report-missing">
                        <span>🔍</span> Report Missing Person
                    </button>
                ` : `
                    <button class="btn btn-outline" id="btn-login-missing">
                        Sign in to Report
                    </button>
                `}
            </div>

            <div class="filter-bar">
                <input type="text" id="filter-missing-query" class="search-input" placeholder="Search by name, features or last seen location..." />
                <select id="filter-missing-status" class="select-input">
                    <option value="">All Statuses</option>
                    <option value="MISSING" selected>Missing Only</option>
                    <option value="FOUND">Found / Reunited</option>
                </select>
                <button class="btn btn-outline btn-sm" id="btn-search-missing">Search</button>
            </div>

            <div id="missing-persons-grid" class="grid">
                <div class="loader"></div>
            </div>
        `;

        const loadMissingPersons = async () => {
            const grid = document.getElementById('missing-persons-grid');
            grid.innerHTML = `<div class="loader"></div>`;

            const query = document.getElementById('filter-missing-query').value.trim();
            const status = document.getElementById('filter-missing-status').value;

            try {
                const params = {};
                if (query) params.query = query;
                if (status) params.status = status;

                const res = await ApiClient.get('/missing-persons', params);
                const list = ApiClient.unbox(res);

                if (list.length === 0) {
                    grid.innerHTML = `
                        <div class="empty-state" style="grid-column:1/-1;">
                            <div class="empty-state-icon">🕊️</div>
                            <h3>No missing persons match your query</h3>
                            <p>Verify spelling or clear filters to view recent community listings.</p>
                        </div>
                    `;
                    return;
                }

                grid.innerHTML = list.map(p => {
                    const id = p.personId || p.id;
                    return `
                        <div class="card">
                            <div class="card-header">
                                <span class="status-badge ${p.status === 'FOUND' ? 'badge-RESOLVED' : 'badge-CRITICAL'}">
                                    ${escapeHtml(p.status || 'MISSING')}
                                </span>
                                <small class="text-muted">Age: ${p.age ?? 'Unknown'}</small>
                            </div>
                            <h3 class="card-title">${escapeHtml(p.name)}</h3>
                            <div class="card-body">
                                <div style="font-size:0.875rem; color:var(--dark-muted); margin-bottom:0.75rem;">
                                    <div>📍 <strong>Last Seen:</strong> ${escapeHtml(p.lastSeenLocation || 'Not provided')}</div>
                                    ${p.gender ? `<div>⚧ <strong>Gender:</strong> ${escapeHtml(p.gender)}</div>` : ''}
                                </div>
                                <p style="font-size:0.875rem; background:var(--bg); padding:0.5rem; border-radius:var(--radius); margin-bottom:0.5rem;">
                                    <strong>Description:</strong> ${escapeHtml(p.description || 'No distinct features noted')}
                                </p>
                                ${p.contactInfo ? `
                                    <div style="font-size:0.85rem; color:var(--secondary);">
                                        📞 <strong>Emergency Contact:</strong> ${escapeHtml(p.contactInfo)}
                                    </div>
                                ` : `
                                    <div style="font-size:0.8rem; color:var(--text-muted); font-style:italic;">
                                        🔒 Contact info hidden for public privacy. Sign in to view.
                                    </div>
                                `}
                            </div>
                            <div class="card-footer">
                                <span>Reported: ${p.createdAt ? new Date(p.createdAt).toLocaleDateString() : 'Recent'}</span>
                                ${p.status !== 'FOUND' && AuthService.isAuthenticated() ? `
                                    <button class="btn btn-outline btn-sm btn-mark-found" data-id="${id}">Mark Found</button>
                                ` : ''}
                            </div>
                        </div>
                    `;
                }).join('');

                grid.querySelectorAll('.btn-mark-found').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        if (confirm('Mark this person as found / reunited?')) {
                            try {
                                await ApiClient.patch(`/missing-persons/${id}/status`, { status: 'FOUND' });
                                showToast('Status updated to FOUND', 'success');
                                loadMissingPersons();
                            } catch (err) {
                                showToast(err.message || 'Failed to update status', 'error');
                            }
                        }
                    });
                });

            } catch (err) {
                grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        document.getElementById('btn-search-missing').addEventListener('click', loadMissingPersons);
        document.getElementById('filter-missing-status').addEventListener('change', loadMissingPersons);
        document.getElementById('filter-missing-query').addEventListener('input', () => {
            clearTimeout(window._missTimeout);
            window._missTimeout = setTimeout(loadMissingPersons, 350);
        });

        if (document.getElementById('btn-report-missing')) {
            document.getElementById('btn-report-missing').addEventListener('click', () => {
                this.showReportDialog(loadMissingPersons);
            });
        }

        if (document.getElementById('btn-login-missing')) {
            document.getElementById('btn-login-missing').addEventListener('click', () => {
                window.location.hash = '#login';
            });
        }

        loadMissingPersons();
    },

    showReportDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Report Missing Person</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="missing-person-form">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label">Full Name *</label>
                            <input type="text" name="name" class="form-control" required placeholder="Person's complete name" />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Age *</label>
                                <input type="number" name="age" class="form-control" min="0" max="150" required placeholder="e.g. 34" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Gender *</label>
                                <select name="gender" class="form-control" required>
                                    <option value="Male">Male</option>
                                    <option value="Female">Female</option>
                                    <option value="Other">Other</option>
                                    <option value="Unknown">Prefer not to say</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Last Seen Location *</label>
                            <input type="text" name="lastSeenLocation" class="form-control" required placeholder="Bridge road, bus stand, or building" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Physical Description & Features *</label>
                            <textarea name="description" rows="2" class="form-control" required placeholder="Clothing worn, height, identifying scars, glasses, or medical details..."></textarea>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Contact Phone / Details *</label>
                            <input type="text" name="contactInfo" class="form-control" required placeholder="Phone number or address to reach reporter" />
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">File Report</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#missing-person-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const payload = {
                name: fd.get('name'),
                age: parseInt(fd.get('age'), 10),
                gender: fd.get('gender'),
                lastSeenLocation: fd.get('lastSeenLocation'),
                description: fd.get('description'),
                contactInfo: fd.get('contactInfo')
            };

            try {
                await ApiClient.post('/missing-persons', payload);
                showToast('Missing person report filed successfully!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to file report', 'error');
            }
        });
    }
};
