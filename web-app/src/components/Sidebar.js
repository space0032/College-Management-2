import React from 'react';
import { NavLink } from 'react-router-dom';

const NAV_SECTIONS = [
  {
    label: 'Overview',
    items: [
      { to: '/dashboard', label: 'Home', icon: '🏠', end: true },
      { to: '/dashboard/profile', label: 'Profile', icon: '👤' },
    ],
  },
  {
    label: 'Academic',
    items: [
      { to: '/dashboard/students', label: 'Students', icon: '🎓' },
      { to: '/dashboard/faculty', label: 'Faculty', icon: '👩‍🏫' },
      { to: '/dashboard/courses', label: 'Courses', icon: '📚' },
      { to: '/dashboard/departments', label: 'Departments', icon: '🏛️' },
      { to: '/dashboard/attendance', label: 'Attendance', icon: '📋' },
      { to: '/dashboard/timetable', label: 'Timetable', icon: '🗓️' },
    ],
  },
  {
    label: 'Campus',
    items: [
      { to: '/dashboard/library', label: 'Library', icon: '📖' },
      { to: '/dashboard/fees', label: 'Fees', icon: '💰' },
      { to: '/dashboard/hostel', label: 'Hostel', icon: '🏠' },
      { to: '/dashboard/placements', label: 'Placements', icon: '💼' },
    ],
  },
  {
    label: 'Communication',
    items: [
      { to: '/dashboard/announcements', label: 'Announcements', icon: '📢' },
      { to: '/dashboard/notifications', label: 'Notifications', icon: '🔔' },
    ],
  },
];

const Sidebar = () => {
  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h2>🎓 College Management</h2>
        <span>Management System</span>
      </div>
      <nav className="sidebar-nav">
        {NAV_SECTIONS.map((section) => (
          <div key={section.label}>
            <div className="sidebar-section-label">{section.label}</div>
            {section.items.map((item) => (
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
