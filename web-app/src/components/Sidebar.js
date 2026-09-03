import React from 'react';
import { NavLink } from 'react-router-dom';
import SessionManager from '../utils/SessionManager';

const Sidebar = ({ isOpen = false, onClose }) => {
  const can = (perm) => SessionManager.hasPermission(perm);
  const isAdmin = SessionManager.hasRole('ADMIN');

  const navSections = {
    Overview: [
      { to: '/dashboard', label: 'Home', icon: '🏠', end: true },
      { to: '/dashboard/profile', label: 'My Profile', icon: '👤' },
    ],
    Academic: [
      { to: '/dashboard/students', label: 'Students', icon: '🎓', perm: 'VIEW_STUDENT' },
      { to: '/dashboard/student-profile', label: 'Student Profile', icon: '📋', perm: 'VIEW_STUDENT' },
      { to: '/dashboard/faculty', label: 'Faculty', icon: '👩‍🏫', perm: 'VIEW_FACULTY' },
      { to: '/dashboard/courses', label: 'Courses', icon: '📚', perm: 'VIEW_COURSE' },
      { to: '/dashboard/course-registrations', label: 'Course Registration', icon: '✅', perm: 'VIEW_COURSE' },
      { to: '/dashboard/departments', label: 'Departments', icon: '🏛️', perm: 'VIEW_DEPARTMENT' },
      { to: '/dashboard/attendance', label: 'Attendance', icon: '📋', perm: 'VIEW_ATTENDANCE' },
      { to: '/dashboard/timetable', label: 'Timetable', icon: '🗓️', perm: 'VIEW_TIMETABLE' },
      { to: '/dashboard/rooms', label: 'Room Availability', icon: '🚪', perm: 'VIEW_ROOM' },
      { to: '/dashboard/grades', label: 'Grades', icon: 'A+', perm: 'VIEW_GRADES' },
      { to: '/dashboard/assignments', label: 'Assignments', icon: '📝', perm: 'VIEW_ASSIGNMENT' },
      { to: '/dashboard/resources', label: 'Resources', icon: '📂', perm: 'VIEW_RESOURCES' },
      { to: '/dashboard/syllabus', label: 'Syllabus', icon: '📋', perm: 'VIEW_SYLLABUS' },
      { to: '/dashboard/calendar', label: 'Academic Calendar', icon: '📅', perm: 'VIEW_CALENDAR' },
    ],
    Campus: [
      { to: '/dashboard/library', label: 'Library', icon: '📖', perm: 'VIEW_LIBRARY' },
      { to: '/dashboard/fees', label: 'Fees', icon: '💰', perm: 'VIEW_FEES' },
      { to: '/dashboard/hostel', label: 'Hostel', icon: '🏠', perm: 'VIEW_HOSTEL' },
      { to: '/dashboard/hostel/complaints', label: 'Hostel Complaints', icon: '📣', perm: 'VIEW_COMPLAINT' },
      { to: '/dashboard/hostel/attendance', label: 'Hostel Attendance', icon: '🛏️', perm: 'VIEW_HOSTEL_ATTENDANCE' },
      { to: '/dashboard/wardens', label: 'Wardens', icon: '🛡️', perm: 'VIEW_HOSTEL' },
      { to: '/dashboard/book-requests', label: 'Book Requests', icon: '📚', perm: 'VIEW_LIBRARY' },
      { to: '/dashboard/feedback', label: 'Feedback', icon: '💬', perm: 'VIEW_FACULTY' },
      { to: '/dashboard/gatepass', label: 'Gate Pass', icon: '🎫', perm: 'VIEW_GATEPASS' },
      { to: '/dashboard/visitors', label: 'Visitors', icon: '🧍', perm: 'VIEW_VISITOR' },
      { to: '/dashboard/placements', label: 'Placements', icon: '💼', perm: 'VIEW_PLACEMENT' },
      { to: '/dashboard/scholarships', label: 'Scholarships', icon: '🏅', perm: 'VIEW_SCHOLARSHIP' },
      { to: '/dashboard/crowdfunding', label: 'Crowdfunding', icon: '🤝', perm: 'VIEW_CROWDFUNDING' },
    ],
    Communication: [
      { to: '/dashboard/announcements', label: 'Announcements', icon: '📢', perm: 'VIEW_ANNOUNCEMENT' },
      { to: '/dashboard/notifications', label: 'Notifications', icon: '🔔', perm: 'VIEW_NOTIFICATION' },
    ],
    Admin: [
      { to: '/dashboard/management', label: 'Institute Management', icon: '🏛️', perm: 'VIEW_ROLE' },
      { to: '/dashboard/roles', label: 'Role Management', icon: '🔑', perm: 'VIEW_ROLE' },
      { to: '/dashboard/employees', label: 'Employees', icon: '👨‍💼', perm: 'VIEW_EMPLOYEE' },
      { to: '/dashboard/payroll', label: 'Payroll', icon: '💸', perm: 'VIEW_PAYROLL' },
      { to: '/dashboard/leaves', label: 'Leave Approvals', icon: '📅', perm: 'VIEW_LEAVE' },
      { to: '/dashboard/workload', label: 'Faculty Workload', icon: '👨‍🏫', perm: 'VIEW_WORKLOAD' },
      { to: '/dashboard/student-affairs', label: 'Student Affairs', icon: '🎓', perm: 'VIEW_STUDENT' },
      { to: '/dashboard/events', label: 'Events', icon: '🎪', perm: 'VIEW_EVENT' },
      { to: '/dashboard/clubs', label: 'Clubs & Societies', icon: '👥', perm: 'VIEW_CLUB' },
      { to: '/dashboard/reports', label: 'Reports', icon: '📊', perm: 'VIEW_REPORT' },
      { to: '/dashboard/audit', label: 'Audit Log', icon: '🗒️', perm: 'VIEW_AUDIT' },
      { to: '/dashboard/settings', label: 'Settings', icon: '⚙️', perm: 'VIEW_SETTINGS' },
    ],
    'Campus Life': [
      { to: '/dashboard/activities', label: 'Activities Hub', icon: '🎯', perm: 'VIEW_EVENT' },
      { to: '/dashboard/volunteer', label: 'Volunteer', icon: '🤝', perm: 'VIEW_VOLUNTEER' },
      { to: '/dashboard/learning', label: 'Learning Portal', icon: '🎓', perm: 'VIEW_COURSE' },
    ],
    Staff: [
      { to: '/dashboard/staff-leave', label: 'My Leave', icon: '🏖️' },
    ],
  };

  const filteredSections = {};
  Object.entries(navSections).forEach(([sectionTitle, items]) => {
    const visible = items.filter((item) => !item.perm || isAdmin || can(item.perm));
    if (visible.length > 0) {
      filteredSections[sectionTitle] = visible;
    }
  });

  return (
    <aside className={`sidebar ${isOpen ? 'open' : ''}`} aria-label="Primary navigation">
      <div className="sidebar-logo">
        <div className="brand-mark">C</div>
        <div className="brand-copy">
          <h2>CampusOne</h2>
          <span>College operations</span>
        </div>
        <button className="sidebar-close" onClick={onClose} aria-label="Close navigation">×</button>
      </div>
      <nav className="sidebar-nav">
        {Object.entries(filteredSections).map(([sectionTitle, items]) => (
          <div key={sectionTitle}>
            <div className="sidebar-section-label">{sectionTitle}</div>
            {items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => (isActive ? 'active' : '')}
                onClick={onClose}
              >
                <span className="nav-icon">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
      <div className="sidebar-footer">
        <span className="sidebar-status-dot" />
        <div><strong>System online</strong><span>Secure campus workspace</span></div>
      </div>
    </aside>
  );
};

export default Sidebar;
