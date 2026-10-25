/**
 * DisasterConnect - Modern API Client
 * Wraps fetch calls, handles JWT auth headers, ApiResponse unboxing, and error notifications.
 */

const API_BASE = '/api';

export const ApiClient = {
    getToken() {
        return localStorage.getItem('dc_token') || sessionStorage.getItem('dc_token');
    },

    setToken(token, remember = true) {
        if (remember) {
            localStorage.setItem('dc_token', token);
        } else {
            sessionStorage.setItem('dc_token', token);
        }
    },

    getUser() {
        const u = localStorage.getItem('dc_user') || sessionStorage.getItem('dc_user');
        return u ? JSON.parse(u) : null;
    },

    setUser(user, remember = true) {
        const str = JSON.stringify(user);
        if (remember) {
            localStorage.setItem('dc_user', str);
        } else {
            sessionStorage.setItem('dc_user', str);
        }
    },

    clearAuth() {
        localStorage.removeItem('dc_token');
        localStorage.removeItem('dc_user');
        sessionStorage.removeItem('dc_token');
        sessionStorage.removeItem('dc_user');
    },

    async request(endpoint = '', options = {}) {
        const ep = typeof endpoint === 'string' ? endpoint : String(endpoint || '');
        const url = `${API_BASE}${ep.startsWith('/') ? ep : '/' + ep}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);
            let responseData;
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                responseData = await response.json();
            } else {
                responseData = await response.text();
            }

            if (!response.ok) {
                if (response.status === 401) {
                    this.clearAuth();
                    window.dispatchEvent(new CustomEvent('auth-changed'));
                }
                
                let errorMessage = `HTTP Error ${response.status}`;
                if (responseData && typeof responseData === 'object') {
                    if (responseData.data && typeof responseData.data === 'object' && !Array.isArray(responseData.data)) {
                        // Field validation errors: { field: "message", ... }
                        const fieldErrors = Object.entries(responseData.data)
                            .map(([field, msg]) => `${field}: ${msg}`)
                            .join('; ');
                        errorMessage = fieldErrors ? `${responseData.message || 'Validation failed'}: ${fieldErrors}` : (responseData.message || errorMessage);
                    } else if (responseData.message) {
                        errorMessage = responseData.message;
                    }
                } else if (typeof responseData === 'string' && responseData.length > 0) {
                    errorMessage = responseData;
                }
                
                throw new Error(errorMessage);
            }

            return responseData;
        } catch (err) {
            console.error(`[API ERROR] ${options.method || 'GET'} ${ep}:`, err);
            throw err;
        }
    },

    get(endpoint, params = {}) {
        const queryString = new URLSearchParams(
            Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
        ).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method: 'GET' });
    },

    post(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    put(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    patch(endpoint, data = {}) {
        return this.request(endpoint, {
            method: 'PATCH',
            body: JSON.stringify(data)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    },

    unbox(res) {
        if (!res) return [];
        const data = res.data !== undefined ? res.data : res;
        if (!data) return [];
        if (Array.isArray(data)) return data;
        if (data.content && Array.isArray(data.content)) return data.content;
        return [data];
    }
};

/** Toast UI Helper */
export function showToast(message, type = 'info', duration = 4000) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type} animate-slide-in`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error' || type === 'danger') icon = '⚠️';
    if (type === 'warning') icon = '🔔';

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-msg">${escapeHtml(message)}</span>
        <button class="toast-close" aria-label="Close">&times;</button>
    `;

    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.remove();
    });

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => toast.remove(), 400);
    }, duration);
}

export function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
