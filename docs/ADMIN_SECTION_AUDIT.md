# Admin Section Deep-Scan Audit

Date: 2026-09-23

This document catalogues the findings of a deep scan of the admin section across all three tiers:

- **Web app (React)** — `web-app/src/pages`, `web-app/src/components`, `web-app/src/services`
- **Backend (Java HTTP server)** — `src/main/java/com/college/api`, `dao`, `utils`
- **Desktop (JavaFX)** — `src/main/java/com/college/fx/views`

Every finding was cross-verified against the backend controller/DAO code and SQL migrations. Severity legend: **Critical** (exploitable / data loss / totally broken feature), **High** (reliable crash or broken core workflow), **Medium** (silent failure / stale data / wrong gate / misleading UX), **Low** (crash-on-edge / dead code / noise).

---

## Web App (React Admin Pages)

### Critical

| ID | Issue | Location |
|----|-------|----------|
| W-C1 | Student Affairs module hits `/affairs/*` endpoints that are **not registered** on the backend. `ApiServer.java` has no `/api/affairs` context, so requests resolve (HTTP 200) against `RootHandler`, returning the "API is running" banner object. `res.data` becomes a plain object, then `incidents.filter(...)` throws `TypeError` and the Disciplinary / Grievance / Parent-Comm tabs crash on load. The `.catch()` sample-data fallbacks never fire because the request never fails. | `services/studentAffairsService.js:4-16`, `pages/StudentAffairsPage.js:40,70,96-98,211,248,377,461` |

### High

| ID | Issue | Location |
|----|-------|----------|
| W-H1 | Creating a student succeeds but **credentials are permanently lost** if room allocation fails afterwards (`SHOW_CREDENTIALS` is only dispatched after `allocateRoom` resolves). Backend never returns the password again. | `StudentManagementPage.js:353-375` |
| W-H2 | Editing any student **silently wipes their `course`** — the form has no Course field, the payload always sends `course:''`, and `StudentDAO.updateStudentChecked` writes it unconditionally. | `StudentManagementPage.js:347`, `StudentDAO.java:162` |
| W-H3 | Edit can partially succeed then report failure: `updateStudent` commits, then `syncAllocation` throws → record already changed but UI shows "Failed to save". Room change does `vacateRoom` before `allocateRoom` with no rollback — student can be left with no allocation. | `StudentManagementPage.js:349-352,294-327` |
| W-H4 | Faculty search effect depends on `[debouncedSearch, search, ...]` — fires on **every keystroke** with the stale debounced value, defeats the 300 ms debounce, spams the server, and races responses out of order. | `FacultyManagementPage.js:90-106` |
| W-H5 | Settings load failure is silently swallowed; a subsequent Save **overwrites stored settings** (incl. the Dropbox API key) with defaults, because the backend upserts every submitted key. | `SettingsPage.js:23-61`, `SettingsController.java:39-63` |

### Medium

| ID | Issue | Location |
|----|-------|----------|
| W-M1 | Audit date filter only applies when **both** from and to are set; one-sided filter silently returns the last 200 unfiltered records. Username/action search is client-side over only 200 rows. | `AuditLogPage.js:28,40-54`, `AuditController.java:56-64` |
| W-M2 | Audit page has no request serialization/abort → filter/reset and double-click races can show stale results. Filter button has no disabled/loading state. | `AuditLogPage.js:24-35,104` |
| W-M3 | Reports: attendance CSV exports non-existent `presentCount`/`absentCount` columns; placement metric labels **application count** as "Offers"; placement tab shows an **infinite spinner** on any error; grade distribution double-counts students. | `ReportsPage.js:150-154,352,47-50,79-80` |
| W-M4 | FacultyWorkload: no permission checks; races in faculty/assign selection; `?size=1000` hard-coded pagination drops courses; 409 "already assigned" is shown as a fake time-conflict; no busy states on assign/unassign. | `FacultyWorkloadPage.js:1-434,46-141` |
| W-M5 | Faculty import modal **never shows results** — closes before the result panel renders and drops error details. | `FacultyImportModal.js:56-76`, `FacultyManagementPage.js:389-396` |
| W-M6 | Leave approvals: role-string check instead of permissions; "Pending Reviews" filters `'APPLIED'` which the backend never emits (`'PENDING'`); "Used This Year" counts records, not days, and ignores the year; loading/error states never rendered; no double-submit protection; student leave path lacks date validation; modal has no a11y. | `LeaveApprovalPage.js:13-42,85-98,147-194` |
| W-M7 | Employee edit coerces empty salary to `'0'`, destroying stored salary data. | `EmployeeManagementPage.js:31` |
| W-M8 | `ConfirmDialog` drops its `destructive` prop → destructive confirmations render as normal primary buttons. | `ConfirmDialog.jsx:15-22` |
| W-M9 | Institute/Role pages: stale `formError` masks real server errors; role `isDirty` ignores the Portal field; role creation can crash on a stale `role` object; self-demotion allowed with no warning. | `InstituteManagementPage.js:23-25,49`, `RoleManagementPage.js:48-49,72` |
| W-M10 | CSV export lacks formula-injection protection (`=`, `+`, `-`, `@` cells). | `exportUtils.js:7-33` |
| W-M11 | Settings page gates on literal role `ADMIN` while sidebar/search gate on `VIEW_SETTINGS`; custom roles granted the permission are blocked in-UI. `accent_color` saved but never returned by GET → theme control always resets. | `SettingsPage.js:17,63-72`, `SettingsController.java:39-41` |
| W-M12 | Leave "Used This Year" / stats hardcode and miscompute; `staffId` sent but ignored by backend; approve/reject has no confirmation. | `LeaveApprovalPage.js:50,85,90,98,147-148` |

### Low

| ID | Issue | Location |
|----|-------|----------|
| W-L1 | Exports only cover currently-fetched pagination rows. | `StudentManagementPage.js:399-405`, `FacultyManagementPage.js:226-232` |
| W-L2 | "Generate" password uses `Math.random().toString(36)` (8 chars, predictable). | `StudentManagementPage.js:780` |
| W-L3 | Faculty import advertises `.xlsx` but only CSV text is parsed. | `FacultyImportModal.js:152`, `FacultyController.java:440-453` |
| W-L4 | Settings theme/logo/accent controls persist but have zero effect. | `SettingsPage.js:52-55,115-153`, `SettingsController.java:39-41` |
| W-L5 | Employee status `<select>` label not associated (a11y). | `EmployeeManagementPage.js:46` |
| W-L6 | One-frame stale table data after tab switch in Institute page. | `InstituteManagementPage.js:20`, `ManagementUI.js:30-34` |

---

## Backend (Admin Controllers / DAOs)

### Critical

| ID | Issue | Location |
|----|-------|----------|
| B-C1 | **Leave IDOR**: `GET /api/leaves/staff?userId=`, `/api/leaves/student?studentId=`, and `/api/leaves/pending` are gated only on `VIEW_LEAVE`, which V73 grants to **STUDENT**. Any student can read arbitrary employees'/students' leave history and the full pending list. Bypasses the `scopeStudentAccess` pattern used by 15+ other controllers. | `LeaveController.java:56-78,80-90`, migration `V73__Restore_Roles_And_Grants.sql:76,58,117` |
| B-C2 | **Broken student-leave approvals**: `student_leaves.student_id` references `users(id)` but `StudentLeaveDAO.getPendingLeaves` joins `students s ON sl.student_id = s.id` — a different table/sequence. API-created student leave requests never appear to approvers. | `V34__Add_Student_Leaves.sql:3`, `StudentLeaveDAO.java:51-54` |
| B-C3 | **Leave status mutation**: no self-approval guard, no status whitelist (arbitrary strings silently remove rows from the pending queue), `approvedBy` spoofable from the request body. | `LeaveController.java:151-203`, `StaffLeaveDAO.java:81-96`, `StudentLeaveDAO.java:72-86` |

### High

| ID | Issue | Location |
|----|-------|----------|
| B-H1 | Student-leave creation: `((Double) map.get("studentId")).intValue()` throws CCE on a string; NPE on missing fields; no date-range validation; no self-scope — anyone with `CREATE_LEAVE` can file leaves for another user. | `LeaveController.java:133-148` |
| B-H2 | `/api/dashboard/stats` gated only on auth — any authenticated user (incl. students) gets institution-wide counts. | `DashboardController.java:41-55` |
| B-H3 | Workload endpoints readable by any `VIEW_WORKLOAD` holder (faculty can enumerate all workloads + timetables); `uniqueSubjects` counts class-count instead of distinct subjects. | `WorkloadController.java:54-113,66-67` |
| B-H4 | Settings: `dropbox_api_key` returned in cleartext; PUT accepts arbitrary keys with no whitelist; malformed values → CCE → 500. | `SettingsController.java:40,49,58` |
| B-H5 | Faculty accounts default password `"123"`, returned in plaintext (also in CSV import). | `FacultyController.java:150,175,489` |
| B-H6 | Password change does not invalidate existing sessions (tokens live up to 24 h). | `UserController.java:102-107`, `TokenStore.java:15` |
| B-H7 | No login rate limiting / account lockout. | `AuthController.java:43-85` |
| B-H8 | Reports PDF: `MANAGE_REPORT` granted to no non-admin role (dead feature for faculty); temp PDF never deleted; absolute temp path leaked in response. | `ReportController.java:50,65-72` |
| B-H9 | Error responses leak raw `e.getMessage()` and can emit malformed JSON (unescaped quotes). | `AuditController.java:40`, `LeaveController.java:52`, `WorkloadController.java:50`, etc. |

### Medium

| ID | Issue | Location |
|----|-------|----------|
| B-M1 | `JsonHelper` parser splits on the first `:` and strips `"` → corrupts URLs/times/quoted values; escaper misses backslashes/control chars. Used by Department/Specialization/Role/Faculty controllers. | `JsonHelper.java:91-156,77-79` |
| B-M2 | Faculty pagination: `size` defaults to `Integer.MAX_VALUE` → `page=N` alone produces a huge offset. | `FacultyController.java:105-110` |
| B-M3 | Audit query params (`userId`, `from`, bad `limit`) return 500 instead of 400; raw exception text leaked. | `AuditController.java:40,54-64` |
| B-M4 | `RoleDAO.assignPermissionToRole` uses MySQL-only `INSERT IGNORE` (PostgreSQL rejects). | `RoleDAO.java:182` |
| B-M5 | Single-pass SHA-256 password hashing (fast offline cracking). *Deferred: legacy hash compatibility would require a migration strategy.* | `PasswordUtils.java:23-38` |
| B-M6 | `handleUpdateMe` CCE on numeric phone. | `FacultyController.java:363` |
| B-M7 | One-sided audit date filter silently ignored by server. | `AuditController.java:56-64` |
| B-M8 | `recordSuccessfulMutation` in `ProtectedHandler` can mis-record / is redundant. | `ApiServer.java:163-193` |
| B-M9 | Every permission check issues multiple fresh DB queries per request. | `PermissionService.java:29-32`, `BaseController.java:72` |
| B-M10 | Fallback DB credentials `postgres/password` hardcoded. | `DatabaseConnection.java:52-54` |

### Low

| ID | Issue | Location |
|----|-------|----------|
| B-L1 | `ApiAuthMiddleware` dead code. | `api/ApiAuthMiddleware.java` |
| B-L2 | `AuditLogDAO.getRecentLogs` accepts unbounded `limit`. | `AuditLogDAO.java:150` |
| B-L3 | EmployeeController silently discards designation changes for user-linked employees. | `EmployeeController.java:37` |

---

## Desktop (JavaFX Admin Views)

### Critical

| ID | Issue | Location |
|----|-------|----------|
| J-C1 | `navigateTo()` performs **no permission checks** — permission checks only hide sidebar buttons. Any user (incl. students) who reaches a view directly (or via the Home search) bypasses RBAC. | `DashboardView.java:521-664`, `HomeView.java:721-765` |
| J-C2 | Students can approve/reject leave requests (no gate in `getView()`, only `isLoggedIn()` in the action handler). | `LeaveApprovalView.java:21-49,82-92` |
| J-C3 | Any user can rename the institution (no permission check in `saveSettings`). | `CollegeSettingsView.java:195-216` |
| J-C4 | Full student PII exposure + bulk export; View Profile and Export buttons not permission-gated. | `StudentManagementView.java:205-231,539-587` |
| J-C5 | Payroll view crashes at construction from `Collectors.toMap` duplicate key `0` (synthetic employees from `EmployeeDAO`). "Generate Payroll" always throws for the same population. | `PayrollManagementView.java:62-65`, `EmployeeDAO.java:119-142`, `PayrollDAO.java:50-52` |

### High

| ID | Issue | Location |
|----|-------|----------|
| J-H1 | CSV import uses weak default password `"123"` for every account; also bypasses `role_id` → RBAC grants ignored. | `utils/CSVImporter.java:92` |
| J-H2 | Faculty add creates an orphaned user account and shows false success when the faculty insert fails. | `FacultyManagementView.java:494-516` |
| J-H3 | "Assign Role" writes the role code then overwrites `users.role` with the display name, breaking legacy role fallbacks. | `FacultyManagementView.java:349-361`, `UserDAO.java:67-78` |
| J-H4 | Faculty default password `"123"` in the mainline flow. | `FacultyManagementView.java:452-453,490` |

### Medium

| ID | Issue | Location |
|----|-------|----------|
| J-M1 | EmployeeManagement: uncaught `ManagementException`; **NPE in search filter** on null fields. | `EmployeeManagementView.java:216-217,319-326,413-421` |
| J-M2 | Student/Faculty edit dialogs mutate the in-memory object then silently discard on failed DB write. | `StudentManagementView.java:493-511`, `FacultyManagementView.java:278-296` |
| J-M3 | Validation errors occur after the dialog closes — user input destroyed. | `StudentManagementView.java:360-446`, `FacultyManagementView.java:465-536` |
| J-M4 | Duplicate/redundant success alerts on student enrollment. | `StudentManagementView.java:425-453` |
| J-M5 | Leave approval is a one-click destructive action with no confirmation. | `LeaveApprovalView.java:82-92,140-151` |
| J-M6 | Leave staff tab gated on legacy `isAdmin()` instead of RBAC. | `LeaveApprovalView.java:43-45` |
| J-M7 | Institute tab leaks Student/Faculty lists to any `MANAGE_SYSTEM` role; Special-users delete bypasses `AccessManagementDAO` (loses last-admin protection); role/department deletes fail with no feedback. | `InstituteManagementView.java:116-122,1047-1060,805-813` |
| J-M8 | System-role save rejected after in-memory object already mutated → ghost data. | `InstituteManagementView.java:519-537` |
| J-M9 | Home quick-action "Add Student Fee" rebuilds the whole dashboard and never performs the action. | `HomeView.java:296-310` |
| J-M10 | Students shown fabricated attendance `"85"` and fee `"Paid"`. | `HomeView.java:684-692` |
| J-M11 | Announcements panel breaks entirely on one null timestamp. | `HomeView.java:585` |
| J-M12 | Dashboard navigation dumps the screen on any load error. | `DashboardView.java:655-663` |

### Low

| ID | Issue | Location |
|----|-------|----------|
| J-L1 | Payroll update result ignored → silent failure. | `PayrollManagementView.java:227-231`, `PayrollDAO.java:113` |
| J-L2 | `alert.showAndWait().get()` throws `NoSuchElementException` on ESC/close. | `InstituteManagementView.java:301,805,1053` |
| J-L3 | Dead background thread in CollegeSettingsView; logo upload not implemented. | `CollegeSettingsView.java:184-209` |
| J-L4 | HomeView avatar NPE/SIOOBE on empty username. | `HomeView.java:411` |
| J-L5 | `EnrollmentGenerator` concatenates SQL `LIKE` prefix; `currentTimeMillis()%1000` collision-prone. | `EnrollmentGenerator.java:23-24,42,56,76` |
| J-L6 | CSV importer leaves orphaned user account on failed insert. | `CSVImporter.java:92-98` |
| J-L7 | ReportsView generates PDF/Excel on the FX thread (UI freeze); `showSaveDialog(null)`. | `ReportsView.java:158-165` |
| J-L8 | `StudentAffairsView` is dead — no menu item or search keyword maps to it. | `StudentAffairsView.java`, `DashboardView.java:600-602` |
| J-L9 | Console noise (`System.out`/`printStackTrace`) left in production paths. | multiple |

---

## Fix Log

Fixes applied during the remediation pass are tracked here as they are completed.

| # | Finding | Fix | Status |
|---|---------|-----|--------|
| 1 | W-C1 / B-C1-affairs | Add `/api/affairs` backend (controller, DAOs, migration) and register context | Done |
| 2 | B-C1/B-C2/B-C3/B-H1 | LeaveController + DAO security/integrity fixes | Done |
| 3 | W-H5 / B-H4 / W-M11 | Settings round-trip + whitelist + masking + guarded save | Done |
| 4 | B-H2/B-H3 | Dashboard + Workload gating | Done |
| 5 | B-H5/J-H1/J-H4 | Strong default passwords (random, not returned) | Done |
| 6 | B-H6/B-H7 | Token invalidation on password change + login rate limit | Done |
| 7 | B-H8 | Report controller temp-file cleanup + no path leak | Done |
| 8 | B-H9/B-M3/B-M1 | Safe error JSON + JsonHelper parser/escaper | Done |
| 9 | B-M2/B-M4 | Pagination default + PG-compatible role grant | Done |
| 10 | W-H1/W-H2/W-H3 | StudentManagementPage create/edit fixes | Done |
| 11 | W-H4/W-M5 | Faculty page search race + import results display | Done |
| 12 | W-M1/W-M2/M7 | Audit page serialization + server search + one-sided date filter | Done |
| 13 | W-M3 | Reports page CSV/metrics/spinner fixes | Done |
| 14 | W-M4 | FacultyWorkload permission + race + size + 409 fixes | Done |
| 15 | W-M6/W-M12 | LeaveApproval page fixes | Done |
| 16 | W-M7/W-M8/W-M9/W-M10 | Employee/ConfirmDialog/Institute/Role/exportUtils | Done |
| 17 | J-C1..J-C5 | JavaFX RBAC + payroll + student/faculty/college settings | Done |
| 18 | J-H1..J-M12 | Remaining JavaFX fixes (orphan rollback, RBAC, strong password, validation, dialogs, dashboard) | Done |