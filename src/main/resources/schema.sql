-- ==============================================================================
-- DisasterConnect - Database Initialization Script
-- Phase 3: Alerts & Incident Reports Schema
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS disaster_connect_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE disaster_connect_db;

-- 1. Users table supporting all 5 roles
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    location VARCHAR(255),
    is_suspended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. NGO Profiles table (linked to users with role NGO)
CREATE TABLE IF NOT EXISTS ngo_profiles (
    ngo_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    org_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_ngo_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Volunteer Profiles table (linked to users with role VOLUNTEER)
CREATE TABLE IF NOT EXISTS volunteer_profiles (
    volunteer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    skills VARCHAR(255),
    availability VARCHAR(100) DEFAULT 'AVAILABLE',
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_volunteer_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Disaster Alerts table (issued by Authorities / Administrators)
CREATE TABLE IF NOT EXISTS disaster_alerts (
    alert_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    disaster_type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    affected_area VARCHAR(150) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    safety_instructions TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    issued_by BIGINT NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_alert_status (status),
    INDEX idx_alert_affected_area (affected_area),
    INDEX idx_alert_severity (severity),
    CONSTRAINT fk_alert_issued_by FOREIGN KEY (issued_by) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Incident Reports table (submitted by Citizens, reviewed by Authorities/NGOs)
CREATE TABLE IF NOT EXISTS incident_reports (
    incident_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reported_by BIGINT NOT NULL,
    incident_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REPORTED',
    image_url VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_incident_status (status),
    INDEX idx_incident_location (location_name),
    INDEX idx_incident_type (incident_type),
    CONSTRAINT fk_incident_reported_by FOREIGN KEY (reported_by) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Assistance Requests (Emergency Help Requests) table
CREATE TABLE IF NOT EXISTS assistance_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    citizen_id BIGINT NOT NULL,
    assistance_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    people_count INT NOT NULL DEFAULT 1,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    assigned_to BIGINT NULL,
    assigned_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_asst_status (status),
    INDEX idx_asst_citizen (citizen_id),
    INDEX idx_asst_assigned_to (assigned_to),
    INDEX idx_asst_assigned_by (assigned_by),
    INDEX idx_asst_priority (priority),
    INDEX idx_asst_type (assistance_type),
    CONSTRAINT fk_asst_citizen FOREIGN KEY (citizen_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_asst_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT fk_asst_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Request Status Logs (Audit trail for help request status changes)
CREATE TABLE IF NOT EXISTS request_status_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    changed_by BIGINT NOT NULL,
    previous_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    notes VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_log_request (request_id),
    CONSTRAINT fk_log_request FOREIGN KEY (request_id) REFERENCES assistance_requests(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_log_user FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Shelters and Essential Services table
CREATE TABLE IF NOT EXISTS shelters (
    shelter_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    capacity INT NULL,
    available_space INT NULL,
    contact_phone VARCHAR(30) NULL,
    contact_email VARCHAR(100) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_shelter_type (service_type),
    INDEX idx_shelter_status (status),
    INDEX idx_shelter_city (city),
    INDEX idx_shelter_lat_lng (latitude, longitude),
    CONSTRAINT fk_shelter_created_by FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Relief Resources table
CREATE TABLE IF NOT EXISTS relief_resources (
    resource_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ngo_id BIGINT NULL,
    resource_type VARCHAR(50) NOT NULL,
    required_quantity DOUBLE NOT NULL,
    available_quantity DOUBLE NOT NULL,
    unit VARCHAR(30) NOT NULL DEFAULT 'units',
    location VARCHAR(255) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    critical_threshold_percent DOUBLE NOT NULL DEFAULT 25.0,
    low_threshold_percent DOUBLE NOT NULL DEFAULT 50.0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_res_type (resource_type),
    INDEX idx_res_location (location),
    INDEX idx_res_status (status),
    INDEX idx_res_ngo (ngo_id),
    CONSTRAINT fk_res_ngo FOREIGN KEY (ngo_id) REFERENCES ngo_profiles(ngo_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Resource Update Logs table (audit trail per REQ-RES-4)
CREATE TABLE IF NOT EXISTS resource_update_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    previous_quantity DOUBLE NULL,
    new_quantity DOUBLE NOT NULL,
    previous_status VARCHAR(20) NULL,
    new_status VARCHAR(20) NOT NULL,
    notes VARCHAR(255) NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_res_log_resource (resource_id),
    CONSTRAINT fk_res_log_resource FOREIGN KEY (resource_id) REFERENCES relief_resources(resource_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_log_user FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Missing Persons table (REQ-MISS-1 to REQ-MISS-4)
CREATE TABLE IF NOT EXISTS missing_persons (
    missing_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reported_by BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL,
    last_seen_location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    contact_info VARCHAR(255) NOT NULL,
    photo_url VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'MISSING',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_missing_name (name),
    INDEX idx_missing_location (last_seen_location),
    INDEX idx_missing_status (status),
    INDEX idx_missing_reported_by (reported_by),
    CONSTRAINT fk_missing_reported_by FOREIGN KEY (reported_by) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Relief Campaigns table (REQ-CAMP-1 to REQ-CAMP-4)
CREATE TABLE IF NOT EXISTS relief_campaigns (
    campaign_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ngo_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    target_amount DOUBLE NOT NULL,
    raised_amount DOUBLE NOT NULL DEFAULT 0.0,
    donation_instructions TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    start_date DATETIME(6) NOT NULL,
    end_date DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    INDEX idx_camp_ngo (ngo_id),
    INDEX idx_camp_status (status),
    INDEX idx_camp_verification (verification_status),
    CONSTRAINT fk_camp_ngo FOREIGN KEY (ngo_id) REFERENCES ngo_profiles(ngo_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Notifications table (REQ-NOTIF-1 to REQ-NOTIF-4)
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50) NULL,
    related_entity_id BIGINT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_read (is_read),
    INDEX idx_notif_created (created_at),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Admin Action Logs table (REQ-ADMIN-4, DDS Section 3.10)
CREATE TABLE IF NOT EXISTS admin_action_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    details TEXT NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_admin_log_admin (admin_id),
    INDEX idx_admin_log_created (created_at),
    CONSTRAINT fk_admin_log_admin FOREIGN KEY (admin_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
