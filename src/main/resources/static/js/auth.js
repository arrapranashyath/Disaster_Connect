/**
 * DisasterConnect - Authentication Service
 * Manages Login, Register, Session, and Role-based permissions.
 */

import { ApiClient, showToast } from './api.js';

export const AuthService = {
    isAuthenticated() {
        return !!ApiClient.getToken();
    },

    getUser() {
        return ApiClient.getUser();
    },

    getRole() {
        const u = this.getUser();
        if (!u) return null;
        let role = u.role || (u.roles && u.roles[0]);
        if (typeof role === 'string' && role.startsWith('ROLE_')) {
            role = role.substring(5);
        }
        return role || null;
    },

    hasRole(...roles) {
        const currentRole = this.getRole();
        if (!currentRole) return false;
        return roles.includes(currentRole);
    },

    async login(email, password, remember = true) {
        try {
            const res = await ApiClient.post('/auth/login', { email, password });
            if (res && res.success && res.data) {
                const data = res.data;
                const token = data.token;
                
                // Backend AuthResponse has { token, tokenType, expiresIn, user: UserResponse }
                // Fallback to top-level fields if flattened
                const userObj = data.user || data;
                let rawRole = userObj.role || data.role || 'CITIZEN';
                if (typeof rawRole === 'string' && rawRole.startsWith('ROLE_')) {
                    rawRole = rawRole.substring(5);
                }

                const user = {
                    userId: userObj.userId || data.userId,
                    name: userObj.name || data.name || userObj.email || data.email,
                    email: userObj.email || data.email || email,
                    role: rawRole,
                    phone: userObj.phone,
                    location: userObj.location,
                    verificationStatus: userObj.verificationStatus
                };
                
                ApiClient.setToken(token, remember);
                ApiClient.setUser(user, remember);
                
                showToast(`Welcome back, ${user.name}!`, 'success');
                window.dispatchEvent(new CustomEvent('auth-changed', { detail: user }));
                return user;
            } else {
                throw new Error(res.message || 'Login failed');
            }
        } catch (err) {
            showToast(err.message || 'Login failed. Please check your credentials.', 'error');
            throw err;
        }
    },

    async register(formData) {
        try {
            const res = await ApiClient.post('/auth/register', formData);
            if (res && res.success) {
                showToast('Registration successful! You may now sign in.', 'success');
                return res.data;
            } else {
                throw new Error(res.message || 'Registration failed');
            }
        } catch (err) {
            showToast(err.message || 'Registration failed.', 'error');
            throw err;
        }
    },

    logout() {
        ApiClient.clearAuth();
        showToast('You have been logged out.', 'info');
        window.dispatchEvent(new CustomEvent('auth-changed', { detail: null }));
        window.location.hash = '#login';
    }
};
