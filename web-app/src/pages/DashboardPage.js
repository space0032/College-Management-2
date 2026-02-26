import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Header from '../components/Header';
import HomePage from './HomePage';
import StudentManagementPage from './StudentManagementPage';
import FacultyManagementPage from './FacultyManagementPage';
import CourseManagementPage from './CourseManagementPage';
import AttendancePage from './AttendancePage';
import LibraryPage from './LibraryPage';
import FeesPage from './FeesPage';
import TimetablePage from './TimetablePage';
import PlacementPage from './PlacementPage';
import HostelPage from './HostelPage';
import ClubsPage from './ClubsPage';
import EventsPage from './EventsPage';
import GradesPage from './GradesPage';
import ReportsPage from './ReportsPage';
import GatePassPage from './GatePassPage';
import VisitorPage from './VisitorPage';
import AcademicCalendarPage from './AcademicCalendarPage';
import ScholarshipPage from './ScholarshipPage';
import AssignmentPage from './AssignmentPage';
import CrowdfundingPage from './CrowdfundingPage';
import InstituteManagementPage from './InstituteManagementPage';
import ResourceManagementPage from './ResourceManagementPage';
import EmployeeManagementPage from './EmployeeManagementPage';
import LeaveApprovalPage from './LeaveApprovalPage';
import RoomAvailabilityPage from './RoomAvailabilityPage';
import FacultyWorkloadPage from './FacultyWorkloadPage';
import StudentActivitiesPage from './StudentActivitiesPage';
import AnnouncementPage from './AnnouncementPage';
import NotificationPage from './NotificationPage';
import PayrollManagementPage from './PayrollManagementPage';
import SyllabusManagementPage from './SyllabusManagementPage';
import LearningPortalPage from './LearningPortalPage';
import VolunteerTasksPage from './VolunteerTasksPage';
import StudentProfilePage from './StudentProfilePage';
import AuditLogPage from './AuditLogPage';
import DepartmentPage from './DepartmentPage'; // Keep for now, as the instruction implies replacement, but the snippet adds a new route for InstituteManagementPage and keeps DepartmentPage.
import ProfilePage from './ProfilePage';
import StaffLeavePage from './StaffLeavePage';
// Keep for now, as the instruction implies replacement, but the snippet adds a new route for InstituteManagementPage and keeps ProfilePage.
import ChangePasswordPage from './ChangePasswordPage';
import StudentAffairsPage from './StudentAffairsPage';
import NotFoundPage from './NotFoundPage';

const DashboardPage = () => {
  return (
    <div className="app-layout">
      <Sidebar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        <Header />
        <main className="main-content">
          <Routes>
            <Route index element={<HomePage />} />
            <Route path="students" element={<StudentManagementPage />} />
            <Route path="faculty" element={<FacultyManagementPage />} />
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
            <Route path="reports" element={<ReportsPage />} />
            <Route path="gatepass" element={<GatePassPage />} />
            <Route path="visitors" element={<VisitorPage />} />
            <Route path="calendar" element={<AcademicCalendarPage />} />
            <Route path="scholarships" element={<ScholarshipPage />} />
            <Route path="assignments" element={<AssignmentPage />} />
            <Route path="crowdfunding" element={<CrowdfundingPage />} />
            <Route path="announcements" element={<AnnouncementPage />} />
            <Route path="notifications" element={<NotificationPage />} />
            <Route path="departments" element={<DepartmentPage />} />
            <Route path="profile" element={<ProfilePage />} />
            <Route path="management" element={<InstituteManagementPage />} />
            <Route path="employees" element={<EmployeeManagementPage />} />
            <Route path="leaves" element={<LeaveApprovalPage />} />
            <Route path="rooms" element={<RoomAvailabilityPage />} />
            <Route path="workload" element={<FacultyWorkloadPage />} />
            <Route path="payroll" element={<PayrollManagementPage />} />
            <Route path="syllabus" element={<SyllabusManagementPage />} />
            <Route path="learning" element={<LearningPortalPage />} />
            <Route path="volunteer" element={<VolunteerTasksPage />} />
            <Route path="student-profile" element={<StudentProfilePage />} />
            <Route path="audit" element={<AuditLogPage />} />
            <Route path="activities" element={<StudentActivitiesPage />} />
            <Route path="resources" element={<ResourceManagementPage />} />
            <Route path="staff-leave" element={<StaffLeavePage />} />
            <Route path="student-affairs" element={<StudentAffairsPage />} />
            <Route path="change-password" element={<ChangePasswordPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

export default DashboardPage;
