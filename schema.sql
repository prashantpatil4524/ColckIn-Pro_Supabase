-- ============================================================
-- ClockInPro - Supabase (PostgreSQL) Schema
-- Run this in: Supabase Dashboard → SQL Editor → New Query
-- ============================================================

-- EMPLOYEES TABLE
-- Uses SERIAL (PostgreSQL auto-increment) instead of MySQL's AUTO_INCREMENT
CREATE TABLE IF NOT EXISTS employees (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE NOT NULL,
    password    VARCHAR(100) NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    role        VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE'
);

-- ATTENDANCE TABLE
-- Uses TIMESTAMP instead of DATETIME (PostgreSQL standard)
CREATE TABLE IF NOT EXISTS attendance (
    id          SERIAL PRIMARY KEY,
    employee_id INT NOT NULL,
    login_time  TIMESTAMP NOT NULL,
    logout_time TIMESTAMP,
    total_hours DECIMAL(10, 2),
    CONSTRAINT fk_attendance_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- PAYROLL TABLE
-- UNIQUE constraint on (employee_id, month) enables ON CONFLICT upsert in PayrollDAO
CREATE TABLE IF NOT EXISTS payroll (
    id           SERIAL PRIMARY KEY,
    employee_id  INT NOT NULL,
    month        VARCHAR(7) NOT NULL,   -- Format: YYYY-MM
    total_hours  DECIMAL(10, 2) NOT NULL,
    total_salary DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_payroll_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT uq_payroll_employee_month UNIQUE (employee_id, month)
);

-- ============================================================
-- SEED DATA - Default admin + sample employees
-- ============================================================

INSERT INTO employees (name, email, password, hourly_rate, role) VALUES
    ('System Admin',  'admin@clockinpro.com', 'admin123',    0.00, 'ADMIN'),
    ('Alice Smith',   'alice@example.com',    'password123', 25.00, 'EMPLOYEE'),
    ('Bob Jones',     'bob@example.com',      'password123', 20.00, 'EMPLOYEE')
ON CONFLICT (email) DO NOTHING;
