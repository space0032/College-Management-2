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
  'student-affairs': 'Student Affairs', roles: 'Roles & Users',
  'faculty-portal': 'Faculty Portal', 'course-registrations': 'Course Registration',
  complaints: 'Hostel Complaints', wardens: 'Wardens', 'book-requests': 'Book Requests',
  feedback: 'Feedback', 'hostel/attendance': 'Hostel Attendance',
};

const PATH_TITLES = {
  '/dashboard/hostel/complaints': 'Hostel Complaints',
  '/dashboard/hostel/attendance': 'Hostel Attendance',
  '/dashboard/course-registrations': 'Course Registration',
  '/dashboard/faculty-portal': 'Faculty Portal',
  '/dashboard/wardens': 'Wardens',
  '/dashboard/book-requests': 'Book Requests',
  '/dashboard/feedback': 'Feedback',
};

export function getPageTitle(pathname) {
  const exact = PATH_TITLES[pathname];
  if (exact) return exact;
  const section = pathname.split('/').filter(Boolean).pop() || '';
  return TITLES[section] || '';
}

const PageTitle = () => {
  const { pathname } = useLocation();

  useEffect(() => {
    document.title = `${getPageTitle(pathname) || 'Page'} | CampusOne`;
  }, [pathname]);

  return null;
};

export default PageTitle;
