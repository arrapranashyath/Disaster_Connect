/**
 * DisasterConnect - Notifications Module
 * Handles in-app notification list, unread badge counter, and mark-as-read triggers.
 */

import { ApiClient, showToast, escapeHtml } from '../api.js';
import { AuthService } from '../auth.js';

export const NotificationsModule = {
    async updateBadge() {
        const badge = document.getElementById('nav-notif-badge');
        if (!badge) return;

        if (!AuthService.isAuthenticated()) {
            badge.style.display = 'none';
            return;
        }

        try {
            const res = await ApiClient.get('/notifications/unread-count');
            const data = (res && res.data) ? res.data : res;
            const count = (data && data.unreadCount !== undefined) ? data.unreadCount : (typeof data === 'number' ? data : 0);
            
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        } catch (err) {
            badge.style.display = 'none';
        }
    },

    async render(container) {
        if (!AuthService.isAuthenticated()) {
            container.innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🔔</div>
                    <h3>Sign In Required</h3>
                    <p>Please log in to view your system and emergency notifications.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="page-header">
                <div>
                    <h1 class="page-title">Notification Center</h1>
                    <p class="page-subtitle">Real-time alerts, verification outcomes, task assignments, and community status changes.</p>
                </div>
                <div>
                    <button class="btn btn-outline btn-sm" id="btn-mark-all-read">Mark All as Read</button>
                    <button class="btn btn-outline btn-sm" id="btn-refresh-notifs" style="margin-left:0.5rem;">Refresh</button>
                </div>
            </div>

            <div id="notifications-list" style="max-width: 800px; margin: 0 auto;">
                <div class="loader"></div>
            </div>
        `;

        const loadNotifications = async () => {
            const list = document.getElementById('notifications-list');
            list.innerHTML = `<div class="loader"></div>`;

            try {
                const res = await ApiClient.get('/notifications');
                const notifs = ApiClient.unbox(res);

                if (notifs.length === 0) {
                    list.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-state-icon">🔕</div>
                            <h3>No notifications right now</h3>
                            <p>You're all caught up!</p>
                        </div>
                    `;
                    this.updateBadge();
                    return;
                }

                list.innerHTML = notifs.map(n => {
                    const id = n.notificationId || n.id;
                    const isRead = n.isRead || n.read;
                    return `
                        <div class="card" style="margin-bottom: 0.75rem; border-left: 4px solid ${isRead ? 'var(--border)' : 'var(--primary)'}; background: ${isRead ? 'white' : 'var(--bg)'};">
                            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                                <div style="flex:1;">
                                    <div style="display:flex; align-items:center; gap:0.5rem; margin-bottom:0.25rem;">
                                        <strong style="font-size:0.95rem; color:var(--dark);">${escapeHtml(n.title || 'Notification')}</strong>
                                        <span class="status-badge badge-${escapeHtml(n.type || 'INFO')}">${escapeHtml(n.type || 'ALERT')}</span>
                                    </div>
                                    <p style="font-size:0.9rem; color:var(--text); margin-bottom:0.5rem;">${escapeHtml(n.message)}</p>
                                    <small style="color:var(--text-muted); font-size:0.8rem;">
                                        ${n.createdAt ? new Date(n.createdAt).toLocaleString() : 'Recent'}
                                    </small>
                                </div>
                                ${!isRead ? `
                                    <button class="btn btn-outline btn-sm btn-mark-read" data-id="${id}" style="font-size:0.75rem;">
                                        Mark Read
                                    </button>
                                ` : ''}
                            </div>
                        </div>
                    `;
                }).join('');

                list.querySelectorAll('.btn-mark-read').forEach(btn => {
                    btn.addEventListener('click', async (e) => {
                        const id = e.target.getAttribute('data-id');
                        try {
                            await ApiClient.patch(`/notifications/${id}/read`);
                            showToast('Marked as read', 'info');
                            loadNotifications();
                        } catch (err) {
                            showToast(err.message || 'Failed to update notification', 'error');
                        }
                    });
                });

                this.updateBadge();

            } catch (err) {
                list.innerHTML = `<div class="empty-state"><p>${escapeHtml(err.message)}</p></div>`;
            }
        };

        document.getElementById('btn-refresh-notifs').addEventListener('click', loadNotifications);

        document.getElementById('btn-mark-all-read').addEventListener('click', async () => {
            try {
                await ApiClient.patch('/notifications/read-all');
                showToast('All notifications marked as read', 'success');
                loadNotifications();
            } catch (err) {
                showToast(err.message || 'Failed to mark all as read', 'error');
            }
        });

        loadNotifications();
    }
};
