-- Immutable baseline migration. Add new migrations for later changes.

SET NAMES utf8mb4;

CREATE TABLE user_account (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    owner_id BIGINT NULL,
    active BIT NOT NULL,
    tax_registered BIT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE building (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    owner_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    wilayat VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    investment_value DECIMAL(15,3) NULL,
    reminder_days INT NOT NULL,
    seasonal_start INT NOT NULL,
    seasonal_end INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE building_access (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    can_write BIT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE unit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    code VARCHAR(255) NOT NULL,
    floor_name VARCHAR(255) NOT NULL,
    size DECIMAL(15,3) NOT NULL,
    kind VARCHAR(40) NOT NULL,
    availability VARCHAR(40) NOT NULL,
    market_rent DECIMAL(15,3) NOT NULL,
    vacancy_since DATE NOT NULL,
    listing TEXT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE tenant (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    account_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    kind VARCHAR(40) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    emergency_contact VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE lease (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    rent DECIMAL(15,3) NOT NULL,
    deposit DECIMAL(15,3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    tax_treatment VARCHAR(40) NOT NULL,
    tax_rate DECIMAL(7,4) NOT NULL,
    supply_classification VARCHAR(255) NOT NULL,
    owner_tax_registered BIT NOT NULL,
    municipality_status VARCHAR(40) NOT NULL,
    municipality_authority VARCHAR(255) NOT NULL,
    municipality_reference VARCHAR(255) NOT NULL,
    municipality_fee DECIMAL(15,3) NOT NULL,
    previous_lease_id BIGINT NULL,
    terminated_on DATE NULL
) ENGINE=InnoDB;

CREATE TABLE due (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    lease_id BIGINT NOT NULL,
    due_date DATE NOT NULL,
    rent_amount DECIMAL(15,3) NOT NULL,
    tax_amount DECIMAL(15,3) NOT NULL,
    amount DECIMAL(15,3) NOT NULL,
    cancelled BIT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE payment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    lease_id BIGINT NOT NULL,
    amount DECIMAL(15,3) NOT NULL,
    method VARCHAR(40) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    effective_date DATE NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    reversed_on DATE NULL,
    reversal_reason VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE allocation (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    payment_id BIGINT NOT NULL,
    due_id BIGINT NOT NULL,
    amount DECIMAL(15,3) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE cheque (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    lease_id BIGINT NOT NULL,
    cheque_number VARCHAR(255) NOT NULL,
    bank VARCHAR(255) NOT NULL,
    cheque_date DATE NOT NULL,
    amount DECIMAL(15,3) NOT NULL,
    status VARCHAR(40) NOT NULL,
    payment_id BIGINT NULL
) ENGINE=InnoDB;

CREATE TABLE deposit_entry (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    lease_id BIGINT NOT NULL,
    kind VARCHAR(40) NOT NULL,
    amount DECIMAL(15,3) NOT NULL,
    effective_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    reverses_id BIGINT NULL
) ENGINE=InnoDB;

CREATE TABLE expense (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NULL,
    amount DECIMAL(15,3) NOT NULL,
    expense_date DATE NOT NULL,
    category VARCHAR(40) NOT NULL,
    description VARCHAR(255) NOT NULL,
    reversed_on DATE NULL,
    reversal_reason VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE maintenance (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    tenant_id BIGINT NULL,
    description TEXT NOT NULL,
    category VARCHAR(40) NOT NULL,
    urgent BIT NOT NULL,
    status VARCHAR(40) NOT NULL,
    assigned_to BIGINT NULL,
    approved_summary VARCHAR(255) NOT NULL,
    proposed_summary VARCHAR(255) NOT NULL,
    proposed_category VARCHAR(40) NOT NULL,
    ai_status VARCHAR(40) NOT NULL,
    resolved_at DATETIME(6) NULL,
    closed_at DATETIME(6) NULL
) ENGINE=InnoDB;

CREATE TABLE audit_event (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NULL,
    resource_type VARCHAR(40) NOT NULL,
    resource_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action VARCHAR(40) NOT NULL,
    note TEXT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE vendor_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    categories VARCHAR(255) NOT NULL,
    hourly_rate DECIMAL(15,3) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE preventive_task (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(40) NOT NULL,
    interval_days INT NOT NULL,
    next_due DATE NOT NULL,
    last_completed DATE NULL,
    enabled BIT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE safety_record (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    kind VARCHAR(40) NOT NULL,
    reference VARCHAR(255) NOT NULL,
    expiry_date DATE NOT NULL,
    notes VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE meter (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NULL,
    account_number VARCHAR(255) NOT NULL,
    kind VARCHAR(40) NOT NULL,
    responsibility VARCHAR(40) NOT NULL,
    common_area BIT NOT NULL,
    alert_threshold DECIMAL(15,3) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE meter_reading (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    meter_id BIGINT NOT NULL,
    reading_date DATE NOT NULL,
    value DECIMAL(15,3) NOT NULL,
    kind VARCHAR(40) NOT NULL,
    observation VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE notice (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    title_ar VARCHAR(255) NOT NULL,
    title_en VARCHAR(255) NOT NULL,
    body_ar TEXT NOT NULL,
    body_en TEXT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE leasing_lead (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    viewing_at DATETIME(6) NULL,
    follow_up_date DATE NULL,
    notes TEXT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE visit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NULL,
    visitor_name VARCHAR(255) NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    check_in DATETIME(6) NOT NULL,
    check_out DATETIME(6) NULL
) ENGINE=InnoDB;

CREATE TABLE guard_check_in (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    note VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE parking (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    space VARCHAR(255) NOT NULL,
    vehicle VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE document (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    unit_id BIGINT NULL,
    tenant_id BIGINT NULL,
    maintenance_id BIGINT NULL,
    reading_id BIGINT NULL,
    kind VARCHAR(40) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    scan_status VARCHAR(40) NOT NULL,
    expiry_date DATE NULL
) ENGINE=InnoDB;

CREATE TABLE follow_up (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    lease_id BIGINT NOT NULL,
    note VARCHAR(255) NOT NULL,
    next_date DATE NULL
) ENGINE=InnoDB;

CREATE TABLE tax_policy (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    building_id BIGINT NOT NULL,
    treatment VARCHAR(40) NOT NULL,
    supply_classification VARCHAR(255) NOT NULL,
    rate DECIMAL(7,4) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL
) ENGINE=InnoDB;

ALTER TABLE user_account ADD CONSTRAINT fk_user_account_owner_id FOREIGN KEY (owner_id) REFERENCES user_account(id);

ALTER TABLE building ADD CONSTRAINT fk_building_owner_id FOREIGN KEY (owner_id) REFERENCES user_account(id);

ALTER TABLE building_access ADD CONSTRAINT fk_building_access_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE building_access ADD CONSTRAINT fk_building_access_user_id FOREIGN KEY (user_id) REFERENCES user_account(id);

ALTER TABLE unit ADD CONSTRAINT fk_unit_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE tenant ADD CONSTRAINT fk_tenant_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE tenant ADD CONSTRAINT fk_tenant_account_id FOREIGN KEY (account_id) REFERENCES user_account(id);

ALTER TABLE lease ADD CONSTRAINT fk_lease_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE lease ADD CONSTRAINT fk_lease_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE lease ADD CONSTRAINT fk_lease_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant(id);

ALTER TABLE lease ADD CONSTRAINT fk_lease_previous_lease_id FOREIGN KEY (previous_lease_id) REFERENCES lease(id);

ALTER TABLE due ADD CONSTRAINT fk_due_lease_id FOREIGN KEY (lease_id) REFERENCES lease(id);

ALTER TABLE payment ADD CONSTRAINT fk_payment_lease_id FOREIGN KEY (lease_id) REFERENCES lease(id);

ALTER TABLE allocation ADD CONSTRAINT fk_allocation_payment_id FOREIGN KEY (payment_id) REFERENCES payment(id);

ALTER TABLE allocation ADD CONSTRAINT fk_allocation_due_id FOREIGN KEY (due_id) REFERENCES due(id);

ALTER TABLE cheque ADD CONSTRAINT fk_cheque_lease_id FOREIGN KEY (lease_id) REFERENCES lease(id);

ALTER TABLE cheque ADD CONSTRAINT fk_cheque_payment_id FOREIGN KEY (payment_id) REFERENCES payment(id);

ALTER TABLE deposit_entry ADD CONSTRAINT fk_deposit_entry_lease_id FOREIGN KEY (lease_id) REFERENCES lease(id);

ALTER TABLE deposit_entry ADD CONSTRAINT fk_deposit_entry_reverses_id FOREIGN KEY (reverses_id) REFERENCES deposit_entry(id);

ALTER TABLE expense ADD CONSTRAINT fk_expense_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE expense ADD CONSTRAINT fk_expense_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE maintenance ADD CONSTRAINT fk_maintenance_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE maintenance ADD CONSTRAINT fk_maintenance_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE maintenance ADD CONSTRAINT fk_maintenance_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant(id);

ALTER TABLE maintenance ADD CONSTRAINT fk_maintenance_assigned_to FOREIGN KEY (assigned_to) REFERENCES user_account(id);

ALTER TABLE audit_event ADD CONSTRAINT fk_audit_event_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE audit_event ADD CONSTRAINT fk_audit_event_actor_id FOREIGN KEY (actor_id) REFERENCES user_account(id);

ALTER TABLE vendor_profile ADD CONSTRAINT fk_vendor_profile_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE vendor_profile ADD CONSTRAINT fk_vendor_profile_user_id FOREIGN KEY (user_id) REFERENCES user_account(id);

ALTER TABLE preventive_task ADD CONSTRAINT fk_preventive_task_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE preventive_task ADD CONSTRAINT fk_preventive_task_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE safety_record ADD CONSTRAINT fk_safety_record_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE meter ADD CONSTRAINT fk_meter_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE meter ADD CONSTRAINT fk_meter_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE meter_reading ADD CONSTRAINT fk_meter_reading_meter_id FOREIGN KEY (meter_id) REFERENCES meter(id);

ALTER TABLE notice ADD CONSTRAINT fk_notice_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE leasing_lead ADD CONSTRAINT fk_lead_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE leasing_lead ADD CONSTRAINT fk_lead_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE visit ADD CONSTRAINT fk_visit_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE visit ADD CONSTRAINT fk_visit_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE guard_check_in ADD CONSTRAINT fk_guard_check_in_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE guard_check_in ADD CONSTRAINT fk_guard_check_in_user_id FOREIGN KEY (user_id) REFERENCES user_account(id);

ALTER TABLE parking ADD CONSTRAINT fk_parking_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE parking ADD CONSTRAINT fk_parking_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE document ADD CONSTRAINT fk_document_building_id FOREIGN KEY (building_id) REFERENCES building(id);

ALTER TABLE document ADD CONSTRAINT fk_document_unit_id FOREIGN KEY (unit_id) REFERENCES unit(id);

ALTER TABLE document ADD CONSTRAINT fk_document_tenant_id FOREIGN KEY (tenant_id) REFERENCES tenant(id);

ALTER TABLE document ADD CONSTRAINT fk_document_maintenance_id FOREIGN KEY (maintenance_id) REFERENCES maintenance(id);

ALTER TABLE document ADD CONSTRAINT fk_document_reading_id FOREIGN KEY (reading_id) REFERENCES meter_reading(id);

ALTER TABLE follow_up ADD CONSTRAINT fk_follow_up_lease_id FOREIGN KEY (lease_id) REFERENCES lease(id);

ALTER TABLE tax_policy ADD CONSTRAINT fk_tax_policy_building_id FOREIGN KEY (building_id) REFERENCES building(id);

CREATE UNIQUE INDEX uq_user_account_1 ON user_account (username);

CREATE UNIQUE INDEX uq_building_access_1 ON building_access (building_id, user_id);

CREATE UNIQUE INDEX uq_unit_1 ON unit (building_id, code);

CREATE UNIQUE INDEX uq_due_1 ON due (lease_id, due_date);

CREATE UNIQUE INDEX uq_payment_1 ON payment (lease_id, idempotency_key);

CREATE UNIQUE INDEX uq_allocation_1 ON allocation (payment_id, due_id);

CREATE UNIQUE INDEX uq_cheque_1 ON cheque (lease_id, cheque_number, bank);

CREATE UNIQUE INDEX uq_cheque_2 ON cheque (payment_id);

CREATE UNIQUE INDEX uq_deposit_entry_1 ON deposit_entry (lease_id, idempotency_key);

CREATE UNIQUE INDEX uq_deposit_entry_2 ON deposit_entry (reverses_id);

CREATE UNIQUE INDEX uq_parking_1 ON parking (building_id, space);

CREATE UNIQUE INDEX uq_vendor_profile_1 ON vendor_profile (building_id, user_id);

CREATE INDEX ix_lease_dates ON lease(unit_id, start_date, end_date, status);

CREATE INDEX ix_due_date ON due(due_date);

CREATE INDEX ix_payment_date ON payment(effective_date);

CREATE INDEX ix_document_expiry ON document(expiry_date);

CREATE INDEX ix_audit_resource ON audit_event(resource_type,resource_id,created_at);

ALTER TABLE lease ADD CHECK (end_date >= start_date AND rent > 0 AND deposit >= 0);

ALTER TABLE payment ADD CHECK (amount > 0);

ALTER TABLE allocation ADD CHECK (amount > 0);

ALTER TABLE cheque ADD CHECK (amount > 0);

ALTER TABLE due ADD CHECK (amount >= 0);
