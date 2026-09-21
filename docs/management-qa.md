# Management regression checks

## Automated checks

From the repository root:

```text
mvn test -Dtest=ManagementControllerTest,ManagementIntegrityTest,PermissionServiceTest,TokenStoreTest
npm --prefix web-app test -- --watchAll=false --runInBand
npm --prefix web-app run build
```

On Windows with PowerShell script execution disabled, use `npm.cmd`.
The management integrity tests use isolated H2 in-memory databases. They do not
connect to the configured institute database or run migrations against it.

## Local browser acceptance

- Check Institute Management, Roles, Employees and Payroll at desktop and phone widths.
- Test an administrator, a delegated HR user, a view-only user and a user without access.
- Verify role creation from both Enter and the modal footer. Rapidly switch permission roles;
  older responses must not replace the new selection. Unsaved role/tab switches require confirmation.
- Check role assignment and permission changes refresh current-user navigation. Protected and
  assigned roles must not be deletable. A user cannot remove the last administrator or delete themselves.
- Edit a saved employee and set up an unsaved account profile. Updating sends the complete object;
  profile setup requires creation permission. Monthly salary is explicit. Missing fields do not crash search.
- Generate a payroll period twice. Existing amounts must remain unchanged. Review skipped staff reasons.
- Adjust a pending entry with decimal amounts; reject negative net salary. Confirm the pending count,
  total and period before marking all paid. Paid entries cannot be edited or deleted through the API.
- Simulate failed requests and delayed month responses. Show retry errors, never another month's records.

## Migration and compatibility

`V72__Unique_Payroll_Period.sql` adds uniqueness for `(employee_id, month, year)`.
Both PostgreSQL and H2 migration runners perform a duplicate-period preflight.
If duplicates exist, migration stops with their employee IDs, periods and counts.
No financial records are deleted, merged or recalculated automatically.

Before applying to a shared environment, review duplicates with:

```sql
SELECT employee_id, month, year, COUNT(*)
FROM payroll_entries
GROUP BY employee_id, month, year
HAVING COUNT(*) > 1;
```

Salary values retain their existing monthly meaning; historical amounts are not converted.
Existing API paths are retained. Payroll generation additionally returns `existing` and
`skipped` (employee identifier and reason) alongside `generated` and `message`.
Paid entries are locked. Marking an already-paid entry again leaves its payment date unchanged.
The shared desktop employee-profile setup remains supported, while HTTP updates require an existing ID.
