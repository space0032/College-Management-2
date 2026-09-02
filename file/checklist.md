# CampusOne QA Fix Checklist

Source reports reviewed:

- `CampusOne-QA-Audit-2026-09-02.md`
- `report.txt`

## Completed in source

- [x] Make the built-in `ADMIN` role a backend superuser, matching the frontend authorization model.
- [x] Preserve explicit permission checks for every non-admin role.
- [x] Add migration V50 to restore every registered permission mapping for `ADMIN`.
- [x] Add regression tests for admin and non-admin permission behavior.

## Deployment retest required

- [ ] Deploy the latest backend commit on Render.
- [ ] Confirm Render connects through the Supabase session pooler and completes migrations through V50.
- [ ] Log out and log back in so the browser receives a fresh session.
- [ ] Verify admin GET/POST/PATCH/DELETE access for Departments, Students, Faculty, Courses, Library, Hostels, Resources, Placements, Announcements, Employees, Roles, and Audit Log.
- [ ] Run CRUD in dependency order: Department -> Course/Faculty/Student -> Attendance/Grades/Assignments.
- [ ] Run hostel CRUD in dependency order: Hostel -> Room -> Allocation.

## Remaining product fixes

- [ ] Return structured API errors with a stable code, message, correlation ID, and field errors.
- [ ] Show request errors separately from genuine empty datasets; add Retry actions.
- [ ] Add relationship-backed selectors for department, course, student, faculty, hostel, and room references.
- [ ] Add matching frontend and backend required/range/format/date validation.
- [ ] Reject invalid email, phone, ISBN, negative capacity/credits/salary, malformed time slots, and invalid foreign keys.
- [ ] Add clear loading, timeout, offline, success, and failure states to every mutation.
- [ ] Remove hard-coded dashboard/module statistics or label them explicitly as demo data.
- [ ] Add module-specific browser titles.
- [ ] Replace the public demo password before production use.
- [ ] Retest update, delete, duplicate, concurrency, pagination, filtering, export, and audit-log behavior.

## Release gate

- [ ] Do not mark production-ready until every deployment retest item passes and no core request returns an unexpected 401, 403, or silent failure.
