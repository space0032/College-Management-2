# CampusOne Full Website & CRUD QA Audit

**Application:** CampusOne | College Management  
**URL:** https://merry-pothos-678398.netlify.app/dashboard  
**Audit date:** 2 September 2026  
**Account tested:** `admin` / `ADMIN` role  
**Method:** Live authenticated UI testing using synthetic records prefixed with `QA`.

## Executive summary

CampusOne is not ready for production use. The frontend is broadly navigable, but core backend authorization is broken for the supplied admin account. Many reads and almost every tested create operation fail with HTTP 403 or a generic failure. This prevents a true end-to-end CRUD cycle and breaks dependent workflows such as department → course/student/faculty, hostel → room → allocation, and student/course → attendance/grades/assignments.

No record creation was confirmed. No pre-existing record was edited or deleted. The final visitor check-in request caused the cloud browser connection to time out, so the creation status of `QA Visitor 20260902` could not be verified.

## Release recommendation

**Block release.** Fix authorization, API availability, error handling, and dependency loading before further feature testing. After that, rerun the full CRUD and relationship test suite.

## Critical blockers

### C-01 — Admin authorization is misconfigured

The supplied admin account receives HTTP `403` responses across core modules. Confirmed affected areas include Students, Courses, Departments, Room Availability, Resources, Placements, Announcements, Notifications, Employees, Faculty Workload, Audit Log, and Role/User operations.

The clearest error appears in Library creation:

> Forbidden: Requires permission CREATE_LIBRARY

An `ADMIN` account can see create controls but lacks the permissions needed to use them.

### C-02 — Primary CRUD workflows cannot complete

| Module | Read | Create | Update | Delete |
| --- | --- | --- | --- | --- |
| Departments | Fails to load | `Failed to save department.` | Unreachable | Unreachable |
| Students | Fails to load | `Failed to save student.` | Unreachable | Unreachable |
| Faculty/Teachers | Shows empty list | `Failed to save faculty.` | Unreachable | Unreachable |
| Employees/Staff | `System error retrieving staff records.` | Submission gives no usable success/failure state | Unreachable | Unreachable |
| Hostels | Shows zero without load error | Save silently fails | Unreachable | Unreachable |
| Hostel Rooms | No parent hostel options | Save cannot complete | Unreachable | Unreachable |
| Room Allocations | No room options | Save cannot complete | Unreachable | Unreachable |
| Courses | Fails to load | `Failed to save course.` | Unreachable | Unreachable |
| Timetable | Requires manual filter | `Failed to save entry.` | Unreachable | Unreachable |
| Library Books | Shows empty list | Explicit `CREATE_LIBRARY` 403 | Unreachable | Unreachable |

### C-03 — Referential dependency chains are unusable

1. Department creation fails, so department-backed student, faculty, and course workflows cannot be validated.
2. Hostel creation fails, leaving Room registration with only `Select Hostel`.
3. Room creation cannot complete, leaving Allocation with only `Select Room`.
4. Students and Courses cannot load, leaving Grade Entry with no selectable student or course.
5. Courses cannot load, leaving Bulk Grades, Assignments, and Syllabus blocked.

### C-04 — API failures are presented as real zero data

Several modules convert failed API responses into `0`, an empty list, or a blank state. Users cannot distinguish “there are no records” from “the system failed to retrieve records.” This is a dangerous data-integrity and operations problem.

### C-05 — Public credentials are unsafe

The published credentials `admin / admin123` are extremely weak. They must only exist in an isolated disposable demo, never in a production or shared environment.

## CRUD and validation defects

### Departments

- Empty submission correctly reports `Department name is required.`
- Valid synthetic submission reports `Failed to save department.`
- Update and delete controls cannot be tested because no record loads or can be created.

### Students

- Empty submission reports `Name and email are required.`
- An invalid email was allowed to reach the save attempt instead of being rejected clearly at the client boundary.
- Phone, department, and course are unrestricted text fields.
- Department and course are not relationship-backed selectors, allowing orphan or misspelled references.
- Semester has no confirmed business-range validation.
- Valid synthetic submission reports `Failed to save student.`

### Faculty / Teachers

- Empty submission reports `Name and email are required.`
- Department is unrestricted text instead of a relationship-backed selector.
- Phone and qualification lack format/length constraints.
- Synthetic submission reports `Failed to save faculty.`
- The list displays zero faculty without explaining whether its initial read succeeded.

### Employees / Staff

- Required browser validation exists for first name, last name, official ID, designation/unit, and work email.
- Phone accepts alphabetic text such as `abc`.
- Annual salary has no minimum and accepts `-1000`.
- Join date has no visible business-rule constraints.
- The list reports `System error retrieving staff records.` while the header still shows a simulated 98.2% retention rate.
- Submission produced no clear success or application-level failure feedback.

### Hostels

- None of the hostel input fields use native required constraints.
- Empty Save produces no validation message.
- Total capacity has no minimum and accepts negative values.
- Warden contact accepts arbitrary text.
- Both negative-capacity and valid-looking submissions leave the dialog open with no success or failure explanation.

### Hostel Rooms

- Hostel selector contains only `Select Hostel` because no hostel can be loaded or created.
- Room number, floor, and capacity are not marked required.
- Floor and capacity have no minimum constraints.
- Empty Save produces no validation error and waits on the backend.

### Room Allocations

- Room selector contains only `Select Room`.
- Student ID is a free numeric field instead of a loaded student relationship.
- Student ID has no minimum or existence validation.
- Student ID, room, and check-in date are not natively required.
- Empty Save produces no validation error.
- The UI allows opening an allocation form even though there are no allocatable rooms.

### Courses

- Empty Save reports `Name and code are required.`
- Labels show required fields, but inputs do not use native required constraints.
- Credits have no minimum and accept negative values.
- Department is unrestricted text, allowing invalid relationships.
- Semester can remain unselected.
- Synthetic submission with negative credits and a nonexistent department reaches the backend and reports `Failed to save course.`

### Attendance

- Fetch with empty Course ID and Date correctly reports `Please enter both Course ID and Date.`
- Course ID is unrestricted text, not a loaded course relationship.
- Individual attendance fields are not natively required; application validation reports `All fields are required.`
- Student ID and Course ID are unrestricted text values.
- Attendance date has no maximum, so future dates are not prevented at the field level.
- Bulk `Load Class List` with empty inputs silently does nothing.
- Bulk `Submit Batch` with empty inputs reports `Course, Date, and Student list are required.`

### Timetable

- Empty Save reports `Day, time slot, and subject are required.`
- Inputs are not natively required.
- Time slot is unrestricted text and accepts `not-a-time`.
- Room and Faculty are unrestricted text rather than relationship-backed selectors.
- Synthetic submission reports `Failed to save entry.`
- Department and semester filters are manual text/number fields, allowing invalid combinations.

### Grades

- Marks correctly use required, minimum `0`, and maximum `100` constraints.
- Student and Course selects are required, but each contains only its placeholder because dependencies fail to load.
- Bulk Entry cannot progress because the Course selector has no course options.
- Update and transcript validation are unreachable.

### Assignments

- Title, description, deadline, and course use native required constraints.
- Course selector contains only `-- Select Course --`.
- Deadline has no minimum, so a past deadline is not prevented at the field level.
- Create, grading, and submission flows cannot progress without Courses.
- The page displays an 82% average score while there are zero assignments.

### Resources

- The entire module is replaced by `Failed to load resources data.`
- No resource list, upload control, retry control, or partial UI is available.

### Syllabus

- `+ Upload Framework` is disabled because no course is available.
- The page does not explain why the control is disabled or how to resolve it.
- No upload or versioning CRUD test can begin.

### Library

- Empty Save reports `Title and author are required.`
- ISBN is unrestricted and accepts `NOT-AN-ISBN`.
- Synthetic creation returns the precise permission error `Forbidden: Requires permission CREATE_LIBRARY`.
- Catalog, issued-book, request, update, and delete workflows cannot progress.

### Fees

- The page initially remains in `Loading fees…`, then falls back to `No fees found.`
- No error is shown to distinguish a failed request from a genuine empty dataset.
- No add, collect-payment, adjustment, or refund control is available to the admin in the tested UI.

### Visitors

- Phone is labeled required but is not actually marked required.
- Phone is a generic text input with no format constraint.
- ID reference has no document-specific validation.
- The page shows 8 check-ins today despite zero visible on-site visitors; this may be historically possible but needs clear context.
- The final synthetic visitor submission caused a browser/backend wait timeout; creation status is unverified.

### Gate Pass

- Pending Approval and Audit Logs load as empty.
- Approved Today and off-campus metrics remain zero.
- No create-request path is available to the admin account, so approval/rejection transitions cannot be tested.

### Placements

- Page reports `Failed to load placement data.`
- `+ Add Company` is visible, but the list dependency is unavailable.
- Company and placement-drive update/delete workflows are unreachable.

### Scholarships

- Page reports zero active grants while showing ₹12.4M total disbursed, ₹45K average award, and 240+ beneficiaries.
- These values appear hard-coded or disconnected from the active dataset.

### Crowdfunding

- Page reports zero active projects and ₹0+ cumulative funding while showing 12 donors, 94% success, named top contributors, donation amounts, and a successful impact feed.
- These values appear hard-coded or disconnected from the active dataset.

## Previously confirmed read/UI defects

- Students: `Failed to load students.`
- Courses: `Failed to load courses.`
- Departments: `Failed to load departments.`
- Room Availability: `Failed to query room availability.`
- Resources: `Failed to load resources data.`
- Placements: `Failed to load placement data.`
- Announcements and Notifications fail to load.
- Employees: `System error retrieving staff records.`
- Faculty Workload fails to load.
- Audit Log: `Failed to load audit logs. Ensure backend is running.`
- Role/user request logs an application error.
- Student Profile tries to load a student profile for the admin account and fails.
- Dashboard reports two students while Student Management reports zero.
- Student Affairs displays Alice and Bob while Students cannot be retrieved.
- Gold volunteer rank is displayed with zero volunteer hours.
- Learning Portal shows 1.4K+ downloads and a 12-day streak with zero learning materials.
- All tested routes use the generic `CampusOne | College Management` browser title instead of module-specific titles.
- The sidebar states `System online` and `Secure campus workspace` while multiple core API operations are failing.

## Error-handling and UX defects

1. Error messages are inconsistent: precise permission errors in Library, generic failures in Students/Courses/Departments/Faculty/Timetable, and silent failures in Hostel/Room/Allocation/Staff.
2. Several actions wait for seconds without a progress indicator or timeout guidance.
3. Empty states are shown after failed reads.
4. Disabled controls frequently lack explanations.
5. There are no visible retry buttons on many failed modules.
6. Simulated/hard-coded statistics are displayed beside live zero or failed data without being labeled as demo data.

## Required fix order

1. Correct role-to-permission mapping for `ADMIN`; verify GET/POST/PATCH/DELETE permissions for every module.
2. Return structured API errors with status, stable error code, readable message, correlation ID, and field errors.
3. Stop converting request failures into zero/empty datasets.
4. Add relationship-backed selectors and server-side foreign-key validation.
5. Add required, range, format, uniqueness, date, and cross-field validation on both frontend and backend.
6. Remove or clearly label all simulated statistics.
7. Add loading, retry, timeout, and offline/error states.
8. Retest CRUD in dependency order: Department → Course/Faculty/Student → Attendance/Grades/Assignments; Hostel → Room → Allocation.
9. Retest update, delete, duplicate, stale-data, concurrency, pagination, search, filter, export, and audit-log behavior.
10. Replace demo credentials before any non-disposable deployment.

## Retest data set

Use clearly isolated records such as:

- Department: `QA Engineering 20260902`
- Course: `QA Course 20260902` / `QA-COURSE`
- Student: `QA Student 20260902`
- Faculty: `QA Faculty 20260902`
- Employee: `QA-STAFF-20260902`
- Hostel: `QA Hostel 20260902`
- Room: `QA-101`
- Visitor: `QA Visitor 20260902`

After all update and relationship tests pass, delete only these synthetic records and verify that dependent deletes are blocked, cascaded, or archived according to the intended business rules.

## Audit limitation

The live browser connection stopped responding during the visitor submission after a large multi-page audit. The browser could not be safely reconnected without risking duplicate submissions. All findings above are based on states directly observed before the timeout plus the earlier completed read-only route audit. Deeper API/database/security testing requires the source repository or project ZIP, or a repaired test environment with working admin permissions.
