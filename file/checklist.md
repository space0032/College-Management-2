# CampusOne QA Fix Checklist

Source reports reviewed:

- `CampusOne-QA-Audit-2026-09-02.md`
- `report.txt`

## Completed in source

- [x] Make the built-in `ADMIN` role a backend superuser, matching the frontend authorization model.
- [x] Preserve explicit permission checks for every non-admin role.
- [x] Add migration V50 to restore every registered permission mapping for `ADMIN`.
- [x] Add regression tests for admin and non-admin permission behavior.
- [x] Add route-aware browser titles for all application modules.
- [x] Make dashboard API failures visible instead of silently rendering zero/empty data.
- [x] Add explicit Learning Portal load errors.
- [x] Remove unsupported hard-coded download and learning-streak statistics.
- [x] Remove or derive unsupported assignment, employee, scholarship, crowdfunding, volunteer, and Student Affairs demo data.
- [x] Add client validation for employee phone and non-negative salary.
- [x] Add required/range validation for courses, hostels, rooms, room allocations, and visitor phone numbers.
- [x] Add email, phone, and semester validation for students and faculty.
- [x] Prevent future attendance dates and malformed timetable time slots.
- [x] Validate ISBN length, assignment deadlines, and assignment grade ranges.
- [x] Fix V9 fresh-deployment ordering so it no longer alters `student_fees` before V49 creates it.
- [x] Abort API startup on migration failure and print the root database error.
- [x] Prevent student creation from crashing when `enrollmentDate` is omitted; default new records to today.
- [x] Replace additional silent room, volunteer, syllabus, and student-activity load failures with visible errors.
- [x] Remove simulated faculty experience and resource file-size values.

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
- [x] Remove hard-coded dashboard/module statistics identified by the QA audit.
- [ ] Add module-specific browser titles.
- [ ] Replace the public demo password before production use.
- [ ] Retest update, delete, duplicate, concurrency, pagination, filtering, export, and audit-log behavior.

## Release gate

- [ ] Do not mark production-ready until every deployment retest item passes and no core request returns an unexpected 401, 403, or silent failure.
