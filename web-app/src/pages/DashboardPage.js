import React, { lazy, Suspense, useState, useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import SessionManager from '../utils/SessionManager';

const pages = {
  HomePage: lazy(() => import('./HomePage')),
  StudentManagementPage: lazy(() => import('./StudentManagementPage')),
  FacultyManagementPage: lazy(() => import('./FacultyManagementPage')),
  CourseManagementPage: lazy(() => import('./CourseManagementPage')),
  AttendancePage: lazy(() => import('./AttendancePage')),
  LibraryPage: lazy(() => import('./LibraryPage')),
  FeesPage: lazy(() => import('./FeesPage')),
  TimetablePage: lazy(() => import('./TimetablePage')),
  PlacementPage: lazy(() => import('./PlacementPage')),
  HostelPage: lazy(() => import('./HostelPage')),
  ClubsPage: lazy(() => import('./ClubsPage')),
  EventsPage: lazy(() => import('./EventsPage')),
  GradesPage: lazy(() => import('./GradesPage')),
  ReportsPage: lazy(() => import('./ReportsPage')),
  GatePassPage: lazy(() => import('./GatePassPage')),
  VisitorPage: lazy(() => import('./VisitorPage')),
  AcademicCalendarPage: lazy(() => import('./AcademicCalendarPage')),
  ScholarshipPage: lazy(() => import('./ScholarshipPage')),
  AssignmentPage: lazy(() => import('./AssignmentPage')),
  CrowdfundingPage: lazy(() => import('./CrowdfundingPage')),
  InstituteManagementPage: lazy(() => import('./InstituteManagementPage')),
  ResourceManagementPage: lazy(() => import('./ResourceManagementPage')),
  EmployeeManagementPage: lazy(() => import('./EmployeeManagementPage')),
  LeaveApprovalPage: lazy(() => import('./LeaveApprovalPage')),
  RoomAvailabilityPage: lazy(() => import('./RoomAvailabilityPage')),
  FacultyWorkloadPage: lazy(() => import('./FacultyWorkloadPage')),
  StudentActivitiesPage: lazy(() => import('./StudentActivitiesPage')),
  AnnouncementPage: lazy(() => import('./AnnouncementPage')),
  NotificationPage: lazy(() => import('./NotificationPage')),
  PayrollManagementPage: lazy(() => import('./PayrollManagementPage')),
  SyllabusManagementPage: lazy(() => import('./SyllabusManagementPage')),
  LearningPortalPage: lazy(() => import('./LearningPortalPage')),
  VolunteerTasksPage: lazy(() => import('./VolunteerTasksPage')),
  StudentProfilePage: lazy(() => import('./StudentProfilePage')),
  AuditLogPage: lazy(() => import('./AuditLogPage')),
  DepartmentPage: lazy(() => import('./DepartmentPage')),
  SpecializationManagementPage: lazy(() => import('./SpecializationManagementPage')),
  ProfilePage: lazy(() => import('./ProfilePage')),
  StaffLeavePage: lazy(() => import('./StaffLeavePage')),
  ChangePasswordPage: lazy(() => import('./ChangePasswordPage')),
  SettingsPage: lazy(() => import('./SettingsPage')),
  StudentAffairsPage: lazy(() => import('./StudentAffairsPage')),
  RoleManagementPage: lazy(() => import('./RoleManagementPage')),
  HostelComplaintsPage: lazy(() => import('./HostelComplaintsPage')),
  HostelAttendancePage: lazy(() => import('./HostelAttendancePage')),
  WardenManagementPage: lazy(() => import('./WardenManagementPage')),
  CourseRegistrationsPage: lazy(() => import('./CourseRegistrationsPage')),
  FeedbackPage: lazy(() => import('./FeedbackPage')),
  BookRequestsPage: lazy(() => import('./BookRequestsPage')),
  FacultyPortalPage: lazy(() => import('./FacultyPortalPage')),
  NotFoundPage: lazy(() => import('./NotFoundPage')),
};

const {
  HomePage, StudentManagementPage, FacultyManagementPage, CourseManagementPage,
  AttendancePage, LibraryPage, FeesPage, TimetablePage, PlacementPage, HostelPage,
  ClubsPage, EventsPage, GradesPage, ReportsPage, GatePassPage, VisitorPage,
  AcademicCalendarPage, ScholarshipPage, AssignmentPage, CrowdfundingPage,
  InstituteManagementPage, ResourceManagementPage, EmployeeManagementPage,
  LeaveApprovalPage, RoomAvailabilityPage, FacultyWorkloadPage, StudentActivitiesPage,
  AnnouncementPage, NotificationPage, PayrollManagementPage, SyllabusManagementPage,
  LearningPortalPage, VolunteerTasksPage, StudentProfilePage, AuditLogPage,
  DepartmentPage, SpecializationManagementPage, ProfilePage, StaffLeavePage, ChangePasswordPage, SettingsPage,
  StudentAffairsPage, RoleManagementPage, NotFoundPage,
  HostelComplaintsPage, HostelAttendancePage, WardenManagementPage,
  CourseRegistrationsPage, FeedbackPage, BookRequestsPage,
  FacultyPortalPage,
} = pages;

const DashboardPage = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const userRole = SessionManager.getUserRole() || 'STUDENT';
  const isAdmin = userRole === 'ADMIN';
  const isFaculty = userRole === 'FACULTY';
  const can = (perm) => SessionManager.hasPermission(perm);

  // Pull fresh permissions on every dashboard mount so permission-tree
  // changes apply without forcing the user to log out and back in.
  const [, forceUpdate] = useState(0);
  useEffect(() => {
    let cancelled = false;
    SessionManager.refreshPermissions().then((perms) => {
      if (!cancelled && perms) forceUpdate((n) => n + 1);
    });
    return () => { cancelled = true; };
  }, []);

  return (
    <div className="app-layout">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      {sidebarOpen && <button className="sidebar-scrim" aria-label="Close navigation" onClick={() => setSidebarOpen(false)} />}
      <div className="app-main">
        <Header onMenuClick={() => setSidebarOpen(true)} />
        <main className="main-content">
          <Suspense fallback={<div className="loading-state">Loading page…</div>}>
          <Routes>
            <Route index element={<HomePage />} />

            {/* Common / Shared Routes (All Roles) */}
            <Route path="courses" element={<CourseManagementPage />} />
            <Route path="attendance" element={<AttendancePage />} />
            <Route path="library" element={<LibraryPage />} />
            <Route path="fees" element={<FeesPage />} />
            <Route path="timetable" element={<TimetablePage />} />
            <Route path="placements" element={<PlacementPage />} />
            <Route path="hostel" element={<HostelPage />} />
            <Route path="clubs" element={<ClubsPage />} />
            <Route path="events" element={<EventsPage />} />
            <Route path="grades" element={<GradesPage />} />
            <Route path="gatepass" element={<GatePassPage />} />
            <Route path="visitors" element={<VisitorPage />} />
            <Route path="calendar" element={<AcademicCalendarPage />} />
            <Route path="scholarships" element={<ScholarshipPage />} />
            <Route path="assignments" element={<AssignmentPage />} />
            <Route path="crowdfunding" element={<CrowdfundingPage />} />
            <Route path="announcements" element={<AnnouncementPage />} />
            <Route path="notifications" element={<NotificationPage />} />
            <Route path="departments" element={<DepartmentPage />} />
            <Route path="specializations" element={<SpecializationManagementPage />} />
            <Route path="profile" element={<ProfilePage />} />
            <Route path="rooms" element={<RoomAvailabilityPage />} />
            <Route path="syllabus" element={<SyllabusManagementPage />} />
            <Route path="learning" element={<LearningPortalPage />} />
            <Route path="volunteer" element={<VolunteerTasksPage />} />
            <Route path="activities" element={<StudentActivitiesPage />} />
            <Route path="resources" element={<ResourceManagementPage />} />
            <Route path="change-password" element={<ChangePasswordPage />} />

            {/* Admin + Faculty Routes */}
            {(can('VIEW_STUDENT') || isAdmin || isFaculty) && (
              <>
                <Route path="students" element={<StudentManagementPage />} />
              </>
            )}
            {can('VIEW_STUDENT_PROFILE') && (
              <Route path="student-profile" element={<StudentProfilePage />} />
            )}
            {(can('VIEW_LEAVE') || isAdmin || isFaculty) && (
              <Route path="staff-leave" element={<StaffLeavePage />} />
            )}
            {can('VIEW_FACULTY_PORTAL') && (
              <Route path="faculty-portal" element={<FacultyPortalPage />} />
            )}

            {/* Admin Only Routes */}
            {isAdmin && (
              <>
                <Route path="faculty" element={<FacultyManagementPage />} />
                <Route path="management" element={<InstituteManagementPage />} />
                <Route path="employees" element={<EmployeeManagementPage />} />
                <Route path="leaves" element={<LeaveApprovalPage />} />
                <Route path="workload" element={<FacultyWorkloadPage />} />
                <Route path="payroll" element={<PayrollManagementPage />} />
                <Route path="audit" element={<AuditLogPage />} />
                <Route path="reports" element={<ReportsPage />} />
                <Route path="student-affairs" element={<StudentAffairsPage />} />
                <Route path="settings" element={<SettingsPage />} />
                <Route path="roles" element={<RoleManagementPage />} />
              </>
            )}

            {/* New feature parity routes */}
            {can('VIEW_HOSTEL') && (
              <>
                <Route path="hostel/attendance" element={<HostelAttendancePage />} />
                <Route path="wardens" element={<WardenManagementPage />} />
              </>
            )}
            {can('VIEW_COMPLAINT') && (
              <Route path="hostel/complaints" element={<HostelComplaintsPage />} />
            )}
            {can('VIEW_COURSE') && (
              <Route path="course-registrations" element={<CourseRegistrationsPage />} />
            )}
            {(can('VIEW_FACULTY') || isAdmin) && (
              <>
                <Route path="feedback" element={<FeedbackPage />} />
                <Route path="book-requests" element={<BookRequestsPage />} />
              </>
            )}

            <Route path="*" element={<NotFoundPage />} />
          </Routes>
          </Suspense>
        </main>
      </div>
    </div>
  );
};

export default DashboardPage;
