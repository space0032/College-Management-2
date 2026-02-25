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
import AnnouncementPage from './AnnouncementPage';
import NotificationPage from './NotificationPage';
import DepartmentPage from './DepartmentPage';
import ProfilePage from './ProfilePage';
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
            <Route path="announcements" element={<AnnouncementPage />} />
            <Route path="notifications" element={<NotificationPage />} />
            <Route path="departments" element={<DepartmentPage />} />
            <Route path="profile" element={<ProfilePage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

export default DashboardPage;
