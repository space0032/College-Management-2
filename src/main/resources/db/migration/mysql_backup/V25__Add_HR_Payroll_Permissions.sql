-- Add missing permissions for new features
-- This migration ensures all new functionality has proper permission controls

-- Employee Management Permissions
INSERT INTO permissions (code, name, category, description)
SELECT 'MANAGE_EMPLOYEES', 'Manage Employees (HR)', 'HR', 
       'Access to Employee Management, edit employee profiles, and manage salary information'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'MANAGE_EMPLOYEES');

INSERT INTO permissions (code, name, category, description)
SELECT 'VIEW_EMPLOYEES', 'View Employees', 'HR', 
       'View employee list and basic information'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_EMPLOYEES');

-- Payroll Management Permissions
INSERT INTO permissions (code, name, category, description)
SELECT 'MANAGE_PAYROLL', 'Manage Payroll', 'Finance', 
       'Full access to payroll management including generation, editing, and bulk operations'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'MANAGE_PAYROLL');

INSERT INTO permissions (code, name, category, description)
SELECT 'VIEW_PAYROLL', 'View Payroll', 'Finance', 
       'View payroll entries and reports'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'VIEW_PAYROLL');

INSERT INTO permissions (code, name, category, description)
SELECT 'APPROVE_PAYROLL', 'Approve Payroll Payments', 'Finance', 
       'Mark payroll entries as paid and perform bulk payment operations'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'APPROVE_PAYROLL');

-- Grant permissions to appropriate roles
SET @manage_emp_id = (SELECT id FROM permissions WHERE code = 'MANAGE_EMPLOYEES' LIMIT 1);
SET @view_emp_id = (SELECT id FROM permissions WHERE code = 'VIEW_EMPLOYEES' LIMIT 1);
SET @manage_payroll_id = (SELECT id FROM permissions WHERE code = 'MANAGE_PAYROLL' LIMIT 1);
SET @view_payroll_id = (SELECT id FROM permissions WHERE code = 'VIEW_PAYROLL' LIMIT 1);
SET @approve_payroll_id = (SELECT id FROM permissions WHERE code = 'APPROVE_PAYROLL' LIMIT 1);

SET @admin_role_id = (SELECT id FROM roles WHERE code = 'ADMIN' LIMIT 1);
SET @finance_role_id = (SELECT id FROM roles WHERE code = 'FINANCE' LIMIT 1);

-- Admin gets all HR and Payroll permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @manage_emp_id WHERE @admin_role_id IS NOT NULL AND @manage_emp_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @view_emp_id WHERE @admin_role_id IS NOT NULL AND @view_emp_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @manage_payroll_id WHERE @admin_role_id IS NOT NULL AND @manage_payroll_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @view_payroll_id WHERE @admin_role_id IS NOT NULL AND @view_payroll_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @admin_role_id, @approve_payroll_id WHERE @admin_role_id IS NOT NULL AND @approve_payroll_id IS NOT NULL;

-- Finance Manager gets payroll permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @finance_role_id, @manage_payroll_id WHERE @finance_role_id IS NOT NULL AND @manage_payroll_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @finance_role_id, @view_payroll_id WHERE @finance_role_id IS NOT NULL AND @view_payroll_id IS NOT NULL;

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT @finance_role_id, @approve_payroll_id WHERE @finance_role_id IS NOT NULL AND @approve_payroll_id IS NOT NULL;
