import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

const TITLES = {
  '': 'Login', dashboard: 'Dashboard', students: 'Students', faculty: 'Faculty', courses: 'Courses',
  attendance: 'Attendance', library: 'Library', fees: 'Fees', timetable: 'Timetable', placements: 'Placements',
  hostel: 'Hostels', clubs: 'Clubs', events: 'Events', grades: 'Grades', reports: 'Reports',
  gatepass: 'Gate Passes', visitors: 'Visitors', calendar: 'Academic Calendar', scholarships: 'Scholarships',
  assignments: 'Assignments', crowdfunding: 'Crowdfunding', management: 'Institute Management',
  resources: 'Resources', employees: 'Employees', leaves: 'Leave Approvals', rooms: 'Room Availability',
  workload: 'Faculty Workload', activities: 'Student Activities', announcements: 'Announcements',
  notifications: 'Notifications', payroll: 'Payroll', syllabus: 'Syllabus', learning: 'Learning Portal',
   volunteer: 'Volunteer Tasks', 'student-profile': 'Student Profile', audit: 'Audit Log', departments: 'Departments',
   specializations: 'Tracks',
  profile: 'Profile', 'staff-leave': 'Staff Leave', 'change-password': 'Change Password', settings: 'Settings',
  'student-affairs': 'Student Affairs', roles: 'Roles & Users'
};

const PageTitle = () => {
  const { pathname } = useLocation();

  useEffect(() => {
    const section = pathname.split('/').filter(Boolean).pop() || '';
    document.title = `${TITLES[section] || 'Page'} | CampusOne`;
  }, [pathname]);

  return null;
};

export default PageTitle;
