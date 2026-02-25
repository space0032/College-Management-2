import React from 'react';
import { NavLink } from 'react-router-dom';

const Sidebar = () => {
  const userRole = localStorage.getItem('userRole') || 'STUDENT';

  const navSections = {
    Overview: [
      { to: '/dashboard', label: 'Home', icon: '🏠', end: true },
      { to: '/dashboard/profile', label: 'My Profile', icon: '👤' },
    ],
    Academic: [
      { to: '/dashboard/students', label: 'Students', icon: '🎓' },
      { to: '/dashboard/faculty', label: 'Faculty', icon: '👩‍🏫' },
      { to: '/dashboard/courses', label: 'Courses', icon: '📚' },
      { to: '/dashboard/departments', label: 'Departments', icon: '🏛️' },
      { to: '/dashboard/attendance', label: 'Attendance', icon: '📋' },
      { to: '/dashboard/timetable', label: 'Timetable', icon: '🗓️' },
      { to: '/dashboard/grades', label: 'Grades', icon: 'A+' },
      { to: '/dashboard/assignments', label: 'Assignments', icon: '📝' },
    ],
    Campus: [
      { to: '/dashboard/library', label: 'Library', icon: '📖' },
      { to: '/dashboard/fees', label: 'Fees', icon: '💰' },
      { to: '/dashboard/hostel', label: 'Hostel', icon: '🏠' },
      { to: '/dashboard/gatepass', label: 'Gate Pass', icon: '🎫' },
      { to: '/dashboard/visitors', label: 'Visitors', icon: '🧍' },
      { to: '/dashboard/placements', label: 'Placements', icon: '💼' },
      { to: '/dashboard/scholarships', label: 'Scholarships', icon: '💰' },
      { to: '/dashboard/crowdfunding', label: 'Crowdfunding', icon: '🤝' },
      { to: '/dashboard/clubs', label: 'Clubs', icon: '🎭' },
      { to: '/dashboard/events', label: 'Events', icon: '🎟️' },
    ],
    Communication: [
      { to: '/dashboard/announcements', label: 'Announcements', icon: '📢' },
      { to: '/dashboard/notifications', label: 'Notifications', icon: '🔔' },
    ]
  };

  if (userRole === 'ADMIN') {
    navSections['Admin'] = [
      { to: '/dashboard/management', label: 'Institute Mgmt', icon: '🏛️' },
      { to: '/dashboard/reports', label: 'Reports', icon: '📊' },
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
