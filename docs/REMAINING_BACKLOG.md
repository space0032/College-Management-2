# Remaining Fix Backlog

**Source:** `docs/Academic-Campus-Deep-Audit-2026-09-22.md` (2026-09-22). Tracking items not covered by commit `3351d9d`.

Status legend: [ ] open · [x] fixed · [~] partial

## Critical / High

- [x] **C1 — Permission alignment** (assignments, gatepass, placement, scholarship, club, crowdfunding) — done in 3351d9d
- [x] Syllabus students blocked: grant `VIEW_SYLLABUS` + `/students/me` self-profile route
- [x] **C2 — RBAC role restore** (V73) — done
- [x] **C3 — Event budget/poll routing + `event_poll_votes`** (V74) — done
- [x] **C4 — Timetable slot-conflict guard** — done
- [x] **C5 — Course drop endpoint unreachable** — `CourseRegistrationController.java:47` regex widened to `.*/course-registrations/drop` (client uses query params)
- [x] **C6 — Library issued list / return / approve guards** — done
- [x] **C7 — Fees bulk-assign / ledger / enum** — done
- [x] **C8 — Hostel data-loss (delete/vacate/allocate/attendance)** — done
- [x] **C9 — IDOR scope (gatepass/complaints/hostel attendance/fee history)** — done
- [x] CGPA/transcript unification — done
- [x] **FACULTY corrupts faculty row while "editing a student"** — `StudentProfilePage.js` edit always routes `PUT /students/{id}`; backend `UPDATE_STUDENT` gates it
- [x] **Grades roster rows lack department/semester/specialization** — `CourseRegistrationDAO.getEnrolledStudentsWithDetails` populates them
- [x] **Workload conflict-check NPE** — `WorkloadController.java:140-141` null-guards on day/time
- [x] **Course-registration integrity** — flip-back-to-PENDING instead of dup INSERT; `status='PENDING'` guards on approve/reject; `GREATEST` enrolled_count math; V76 unique index
- [x] **Syllabus portal download 404 / empty path / filename loss** — `SyllabusController` add requires a file, download serves Content-Type by extension + title-based `Content-Disposition`; `LearningPortalPage.js:198` + `SyllabusManagementPage` use authenticated blob download with header filename
- [x] **Calendar merged-ID collision + working-day math + malformed ICS/CSV** — deterministic negative ids for Google holidays (edit/delete hidden), dedup weekday holidays+exams, proper CSV/ICS escaping + correct all-day `DTEND`
- [x] **Placement status CHECK mismatch** — `V43` vs INTERVIEWING/OFFERED (V77 widens CHECK; H2 fallback caveat)
- [x] **Library overdue reminders wrong FK** — `recipient_user_id` resolved to `users.id` via `StudentDAO.getUserIdByStudentId`
- [x] **Club member_count drift** — `ClubDAO.java:186,277` PENDING-only approve + guarded decrements
- [x] **Event capacity not enforced server-side** — `EventDAO.registerStudent` transactional, `SELECT ... FOR UPDATE` + cap check
- [x] **Complaint status no whitelist** — `ComplaintController.java` validates OPEN/IN_PROGRESS/RESOLVED/REJECTED
- [x] **Gate-pass approve/reject no PENDING guard + `Double` casts** — `GatePassDAO.java` + `GatePassController` `Number` casts
- [x] **Warden default password `"123"` seeded/printed** — random 12-char generated once, shown on creation only
- [x] **Visitors: duplicate check-in + exit on non-IN** — `VisitorDAO.hasActiveLog` + guarded `logExit`
- [x] **Hostel stats drift + hostel attendance `hostelId:0`** — computed stats from rooms table; auto-resolve hostelId from active allocation
- [ ] **`FeeTransactionController.handleRecord` ownership scope** (left as-is in 3351d9d)

## Medium

- [ ] New-book `available` defaults to 0 + no `0<=available<=quantity` server validation
- [ ] Fine day-count ms math vs SQL formula
- [ ] Payment-request approval cap; refund > total; H2 no row lock
- [ ] `FeesPage.today()`/`AttendancePage` UTC off-by-one
- [ ] `Submission` JSON lacks `isGraded`/`submissionDate`
- [ ] Resource new-row dead code + dead `/api/resources/search`; faculty-username collision; deleted-course resource orphans
- [ ] TOCTOU duplicate applications (scholarship unique)
- [ ] Attendance stats N+1 / swallowed errors

## UI

- [x] Sidebar/route permission parity — done
- [x] GlobalSearch coverage + permission filter — done
- [x] HomePage stat-card gating + `/` redirect — done
- [x] LibraryPage pagination deps + delete dialog — done
- [x] ProfilePage mock save (real endpoint) — done
- [x] Modal nesting (Announcement/Assignment AI) — done
- [x] badge CSS + hostel/complaint badge semantics — done
- [x] JsonHelper ISO-8601 dates — done
- [x] NotificationPage skeleton flash + mark-as-unread
- [x] Attendance "Subject" column raw numeric id
- [x] Faculty "Average Experience" hardcoded `'N/A'`
- [x] Calendar error/race + empty-state-with-no-retry patterns
- [x] Hardcoded demo data (VisitorPage, CrowdfundingPage, gate-pass timestamps)
- [x] `Header.js` nested-route title
- [x] `api.js` 401 → `SessionManager.clearSession()`
- [x] `BookRoomModal`/`CourseManagementPage` form reset
- [x] Duplicate `.badge`/`.modal`/`.modal-backdrop` CSS blocks
- [x] `Toast`/`SearchableSelect`/`useManagementData` robustness