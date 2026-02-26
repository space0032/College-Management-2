import React from 'react';
import { NavLink } from 'react-router-dom';
import SessionManager from '../utils/SessionManager';

const Sidebar = () => {
  const userRole = SessionManager.getUserRole() || 'STUDENT';
  const isAdmin = userRole === 'ADMIN';
  const isFaculty = userRole === 'FACULTY';
  const isStudent = userRole === 'STUDENT';

  const navSections = {
    Overview: [
      { to: '/dashboard', label: 'Home', icon: '🏠', end: true },
      { to: '/dashboard/profile', label: 'My Profile', icon: '👤' },
    ],
    Academic: [
      ...(isAdmin || isFaculty ? [
        { to: '/dashboard/students', label: 'Students', icon: '🎓' },
        { to: '/dashboard/student-profile', label: 'Student Profile', icon: '📋' },
      ] : []),
      ...(isAdmin ? [{ to: '/dashboard/faculty', label: 'Faculty', icon: '👩‍🏫' }] : []),
      { to: '/dashboard/courses', label: 'Courses', icon: '📚' },
      { to: '/dashboard/departments', label: 'Departments', icon: '🏛️' },
      { to: '/dashboard/attendance', label: 'Attendance', icon: '📋' },
      { to: '/dashboard/timetable', label: 'Timetable', icon: '🗓️' },
      { to: '/dashboard/rooms', label: 'Room Availability', icon: '🚪' },
      { to: '/dashboard/grades', label: 'Grades', icon: 'A+' },
      { to: '/dashboard/assignments', label: 'Assignments', icon: '📝' },
      { to: '/dashboard/resources', label: 'Resources', icon: '📂' },
      { to: '/dashboard/syllabus', label: 'Syllabus', icon: '📋' },
      { to: '/dashboard/calendar', label: 'Academic Calendar', icon: '📅' },
    ],
    Campus: [
      { to: '/dashboard/library', label: 'Library', icon: '📖' },
      { to: '/dashboard/fees', label: 'Fees', icon: '💰' },
      { to: '/dashboard/hostel', label: 'Hostel', icon: '🏠' },
      { to: '/dashboard/gatepass', label: 'Gate Pass', icon: '🎫' },
      { to: '/dashboard/visitors', label: 'Visitors', icon: '🧍' },
      { to: '/dashboard/placements', label: 'Placements', icon: '💼' },
      { to: '/dashboard/scholarships', label: 'Scholarships', icon: '🏅' },
      { to: '/dashboard/crowdfunding', label: 'Crowdfunding', icon: '🤝' },
    ],
    Communication: [
      { to: '/dashboard/announcements', label: 'Announcements', icon: '📢' },
      { to: '/dashboard/notifications', label: 'Notifications', icon: '🔔' },
    ],
  };

  // ADMIN — full management panel (including Events, Clubs, Student Affairs)
  if (isAdmin) {
    navSections['Admin'] = [
      { to: '/dashboard/management', label: 'Institute Mgmt', icon: '🏛️' },
      { to: '/dashboard/roles', label: 'Role Management', icon: '🔑' },
      { to: '/dashboard/employees', label: 'Employees', icon: '👨‍💼' },
      { to: '/dashboard/payroll', label: 'Payroll', icon: '💸' },
      { to: '/dashboard/leaves', label: 'Leave Approvals', icon: '📅' },
      { to: '/dashboard/workload', label: 'Faculty Workload', icon: '👨‍🏫' },
      { to: '/dashboard/student-affairs', label: 'Student Affairs', icon: '🎓' },
      { to: '/dashboard/events', label: 'Events', icon: '🎪' },
      { to: '/dashboard/clubs', label: 'Clubs & Societies', icon: '👥' },
      { to: '/dashboard/reports', label: 'Reports', icon: '📊' },
      { to: '/dashboard/audit', label: 'Audit Log', icon: '🗒️' },
      { to: '/dashboard/settings', label: 'Settings', icon: '⚙️' },
    ];
  }

  // FACULTY — campus life + self-service staff leave + settings
  if (isFaculty) {
    navSections['Campus Life'] = [
      { to: '/dashboard/activities', label: 'Activities Hub', icon: '🎯' },
      { to: '/dashboard/events', label: 'Events', icon: '🎪' },
      { to: '/dashboard/clubs', label: 'Clubs & Societies', icon: '👥' },
      { to: '/dashboard/volunteer', label: 'Volunteer', icon: '🤝' },
    ];
    navSections['Staff'] = [
      { to: '/dashboard/staff-leave', label: 'My Leave', icon: '🏖️' },
      { to: '/dashboard/settings', label: 'Settings', icon: '⚙️' },
    ];
  }

  // STUDENT — campus life + account settings
  if (isStudent) {
    navSections['Campus Life'] = [
      { to: '/dashboard/activities', label: 'Activities Hub', icon: '🎯' },
      { to: '/dashboard/events', label: 'Events', icon: '🎪' },
      { to: '/dashboard/clubs', label: 'Clubs & Societies', icon: '👥' },
      { to: '/dashboard/volunteer', label: 'Volunteer', icon: '🤝' },
      { to: '/dashboard/learning', label: 'Learning Portal', icon: '🎓' },
    ];
    navSections['Account'] = [
      { to: '/dashboard/settings', label: 'Settings', icon: '⚙️' },
    ];
  }

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h2>🎓 College Management</h2>
      </div>
      <nav className="sidebar-nav">
        {Object.entries(navSections).map(([sectionTitle, items]) => (
          <div key={sectionTitle}>
            <div className="sidebar-section-label">{sectionTitle}</div>
            {items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => (isActive ? 'active' : '')}
              >
                <span className="nav-icon">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
    </aside>
  );
};

export default Sidebar;
