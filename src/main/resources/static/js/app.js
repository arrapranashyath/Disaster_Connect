/**
 * DisasterConnect - Main Application Router & Entry Point
 */

import { ApiClient, showToast } from './api.js';
import { AuthService } from './auth.js';
import { CitizenModule } from './modules/citizen.js';
import { SheltersModule } from './modules/shelters.js';
import { MissingPersonsModule } from './modules/missingPersons.js';
import { CampaignsModule } from './modules/campaigns.js';
import { NotificationsModule } from './modules/notifications.js';
import { RoleDashboardsModule } from './modules/roleDashboards.js';

class App {
    constructor() {
        this.currentRoute = '';
        this.init();
    }

    init() {
        // Listen to hash changes (browser back, forward, link clicks)
        window.addEventListener('hashchange', () => this.handleRoute());
        
        // Listen to auth state changes
        window.addEventListener('auth-changed', () => {
            this.renderNavbar();
            NotificationsModule.updateBadge();
            this.handleRoute();
        });

        // Setup notification bell
        const bell = document.getElementById('nav-notif-bell');
        if (bell) {
            bell.addEventListener('click', () => {
                window.location.hash = '#notifications';
            });
        }

        // Initial setup
        this.renderNavbar();
        NotificationsModule.updateBadge();
        
        // Start routing
        if (!window.location.hash) {
            window.location.hash = '#alerts';
        } else {
            this.handleRoute();
        }

        // Periodically refresh notification badge if logged in
        setInterval(() => {
            NotificationsModule.updateBadge();
        }, 30000);
    }

    getRoleDefaultRoute(role) {
        switch (role) {
            case 'VOLUNTEER': return '#volunteer';
            case 'NGO': return '#ngo';
            case 'AUTHORITY': return '#authority';
            case 'ADMIN': return '#admin';
            case 'CITIZEN':
            default: return '#alerts';
        }
    }

    renderNavbar() {
        const user = AuthService.getUser();
        const role = AuthService.getRole();
        const navLinks = document.getElementById('nav-links');
        const navAuth = document.getElementById('nav-auth');

        // Dynamic navigation links based on role
        let linksHtml = `
            <li><a href="#alerts" class="nav-link" data-route="alerts">📢 Alerts</a></li>
            <li><a href="#incidents" class="nav-link" data-route="incidents">📋 Incidents</a></li>
            <li><a href="#assistance" class="nav-link" data-route="assistance">🆘 Assistance</a></li>
            <li><a href="#shelters" class="nav-link" data-route="shelters">🏕️ Shelters</a></li>
            <li><a href="#resources" class="nav-link" data-route="resources">📦 Resources</a></li>
            <li><a href="#missing" class="nav-link" data-route="missing">🔍 Missing Persons</a></li>
            <li><a href="#campaigns" class="nav-link" data-route="campaigns">❤️ Campaigns</a></li>
        `;

        if (role === 'VOLUNTEER') {
            linksHtml += `<li><a href="#volunteer" class="nav-link" data-route="volunteer">🤝 Volunteer Console</a></li>`;
        } else if (role === 'NGO') {
            linksHtml += `<li><a href="#ngo" class="nav-link" data-route="ngo">🏢 NGO Center</a></li>`;
        } else if (role === 'AUTHORITY') {
            linksHtml += `<li><a href="#authority" class="nav-link" data-route="authority">⚖️ Authority Ops</a></li>`;
        } else if (role === 'ADMIN') {
            linksHtml += `
                <li><a href="#volunteer" class="nav-link" data-route="volunteer">Volunteer</a></li>
                <li><a href="#ngo" class="nav-link" data-route="ngo">NGO</a></li>
                <li><a href="#authority" class="nav-link" data-route="authority">Authority</a></li>
                <li><a href="#admin" class="nav-link" data-route="admin">⚙️ Admin Console</a></li>
            `;
        }

        navLinks.innerHTML = linksHtml;

        // Auth section
        if (user) {
            navAuth.innerHTML = `
                <div class="user-pill">
                    <span>👤</span>
                    <span>${user.name || user.email}</span>
                    <span class="role-badge role-${user.role}">${user.role}</span>
                </div>
                <button class="btn btn-outline btn-sm" id="btn-logout">Sign Out</button>
            `;
            document.getElementById('btn-logout').addEventListener('click', () => {
                AuthService.logout();
            });
        } else {
            navAuth.innerHTML = `
                <a href="#login" class="btn btn-outline btn-sm" data-route="login">Sign In</a>
                <a href="#register" class="btn btn-primary btn-sm" data-route="register">Register</a>
            `;
        }

        this.updateActiveNavLink();
    }

    updateActiveNavLink() {
        const hash = (window.location.hash || '#alerts').substring(1);
        document.querySelectorAll('.nav-link').forEach(link => {
            if (link.getAttribute('data-route') === hash) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    async handleRoute() {
        const hash = (window.location.hash || '#alerts').substring(1);
        this.currentRoute = hash;
        this.updateActiveNavLink();

        const container = document.getElementById('app-content');
        if (!container) return;

        // Navigation access guards
        const isAuth = AuthService.isAuthenticated();
        const role = AuthService.getRole();

        // Protected routes requiring authentication
        if (hash === 'notifications') {
            if (!isAuth) {
                showToast('Please sign in to view your notifications.', 'warning');
                window.location.hash = '#login';
                return;
            }
        }

        if (hash === 'volunteer') {
            if (!isAuth) {
                showToast('Please sign in to access the Volunteer console.', 'warning');
                window.location.hash = '#login';
                return;
            }
            if (!AuthService.hasRole('VOLUNTEER', 'ADMIN')) {
                showToast('Access restricted to Volunteers and Administrators.', 'error');
                window.location.hash = this.getRoleDefaultRoute(role);
                return;
            }
        }

        if (hash === 'ngo') {
            if (!isAuth) {
                showToast('Please sign in to access the NGO Relief center.', 'warning');
                window.location.hash = '#login';
                return;
            }
            if (!AuthService.hasRole('NGO', 'ADMIN')) {
                showToast('Access restricted to NGO organizations and Administrators.', 'error');
                window.location.hash = this.getRoleDefaultRoute(role);
                return;
            }
        }

        if (hash === 'authority') {
            if (!isAuth) {
                showToast('Please sign in to access Authority operations.', 'warning');
                window.location.hash = '#login';
                return;
            }
            if (!AuthService.hasRole('AUTHORITY', 'ADMIN')) {
                showToast('Access restricted to Disaster Authorities and Administrators.', 'error');
                window.location.hash = this.getRoleDefaultRoute(role);
                return;
            }
        }

        if (hash === 'admin') {
            if (!isAuth) {
                showToast('Please sign in to access the Admin Console.', 'warning');
                window.location.hash = '#login';
                return;
            }
            if (!AuthService.hasRole('ADMIN')) {
                showToast('Access restricted to System Administrators.', 'error');
                window.location.hash = this.getRoleDefaultRoute(role);
                return;
            }
        }

        switch (hash) {
            case 'alerts':
                await CitizenModule.renderAlerts(container);
                break;
            case 'incidents':
                await CitizenModule.renderIncidents(container);
                break;
            case 'assistance':
                await CitizenModule.renderAssistance(container);
                break;
            case 'shelters':
                await SheltersModule.renderShelters(container);
                break;
            case 'resources':
                await SheltersModule.renderResources(container);
                break;
            case 'missing':
                await MissingPersonsModule.render(container);
                break;
            case 'campaigns':
                await CampaignsModule.render(container);
                break;
            case 'notifications':
                await NotificationsModule.render(container);
                break;
            case 'volunteer':
                await RoleDashboardsModule.renderVolunteer(container);
                break;
            case 'ngo':
                await RoleDashboardsModule.renderNgo(container);
                break;
            case 'authority':
                await RoleDashboardsModule.renderAuthority(container);
                break;
            case 'admin':
                await RoleDashboardsModule.renderAdmin(container);
                break;
            case 'login':
                this.renderLogin(container);
                break;
            case 'register':
                this.renderRegister(container);
                break;
            default:
                await CitizenModule.renderAlerts(container);
                break;
        }
    }

    renderLogin(container) {
        if (AuthService.isAuthenticated()) {
            const role = AuthService.getRole();
            window.location.hash = this.getRoleDefaultRoute(role);
            return;
        }

        container.innerHTML = `
            <div style="max-width: 440px; margin: 2rem auto;">
                <div class="card" style="padding: 2rem;">
                    <div style="text-align: center; margin-bottom: 1.5rem;">
                        <span style="font-size: 2.5rem;">🛡️</span>
                        <h2 style="font-size: 1.5rem; font-weight: 800; margin-top: 0.5rem; color: var(--dark);">Sign in to DisasterConnect</h2>
                        <p style="font-size: 0.9rem; color: var(--text-muted);">Access emergency dispatches and relief coordination</p>
                    </div>

                    <form id="app-login-form">
                        <div class="form-group">
                            <label class="form-label">Email Address *</label>
                            <input type="email" name="email" class="form-control" required placeholder="user@example.com" autocomplete="email" />
                        </div>
                        <div class="form-group">
                            <label class="form-label">Password *</label>
                            <input type="password" name="password" class="form-control" required placeholder="••••••••" autocomplete="current-password" />
                        </div>
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
                            <label style="font-size: 0.85rem; display: flex; align-items: center; gap: 0.4rem; cursor: pointer;">
                                <input type="checkbox" name="remember" checked /> Remember me
                            </label>
                        </div>
                        <button type="submit" id="btn-login-submit" class="btn btn-primary" style="width: 100%; justify-content: center; padding: 0.75rem;">
                            <span>Sign In</span>
                        </button>
                    </form>

                    <div style="text-align: center; margin-top: 1.5rem; font-size: 0.875rem; color: var(--text-muted);">
                        Don't have an account? <a href="#register" style="color: var(--secondary); font-weight: 600; text-decoration: none;">Create one here</a>
                    </div>
                </div>
            </div>
        `;

        const form = document.getElementById('app-login-form');
        const submitBtn = document.getElementById('btn-login-submit');

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (submitBtn.disabled) return;

            const fd = new FormData(form);
            const email = (fd.get('email') || '').trim();
            const password = fd.get('password') || '';
            const remember = fd.get('remember') !== null;

            if (!email || !password) {
                showToast('Please enter both email and password', 'warning');
                return;
            }

            // Visible loading state and debounce
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="btn-spinner"></span> <span>Signing In...</span>`;

            try {
                const user = await AuthService.login(email, password, remember);
                // Redirect user based on their actual role
                const targetRoute = this.getRoleDefaultRoute(user.role);
                window.location.hash = targetRoute;
            } catch (err) {
                // Restore button state
                submitBtn.disabled = false;
                submitBtn.innerHTML = `<span>Sign In</span>`;
            }
        });
    }

    renderRegister(container) {
        if (AuthService.isAuthenticated()) {
            const role = AuthService.getRole();
            window.location.hash = this.getRoleDefaultRoute(role);
            return;
        }

        container.innerHTML = `
            <div style="max-width: 540px; margin: 2rem auto;">
                <div class="card" style="padding: 2rem;">
                    <div style="text-align: center; margin-bottom: 1.5rem;">
                        <span style="font-size: 2.5rem;">📝</span>
                        <h2 style="font-size: 1.5rem; font-weight: 800; margin-top: 0.5rem; color: var(--dark);">Create Your Account</h2>
                        <p style="font-size: 0.9rem; color: var(--text-muted);">Join our disaster response and humanitarian network</p>
                    </div>

                    <form id="app-register-form">
                        <div class="form-group">
                            <label class="form-label">Full Name *</label>
                            <input type="text" name="name" class="form-control" required placeholder="John Doe" />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Email Address *</label>
                                <input type="email" name="email" class="form-control" required placeholder="john@example.com" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Phone Number *</label>
                                <input type="text" name="phone" class="form-control" required placeholder="+91 98765 43210" />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Password * (Min 6 chars)</label>
                                <input type="password" name="password" minlength="6" class="form-control" required placeholder="••••••••" />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Account Role *</label>
                                <select name="role" id="reg-role-select" class="form-control" required>
                                    <option value="CITIZEN">Citizen</option>
                                    <option value="VOLUNTEER">Volunteer</option>
                                    <option value="NGO">NGO Organization</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Primary Location / City</label>
                            <input type="text" name="location" class="form-control" placeholder="Chennai, India" />
                        </div>

                        <!-- Conditional NGO fields -->
                        <div id="ngo-fields" style="display: none; background: var(--bg); padding: 1rem; border-radius: var(--radius); margin-bottom: 1rem;">
                            <div class="form-group">
                                <label class="form-label">Organization Name *</label>
                                <input type="text" name="orgName" id="input-orgName" class="form-control" placeholder="Red Cross Disaster Relief" />
                            </div>
                            <div class="form-group" style="margin-bottom:0;">
                                <label class="form-label">Registration / Tax Number *</label>
                                <input type="text" name="registrationNumber" id="input-registrationNumber" class="form-control" placeholder="NGO-REG-98721" />
                            </div>
                        </div>

                        <!-- Conditional Volunteer fields -->
                        <div id="volunteer-fields" style="display: none; background: var(--bg); padding: 1rem; border-radius: var(--radius); margin-bottom: 1rem;">
                            <div class="form-group">
                                <label class="form-label">Skills & Certifications</label>
                                <input type="text" name="skills" class="form-control" placeholder="First Aid, Boat Rescue, Medical, Food Prep" />
                            </div>
                            <div class="form-group" style="margin-bottom:0;">
                                <label class="form-label">Availability</label>
                                <input type="text" name="availability" class="form-control" placeholder="Full-time, Weekends, 24/7 on call" />
                            </div>
                        </div>

                        <button type="submit" id="btn-register-submit" class="btn btn-primary" style="width: 100%; justify-content: center; padding: 0.75rem; margin-top: 0.5rem;">
                            <span>Complete Registration</span>
                        </button>
                    </form>

                    <div style="text-align: center; margin-top: 1.5rem; font-size: 0.875rem; color: var(--text-muted);">
                        Already registered? <a href="#login" style="color: var(--secondary); font-weight: 600; text-decoration: none;">Sign in here</a>
                    </div>
                </div>
            </div>
        `;

        const form = document.getElementById('app-register-form');
        const submitBtn = document.getElementById('btn-register-submit');
        const roleSelect = document.getElementById('reg-role-select');
        const ngoFields = document.getElementById('ngo-fields');
        const volFields = document.getElementById('volunteer-fields');
        const orgNameInput = document.getElementById('input-orgName');
        const regNumInput = document.getElementById('input-registrationNumber');

        roleSelect.addEventListener('change', () => {
            const val = roleSelect.value;
            if (val === 'NGO') {
                ngoFields.style.display = 'block';
                volFields.style.display = 'none';
                if (orgNameInput) orgNameInput.required = true;
                if (regNumInput) regNumInput.required = true;
            } else if (val === 'VOLUNTEER') {
                volFields.style.display = 'block';
                ngoFields.style.display = 'none';
                if (orgNameInput) orgNameInput.required = false;
                if (regNumInput) regNumInput.required = false;
            } else {
                ngoFields.style.display = 'none';
                volFields.style.display = 'none';
                if (orgNameInput) orgNameInput.required = false;
                if (regNumInput) regNumInput.required = false;
            }
        });

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (submitBtn.disabled) return;

            const fd = new FormData(form);
            const payload = {
                name: (fd.get('name') || '').trim(),
                email: (fd.get('email') || '').trim(),
                password: fd.get('password') || '',
                phone: (fd.get('phone') || '').trim(),
                role: fd.get('role'),
                location: (fd.get('location') || '').trim() || null
            };

            if (payload.role === 'NGO') {
                payload.orgName = (fd.get('orgName') || '').trim() || payload.name;
                payload.registrationNumber = (fd.get('registrationNumber') || '').trim() || 'NGO-REG-PENDING';
            } else if (payload.role === 'VOLUNTEER') {
                payload.skills = (fd.get('skills') || '').trim() || 'General Volunteer';
                payload.availability = (fd.get('availability') || '').trim() || 'Available';
            }

            // Visible loading state and debounce
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="btn-spinner"></span> <span>Creating Account...</span>`;

            try {
                await AuthService.register(payload);
                window.location.hash = '#login';
            } catch (err) {
                // Restore button state
                submitBtn.disabled = false;
                submitBtn.innerHTML = `<span>Complete Registration</span>`;
            }
        });
    }
}

// Instantiate on DOM load
document.addEventListener('DOMContentLoaded', () => {
    new App();
});
