# Academic & Campus Deep Code Audit

**Application:** CampusOne | College Management
**Repo:** College-Management-2
**Audit date:** 22 September 2026
**Method:** Read-only static code review of the Academic + Campus sections (web React SPA, JavaFX desktop, Java REST API, PostgreSQL/H2, RBAC permission system). Nothing was modified.

## Executive summary

The app is broadly navigable but is **not production-ready**. ~15 critical/high features are broken end-to-end, multiple modules have silent data-loss or data-corruption paths, and there is a widespread **permission-code drift (singular vs plural)** that dead-locks whole student-facing features. There is also a significant RBAC structural bug: the migration chain deletes all non-admin roles and never reliably recreates them, so most non-admin users end up with **no role at all → 403 on everything** (ADMIN survives only via a superuser bypass).

This audit confirms and deepens the earlier `docs/CampusOne-QA-Audit-2026-09-02.md` ("block release") findings with exact file:line references.

---

## 1. CRITICAL — broken features, data loss, security

### C1. Students are locked out of nearly every self-service module (permission mismatch)

Backend controllers check singular permission codes that the STUDENT role is never granted, so every action returns 403. The frontend often still renders the form/button (role-based gating), so users see working UI and failures.

| Feature | Controller code required | What STUDENT actually holds |
| --- | --- | --- |
| Assignments (view/submit) | `VIEW_ASSIGNMENT`, `MANAGE_ASSIGNMENT` (`AssignmentController.java:75,243`) | none (legacy plural `VIEW_ASSIGNMENTS`/`SUBMIT_ASSIGNMENTS` wiped by V65) |
| Gate pass request | `CREATE_GATEPASS` (`GatePassController.java:82`) | `REQUEST_GATE_PASS` only |
| Placement apply | `MANAGE_PLACEMENT` (`PlacementController.java:148`) | `VIEW_PLACEMENT` only |
| Scholarship apply | `MANAGE_SCHOLARSHIP` (`ScholarshipController.java:86`) | `SCHOLARSHIP_APPLY` only |
| Club join | `MANAGE_CLUB` (`ClubController.java`) | `JOIN_CLUBS` only |
| Crowdfunding donate | `MANAGE_CROWDFUNDING` (`CrowdfundingController.java:73`) | `CROWDFUNDING_DONATE` only |
| Syllabus view | `VIEW_SYLLABUS` (`SyllabusController.java:69,137`) | not granted (Learning Portal "Frameworks" 403) |
| Own student profile | `VIEW_STUDENT` (`StudentController.java:110`; `StudentProfilePage.js:114` calls `GET /students`) | not granted |

Key references: `V54__Reconcile_Permissions.sql:287-344`, `V2` (legacy), `V65__Delete_Non_Admin_Roles.sql`, `V66__Backfill_Student_Faculty_Roles.sql`, `V71__Ensure_Warden_Role_Assigned.sql`.

**Fix:** grant students the self-service codes (or check the codes they actually hold); add a `/students/me` self-profile route under `VIEW_STUDENT_PROFILE`.

### C2. Migration chain destroys non-admin roles

`V65__Delete_Non_Admin_Roles.sql` deletes every role except ADMIN (cascading `role_permissions`, setting `users.role_id = NULL`). `V66` backfill is a conditional `UPDATE ... WHERE EXISTS (SELECT 1 FROM roles ...)` that **no-ops** when the roles are missing; only WARDEN is recreated (V71 + runtime `WardenDAO.ensureWardenRole()`). Role rows otherwise exist only via `RoleDAO.insertRole` from the admin UI. So V54/V55/V56 grants bind to roles that often don't exist.
`PermissionService` treats ADMIN as a hard superuser bypass, which is why the admin demo account still works while everything else 403s.

**Fix:** recreate STUDENT/FACULTY/FINANCE roles in a forced migration before re-applying role_permissions grants.

### C3. Event budget/poll endpoints are dead (route shadowing) AND vote table missing

- `EventController.java:61` — the catch-all `.*/events.*` is matched **before** the specific budget/poll routes at lines 68-101, so they can never be reached. Worse, `GET /events/{id}/budget` is swallowed by the catch-all and returns the **whole event list** (silently corrupting the budget table). Verified by reading the dispatch order.
- `event_poll_votes` is **never created in any migration** (empty grep across `db/migration/**` and `legacy_scripts`). `EventDAO.java:428,443` query/insert against it → every poll vote SQL-fails with 500.

**Fix:** move/remove the catch-all (match explicit `.*/events` only), budget/poll routes first; add `CREATE TABLE event_poll_votes (... UNIQUE(poll_id, student_id))`.

### C4. Room booking silently overwrites timetable records (data loss)

`TimetableDAO.saveTimetableEntry` (`:87-125`) upserts on `(department, semester, day_of_week, time_slot)` (matching the `unique_timetable_slot` constraint added by V7). `TimetableController.handlePost` only pre-checks **room** occupancy, so booking a free room in a slot where the department/semester already has a class **replaces that class's subject/faculty/room** and returns "success". The `DO UPDATE` also does not update `specialization`/`course_id`, so track-specific rows can become hybrids (CR3).

**Fix:** include `room_number` in the conflict key; reject collisions server-side; align the unique constraint with any required track dimension.

### C5. Course drop endpoint is unreachable

`CourseRegistrationController.java:47` matches `.*/course-registrations/drop/.+` (requires a path segment after `drop/`), but the client (`featureService.js:26-27`) sends `DELETE /course-registrations/drop?studentId=..&courseId=..`. No caller ever matches → every drop returns 405.

**Fix:** match `.*/course-registrations/drop` and read query params (`handleDrop`, lines 132-153, already does this).

### C6. Library: issued-books list always empty + return double-increment

- `BookIssueDAO.java:140-144,247,258-264` — the "issued books by student" query selects `bi.*, b.title as book_title` but `extractIssueFromResultSet` reads `rs.getString("enrollment_id")` outside the guarded try/catch → `SQLException` on the first row, swallowed, empty list. My Books tab always shows zero active loans.
- `BookIssueDAO.java:52-84` (`returnBook`) — the `UPDATE ... SET return_date` lacks `AND status='ISSUED'`, then unconditionally bumps `books.available + 1` (`updateBookAvailability`, lines 207-220) with no upper bound. Returning an already-returned book inflates availability beyond quantity.
- `BookRequestDAO.java:104-145` (`approveRequest`) — status update lacks `WHERE status='PENDING'`; not transactional with the issue INSERT → double-approval re-issues and double-decrements.

**Fix:** add the join for `enrollment_id` (mirror `getIssueById`), gate on `status='ISSUED'` + `executeUpdate()==1`, make approve a single transaction with a PENDING guard.

### C7. Fees: bulk-assign to everyone, missing ledger, 500 on adjustments

- `FeeOperationsDAO.java:171-181` (`bulkAssign`) — when CSV import yields zero matches, `ids.isEmpty()` falls back to `SELECT id FROM students WHERE 1=1` with only cohort filters → fees assigned to **every student**. `FeesPage.js:92-94` sends `studentIds: []` with empty filters for CSV with no matches.
- `/fees/pay` (`EnhancedFeeDAO.recordPaymentDetailed`) writes only `fee_payments`, never a `fee_transactions` PAYMENT row; `FeeOperationsDAO.recordPaymentTransaction` is dead code → ledger can't be reconciled.
- `FeeTransactionDAO.java:56-57` — `FeeTransaction.Type.valueOf`/`PaymentMode.valueOf` on unseeded enum values (`CREDIT_ADJUSTMENT`, NULL mode) throws non-SQLException → 500 on any account with an adjustment.

**Fix:** keep CSV/cohort branches distinct (abort on empty CSV), write ledger rows in the payment transaction, tolerant string→enum mapping.

### C8. Hostel: cascade data loss, negative occupancy, attendance overwrite

- `HostelDAO.java:391-401` (`deleteHostel`) — bare `DELETE FROM hostels WHERE id=?`; `rooms.hostel_id` (V1:189) and `hostel_allocations.room_id` (V1:202) are `ON DELETE CASCADE` → deleting a hostel with residents silently wipes rooms + allocation history. `HostelController.java:181-189` has no guard despite an error message implying one.
- `HostelDAO.java:167-192` (`vacateRoom`) — `UPDATE ... SET status='VACATED'` lacks `AND status='ACTIVE'`; then unconditional `updateRoomOccupancy(-1)`. Double-vacate drives occupancy negative.
- `HostelDAO.java:121-162` (`allocateRoom`) — check (capacity, no active allocation) then separate INSERT; no transaction / partial-unique index → concurrent double-allocation exceeds capacity.
- `HostelAttendanceDAO.java:19-47` — upsert on `UNIQUE(student_id, date)` (V7:57-59) overwrites morning/night marks; `DO UPDATE` doesn't refresh stale `hostel_id`.

**Fix:** guard delete against active rooms/allocations; `AND status='ACTIVE'` + `executeUpdate()==1` on vacate; `SELECT ... FOR UPDATE` on allocate + partial unique index on ACTIVE allocation; add a `shift` dimension to hostel attendance.

### C9. IDOR — students can read other students' private data

`BaseController.java:127-134` / `StudentDAO.java:496-515` (`WHERE s.enrollment_id = ? OR u.username = ?`) resolve a student from any path id with **no ownership check**, and endpoints only gate on broad view perms that STUDENT holds (`VIEW_GATEPASS`, `VIEW_COMPLAINT`, `VIEW_HOSTEL_ATTENDANCE`). Any student can enumerate `GET .../gatepass/student/{id}`, `.../complaints/student/{id}`, `.../hostel-attendance/student/{id}` and read destinations, parent contacts, complaints, attendance. Also `FeeTransactionController.java:50-57` (ledger history) has no owner-scoping.

**Fix:** when caller is a STUDENT, force the queried identity to the token's own student; only WARDEN/ADMIN may pass arbitrary ids.

---

## 2. HIGH severity

### Academic
- **CGPA is mathematically wrong** — `GradeDAO.calculateCGPA` (`:168-181`) averages raw `marks_obtained` ignoring max marks and credits (45/50 = 4.5 not 9.0); mid-term/final/assignment all count equally. The `getGradesByStudent` query even selects `c.credits` and never uses it.
- **Transcript empty for web-entered grades** — `TranscriptService.java:35-48` skips everything except `examType == "Final"`; the web app writes `MID TERM`/`END TERM`/`ASSIGNMENT`/`PRACTICAL` (`GradesPage.js:24`) → desktop transcript CGPA 0.0 while the web CGPA endpoint reports something different. Two "authoritative" calculators disagree.
- **FACULTY corrupts a faculty record while "editing a student"** — `StudentProfilePage.js:611-613` PUTs `/faculty/${targetId}` with student payload when role is FACULTY.
- **Course-registration roster rows lack department/semester/specialization** — `CourseRegistrationDAO.getEnrolledStudentsWithDetails` (`:422-455`) only fills id/name/username; `GradesPage` "Add All" copies null department/semester. The sibling `getEnrolledStudents` query (`:369-420`) runs and discards its result (pure dead work).
- **Workload conflict-check NPE** — `WorkloadController.java:140-141` `fEntry.getDayOfWeek().equals(...)` NPEs on null day/time (legacy rows) → 500, silently disabling conflict protection. Fix with `Objects.equals`.
- **Timetable Sunday invisible** — `TimetablePage.js:6` `DAYS = ['Monday'...'Saturday']`; Sunday classes never display and can't be added.
- **Assignment module (see C1) + submission endpoints** — route regex `submissions/student/\d+` (`AssignmentController.java:56`) vs frontend sending string usernames → 404; `handleGetAssignments` silently returns semester-1 data when studentId not resolvable instead of 400 (`:78-93`).
- **Course registration integrity** — `CourseRegistrationDAO.java:110-128` inserts a second PENDING row after REJECT (no `(student_id, course_id)` unique in live Postgres schema); `approveRequest` (`:163-218`) over-increments `enrolled_count` with no status guard; `StudentDAO.registerCourse` (`:621-655`) relies on `ON CONFLICT (student_id, course_id)` that doesn't exist in the live schema → falls back to plain INSERT duplicating ENROLLED rows.

### Content (resources / syllabus / calendar)
- **General resource upload always 500s** — `ResourceController.java:78-83` `((Double) map.get("courseId")).intValue()` NPEs on `courseId: null` sent by `ResourceManagementPage.js:116` for public resources (same for repeated unguarded `Double` casts).
- **Download-counter increments 403 for non-admin** — `ResourceController.java:99-105` requires `MANAGE_RESOURCE` (ADMIN-only grant) for every download → `download_count` never increments for faculty/students.
- **Calendar merged-ID collision** — `CalendarDAO` (`:31-101`) UNIONs `calendar_events` with `events` (same numeric id range, explicitly commented risk); `updateEvent`/`deleteEvent` always write `calendar_events` → editing/deleting a merged `events` row hits an unrelated row or 404s; Google holidays have `id=0`. React `key={ev.id}` also collides.
- **Calendar working-day math double-counts** — `AcademicCalendarPage.js:165-176` subtracts holidays and exams (incl. weekends, duplicates) from weekday count.
- **ICS/CSV calendar exports malformed** — zero-length `DTEND` (RFC 5545 requires +1 day), no UID/DTSTAMP, no TEXT escaping, bare `\n`; CSV lacks quote-escaping and UTF-8 BOM (`AcademicCalendarPage.js:177-202`).
- **Syllabus** — students blocked (C1); portal download `<a href={s.filePath}>` 404s for locally stored files (`LearningPortalPage.js:198`); upload can store an empty path and "succeed" (`SyllabusController.java:109-122`); downloads discard original filename/content-type.

### Campus
- **Overdue reminders go nowhere** — `LibraryController.java:245` sets `recipient_user_id = BookIssue.studentId` (a `students.id`) against `notifications.recipient_user_id` FK to `users(id)`.
- **Placement status CHECK mismatch** — `V43__Add_Placement_Cell.sql:32` CHECK allows only APPLIED/SHORTLISTED/SELECTED/REJECTED but controller/UI use INTERVIEWING/OFFERED → status update always violates the constraint.
- **Book-request/scholarship/club permission mismatches** (see C1); duplicate applications possible via TOCTOU — no DB unique on `scholarship_applications(scholarship_id, student_id)`.
- **Club member_count drift** — `ClubDAO.java:186,277` double-approve double-increments; leave on non-membership decrements negative.
- **Event capacity never enforced server-side** — `EventDAO.registerStudent` inserts unconditionally; no max_participants/deadline check.
- **Complaint status writes `resolved_date`/`resolved_by` on every transition and accepts arbitrary status strings** (`ComplaintDAO.java:39-54`, controller lacks whitelist).
- **Gate-pass approve/reject lack `WHERE status='PENDING'`**; reject writes `approved_by`/`approved_at` (`GatePassDAO.java:53-92`); controllers cast body numbers with `((Double) map.get(..)).intValue()` → 500 on strings (`GatePassController.java:124,148`).
- **Warden default password `"123"`** is seeded and printed in the UI (`WardenManagementPage.js:135`), no forced change.
- **Visitors** — duplicate active check-ins allowed; exit on a non-IN log returns 200 anyway (`VisitorDAO.java:55-78`, `VisitorController.java:117-123`).
- **Hostel stats drift** — `total_rooms`/`total_capacity` never recomputed with the rooms table → "Rooms"/"Capacity" columns always 0/stale; dashboard "Residents" counts non-ACTIVE allocations and "Available Slots" can go negative.
- **Hostel attendance `hostelId:0`** — blank form sends 0 (`HostelAttendancePage.js:16,53`) vs FK → generic failure; no fallback to allocated hostel.

---

## 3. MEDIUM severity (selected)

- N+1 + swallowed errors in attendance stats and room-availability day-grid.
- `LibraryPage` pagination instantly resets to page 0 — the reset effect's deps include the page-state-bound fetch callbacks (`LibraryPage.js:151-156`) → lists never advance past page 1.
- New-book `available` defaults to 0 (`LibraryPage.js:43`) → created books unissuable; no server-side 0<=available<=quantity validation.
- Fine day-count uses ms division and diverges from the SQL `(CURRENT_DATE - due_date)` formula (`BookIssue.java:159-171` vs `BookIssueDAO.java:272-276`).
- Payment-request approval can silently collect less than approved (no cap warning/flag, `FeeOperationsDAO.java:159-169`); refund+payment leaves `paid_amount > total_amount` (`EnhancedFeeDAO.java:504-527`).
- H2 fallback payment path has no row lock → concurrent double-payment possible (`EnhancedFeeDAO.java:283-370`).
- `FeesPage.today()` uses UTC — receipt/export filenames off-by-one before midnight local.
- Book-request / assignment routes regex `\d+` reject non-numeric usernames/enrollment ids.
- JS date serialization — `JsonHelper` writes `java.util.Date.toString()` ("Sun Jan 01 ... IST 2023"); `new Date(...)` renders "Invalid Date" across Assignment/Attendance/Profile pages, and `isOverdue`/`isDueSoon` badges are always false.
- `Submission` JSON lacks `isGraded`/`submissionDate` that the frontend reads → graded submissions always show "Pending".
- Resource "new row" highlight is dead code (POST returns only a message); dead `/api/resources/search` route.
- `BookRequestDAO`/`Approve` not transactional; fee reminders dedupe only same-calendar-day.
- Faculty username generation can collide after max-row delete (`FacultyController.java:230-246`).
- Deleted course orphans resources (`ON DELETE SET NULL`) while syllabi cascade — asymmetric.
- "Add All" grade roster nulls (see High); `AttendanceDAO` counts LATE as absent by design (verify intended).

---

## 4. UI errors & glitches

### Navigation / routing
- **Sidebar shows links whose routes are ADMIN-only → NotFoundPage**: Faculty, Leave Approvals, Faculty Workload, Audit, Reports, Student Affairs, Settings, Hostel Attendance, Book Requests (`Sidebar.js:18,37,39,56-63` vs `DashboardPage.js:165-199`).
- **GlobalSearch** missing 13 registered features (`faculty-portal`, `student-profile`, `course-registrations`, `hostel/complaints`, `hostel/attendance`, `wardens`, `book-requests`, `feedback`, `roles`, `student-affairs`, `audit`, `activities`, `staff-leave`); results not permission-filtered → students offered payroll/audit links; "Dashboard" quick-access chip never renders (label mismatch).
- **HomePage stat cards navigate students into 404s** (`HomePage.js:28-41`); cards not permission-gated.
- **`/` route always shows LoginPage** even for authenticated users (`App.js:17-26`).
- **Header title wrong on nested routes** — uses `path.split('/').pop()` (`Header.js:28-30`) → "Complaints" instead of "Hostel Complaints" (PageTitle map is correct).
- **Hostel nav double-highlights** on `/dashboard/hostel/*` (missing `end` prop, `Sidebar.js:35`).

### Forms / modals / toasts
- **ProfilePage "save" is a mock** — `editForm` never initialized from profile data, save overwrites real data with blanks, success toast with no API call (`ProfilePage.js:166-169`).
- **Nested modal breaks Escape + scroll lock + duplicate `id="modal-title"`/`aria-labelledby`** (AnnouncementPage AI-assist inside Modal).
- **LibraryPage delete ConfirmDialog never closes after success** (`LibraryPage.js:225-235`); no `loading` guard → double-delete risk; destructive confirm buttons are not red (`.btn-primary.danger` documented in `ConfirmDialog.jsx:32` but never defined in CSS).
- `BookRoomModal`/`CourseManagementPage` form state not reset between opens; stale values carry over.
- Toast/`useToast` silently drops messages outside the provider (`Toast.jsx:5-24`).
- `SearchableSelect` active index can go out of range; not reset on options change.
- `useManagementData` blanks the list when loader identity changes (`ManagementUI.js:7-28`).

### Data display
- **NotificationPage flashes a full skeleton on every 30s poll**; "Mark as unread" never works (`n.read` stays truthy).
- **`.badge-secondary` never defined** in CSS but used in Faculty/BookRequests/Clubs/Role pages → colorless pills.
- Duplicate competing `.badge`, `.modal`, `.modal-backdrop` CSS blocks silently override each other (`main.css` ~728/1119/510/1125).
- Attendance "Subject" column renders the raw numeric course id (a subject-name helper exists but is unused, `AttendancePage.js:22`).
- Faculty "Average Experience" shows hardcoded `'N/A'` as real data.
- Calendar load failure shows the previous month's data with no error/abort; rapid month nav can race.
- Empty states shown after failed reads (no retry); failed requests presented as zero data.

### Hard-coded / demo data passed as real
- Visitor dashboard: "Peak Hour 11:00 AM", "Avg Stay Time 42m", `Check-ins Today = Math.max(totalToday, 8)` (`VisitorPage.js:178-189`).
- Crowdfunding: static donor feed and impact numbers (`CrowdfundingPage.js:221`).
- Gate pass lists render `new Date()` instead of `requestedAt`/`approvedAt` audit timestamps.
- Scholarship/crowdfunding/learning-portal summary stats disconnected from live datasets.

### Misc
- `api.js` 401 handler clears localStorage without `SessionManager.clearSession()` → stale cached identity until reload.
- `AttendancePage` uses `new Date().toISOString()` (UTC) for "today" → off-by-one-day boundaries.
- `.badge-primary` on 'Boys' vs `.badge-danger` on every other hostel type (Girls/Co-ed shown red); complaint badges: everything non-OPEN = green success.

---

## 5. Recommended fix order

1. **Migrations/RBAC**: stop V65 from destroying non-admin roles (recreate STUDENT/FACULTY/FINANCE) before grants; add a reconciliation migration for grants.
2. **Permission alignment**: singular/plural alignment for assignments/placements/gatepass/scholarship/club/crowdfunding/syllabus; grant self-service codes; add `/students/me`.
3. **Event routing + schema**: move EventController catch-all last; create `event_poll_votes`.
4. **Data-loss fixes**: timetable upsert conflict key (+ room), hostel delete guard, vacate/allocate atomicity, library return/approve guards, bulk-assign CSV guard.
5. **CGPA/transcript unification** (single calculator: max marks, credits, terminal exam type).
6. **IDOR**: ownership-scoped student routes (gatepass/complaints/hostel attendance/fee history).
7. **UI**: sidebar/route permission parity, GlobalSearch filter + coverage, pagination effect deps, date serialization (ISO-8601), badge CSS, modal nesting, mock ProfilePage save.

## Audit limitation

Static read-only review. Findings cite code paths but were not reproduced live against a running instance; the earlier live QA audit (`CampusOne-QA-Audit-2026-09-02.md`) independently observed the same admin-403 symptom for many of these modules.