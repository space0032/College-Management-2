-- Stop on existing duplicate employee periods; do not discard financial history.
-- To identify records requiring review:
-- SELECT employee_id, month, year, COUNT(*) FROM payroll_entries
-- GROUP BY employee_id, month, year HAVING COUNT(*) > 1;
CREATE UNIQUE INDEX IF NOT EXISTS uq_payroll_employee_period
    ON payroll_entries (employee_id, month, year);
