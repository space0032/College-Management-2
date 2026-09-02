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
- [x] Send required department codes from the UI.
- [x] Default missing faculty join dates instead of crashing.
- [x] Align announcement audience fields and populate creator/active state server-side.
- [x] Supply safe notification defaults and allow role/broadcast notifications without an invalid user FK.
- [x] Add the missing hostel update route.
- [x] Stop placement endpoints from returning false success when persistence fails.
- [x] Record successful web login, logout, create, update, and delete operations in the audit log.
- [x] Serialize employee status enums correctly so staff retrieval cannot recurse/fail during JSON output.
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

- [x] Make Department Name natively required.
- [x] Add the required Department Code to the Institute Management form and validate it server-side.
- [x] Parse quoted numeric form fields correctly so hostel capacities and room foreign keys are not silently stored as zero.
- [x] Add the missing PostgreSQL course specialization column used by CourseDAO.
- [x] Synchronize PostgreSQL primary-key sequences after explicit-ID bootstrap/import data.
- [x] Align Timetable UI fields with the API model (`dayOfWeek`, `facultyName`, and `roomNumber`) and validate required data server-side.
- [x] Add the missing Faculty specialization column.
- [x] Parse browser date-time values and complete required Event fields server-side and in the UI.
- [x] Synchronize Assignment, Event, Timetable, Club, and Resource sequences.
- [x] Align Placement Drive form fields with the backend model and require both drive and deadline dates.
- [x] Restore visible Hostel/Room loading and mutation errors instead of discarding their state.
- [x] Normalize Student, Faculty, and Staff edit forms and preserve legacy academic values during cleanup.
- [x] Replace free-text Department/Course inputs in academic create forms with master-data selectors.
- [x] Require student department, course, and semester fields.
- [x] Add missing timetable, bulk-grade, attendance, and visitor field constraints.
- [x] Exclude weekends from the Academic Calendar standard-day count.

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
- [x] Preserve the discovered visitor phone and existing check-in fields after a new-number lookup.
- [x] Derive assignment ownership from the authenticated session and validate assignment payloads.
- [x] Generate required role codes server-side and resynchronize the PostgreSQL role sequence.
- [x] Route Student search requests to the database search operation.
- [x] Derive staff leave ownership from authentication and validate its date range.
- [x] Prevent stale Institute Management tab requests from replacing current data.
- [x] Keep CSV blob URLs alive until Edge has started the download.
