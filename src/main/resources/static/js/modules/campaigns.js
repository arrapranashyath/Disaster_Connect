/**
 * DisasterConnect - Relief Campaigns Module
 * Shows verified relief campaigns created by accredited NGOs with donation and bank instructions.
 */

import { ApiClient, showToast, escapeHtml } from '../api.js';
import { AuthService } from '../auth.js';

export const CampaignsModule = {
    async render(container) {
        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Relief Campaigns</h1>
                    <p class="page-subtitle">Support verified humanitarian campaigns organized by accredited NGOs to deliver aid directly to affected zones.</p>
                </div>
                ${AuthService.hasRole('NGO', 'ADMIN') ? `
                    <button class="btn btn-primary" id="btn-create-campaign">
                        <span>❤️</span> Launch Relief Campaign
                    </button>
                ` : ''}
            </div>

            <div id="campaigns-grid" class="grid">
                <div class="loader"></div>
            </div>
        `;

        const loadCampaigns = async () => {
            const grid = document.getElementById('campaigns-grid');
            grid.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/campaigns');
                const campaigns = ApiClient.unbox(res);

                if (campaigns.length === 0) {
                    grid.innerHTML = `
                        <div class="empty-state" style="grid-column:1/-1;">
                            <div class="empty-state-icon">🤝</div>
                            <h3>No active relief campaigns right now</h3>
                            <p>Accredited NGOs launch campaigns during disaster events to mobilize funds and supplies.</p>
                        </div>
                    `;
                    return;
                }

                grid.innerHTML = campaigns.map(c => {
                    const target = c.targetAmount || 0;
                    const raised = c.raisedAmount || 0;
                    const pct = target > 0 ? Math.min(100, Math.round((raised / target) * 100)) : 0;
                    const id = c.campaignId || c.id;

                    return `
                        <div class="card">
                            <div class="card-header">
                                <span class="status-badge badge-${escapeHtml(c.status || 'ACTIVE')}">${escapeHtml(c.status || 'ACTIVE')}</span>
                                <small class="text-muted">By: ${escapeHtml(c.ngoName || 'Verified NGO')}</small>
                            </div>
                            <h3 class="card-title">${escapeHtml(c.title)}</h3>
                            <div class="card-body">
                                <p>${escapeHtml(c.description)}</p>
                                <div style="margin-top:auto; padding-top:0.75rem;">
                                    <div style="display:flex; justify-content:space-between; font-size:0.85rem; margin-bottom:0.25rem;">
                                        <span>Target: ₹${target.toLocaleString()}</span>
                                        <strong>₹${raised.toLocaleString()} (${pct}%)</strong>
                                    </div>
                                    <div style="background:var(--border); border-radius:999px; height:8px; overflow:hidden;">
                                        <div style="background:var(--secondary); width:${pct}%; height:100%;"></div>
                                    </div>
                                </div>
                            </div>
                            <div class="card-footer">
                                <span>End Date: ${c.endDate ? new Date(c.endDate).toLocaleDateString() : 'Ongoing'}</span>
                                <button class="btn btn-secondary btn-sm btn-donate-campaign" data-id="${id}" data-title="${escapeHtml(c.title)}">
                                    Contribute Aid
                                </button>
                            </div>
                        </div>
                    `;
                }).join('');

                grid.querySelectorAll('.btn-donate-campaign').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        try {
                            const detailsRes = await ApiClient.get(`/campaigns/${id}`);
                            const camp = detailsRes.data || detailsRes;
                            this.showDonationInstructionsDialog(camp);
                        } catch (err) {
                            showToast(err.message || 'Could not fetch campaign details', 'error');
                        }
                    });
                });

            } catch (err) {
                grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        if (document.getElementById('btn-create-campaign')) {
            document.getElementById('btn-create-campaign').addEventListener('click', () => {
                this.showCreateCampaignDialog(loadCampaigns);
            });
        }

        loadCampaigns();
    },

    showDonationInstructionsDialog(campaign) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Donation & Relief Instructions</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <div class="modal-body">
                    <h4 style="color:var(--dark); margin-bottom:0.5rem;">${escapeHtml(campaign.title)}</h4>
                    <p style="font-size:0.9rem; color:var(--text-muted); margin-bottom:1rem;">${escapeHtml(campaign.description)}</p>
                    
                    <div style="background:var(--bg); border:1px solid var(--border); border-radius:var(--radius); padding:1rem; margin-bottom:1rem;">
                        <h5 style="color:var(--dark); margin-bottom:0.5rem;">💳 Verified Donation Instructions</h5>
                        <p style="font-size:0.9rem; white-space:pre-wrap; line-height:1.5;">${escapeHtml(campaign.donationInstructions || 'Please contact the managing NGO directly for official bank transfer and relief supply delivery instructions.')}</p>
                    </div>

                    <div style="font-size:0.85rem; color:var(--text-muted);">
                        <div>Managing Organization: <strong>${escapeHtml(campaign.ngoName || 'Accredited NGO')}</strong></div>
                        <div>Target Goal: <strong>₹${(campaign.targetAmount || 0).toLocaleString()}</strong></div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary btn-close-modal">Understood</button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-close-modal').addEventListener('click', close);
    },

    showCreateCampaignDialog(onSuccess) {
        const modal = document.createElement('div');
        modal.className = 'modal-backdrop';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h3 class="modal-title">Launch Relief Campaign</h3>
                    <button class="modal-close">&times;</button>
                </div>
                <form id="create-campaign-form">
                    <div class="modal-body">
                        <div class="form-group">
                            <label class="form-label">Campaign Title *</label>
                            <input type="text" name="title" class="form-control" required placeholder="e.g. Coastal Flood Relief Fund" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Target Funding (₹) *</label>
                            <input type="number" step="any" name="targetAmount" class="form-control" required min="100" value="50000" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Donation Instructions (Bank A/C, UPI, Depot Address) *</label>
                            <textarea name="donationInstructions" rows="3" class="form-control" required placeholder="Provide official UPI ID, Bank IFSC, account name, and collection center addresses..."></textarea>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Campaign Details & Objectives *</label>
                            <textarea name="description" rows="3" class="form-control" required placeholder="Explain how funds will be deployed and who is being aided..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline btn-cancel-modal">Cancel</button>
                        <button type="submit" class="btn btn-primary">Launch Campaign</button>
                    </div>
                </form>
            </div>
        `;

        document.body.appendChild(modal);

        const close = () => modal.remove();
        modal.querySelector('.modal-close').addEventListener('click', close);
        modal.querySelector('.btn-cancel-modal').addEventListener('click', close);

        modal.querySelector('#create-campaign-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const fd = new FormData(e.target);
            const payload = {
                title: fd.get('title'),
                targetAmount: parseFloat(fd.get('targetAmount')),
                donationInstructions: fd.get('donationInstructions'),
                description: fd.get('description')
            };

            try {
                await ApiClient.post('/campaigns', payload);
                showToast('Relief campaign created successfully!', 'success');
                close();
                if (onSuccess) onSuccess();
            } catch (err) {
                showToast(err.message || 'Failed to create campaign', 'error');
            }
        });
    }
};
