# College Management System API Documentation

## Overview

The College Management API is a RESTful HTTP API running on port **7000** using Java's built-in HTTP server.

**Base URL:** `http://localhost:7000/api`

## Authentication

All endpoints except `/api/auth/login` require a Bearer token in the `Authorization` header.

### POST /api/auth/login
Authenticate and receive a session token.

**Request:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "uuid-token-here",
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "roleId": 1
  }
}
```

### POST /api/auth/logout
Invalidate the current session token.

**Headers:** `Authorization: Bearer <token>`

### GET /api/auth/session
Get current session information.

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "userId": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

---

## Students

### GET /api/students
Get all students.

### POST /api/students
Create a new student.

**Request Body:** Student JSON object

### GET /api/students/{id}
Get student by ID.

### GET /api/students/search?q={keyword}
Search students by keyword.

---

## Faculty

### GET /api/faculty
Get all faculty members.

### POST /api/faculty
Create a new faculty member.

### GET /api/faculty/{id}
Get faculty by ID.

### PUT /api/faculty/{id}
Update faculty member.

### DELETE /api/faculty/{id}
Delete faculty member.

### GET /api/faculty/search?q={keyword}
Search faculty by keyword.

---

## Courses

### GET /api/courses
Get all courses.

### POST /api/courses
Create a new course.

### GET /api/courses/{id}
Get course by ID.

### PUT /api/courses/{id}
Update course.

### DELETE /api/courses/{id}
Delete course.

---

## Attendance

### GET /api/attendance?courseId={id}&date={date}
Get attendance records for a course on a specific date.

### GET /api/attendance/student/{studentId}
Get attendance records for a specific student.

### POST /api/attendance
Mark attendance for a single student.

**Request Body:**
```json
{
  "studentId": 1,
  "courseId": 1,
  "date": "2024-01-15",
  "status": "PRESENT"
}
```

### POST /api/attendance/bulk
Mark attendance for multiple students at once.

**Request Body:** Array of attendance records.

---

## Library

### GET /api/library/books
Get all books.

### POST /api/library/books
Add a new book.

### PUT /api/library/books/{id}
Update book information.

---

## Fees

### GET /api/fees/pending
Get all pending fee records.

---

## Timetable

### GET /api/timetable?department={dept}&semester={sem}
Get timetable entries for a department and semester.

### POST /api/timetable
Save a timetable entry.

### DELETE /api/timetable/{id}
Delete a timetable entry.

---

## Placements

### GET /api/placements/drives
Get all placement drives.

### POST /api/placements/drives
Add a new placement drive.

### DELETE /api/placements/drives/{id}
Delete a placement drive.

### GET /api/placements/companies
Get all placement companies.

### POST /api/placements/companies
Add a new company.

### DELETE /api/placements/companies/{id}
Delete a company.

---

## Hostels

### GET /api/hostels
Get all hostels.

### POST /api/hostels
Add a new hostel.

### GET /api/hostels/rooms
Get all rooms.

### POST /api/hostels/rooms
Add a new room.

### GET /api/hostels/allocations
Get all active allocations.

### POST /api/hostels/allocations
Allocate a room to a student.

### DELETE /api/hostels/allocations/{id}
Vacate a room (remove allocation).

---

## Announcements

### GET /api/announcements
Get all announcements.

### POST /api/announcements
Create a new announcement.

### PUT /api/announcements/{id}
Update an announcement.

### DELETE /api/announcements/{id}
Delete an announcement.

---

## Notifications

### GET /api/notifications
Get all pending notifications.

### POST /api/notifications
Create a new notification.

---

## Departments

### GET /api/departments
Get all departments.

### POST /api/departments
Add a new department.

### PUT /api/departments/{id}
Update a department.

### DELETE /api/departments/{id}
Delete a department.

---

## Roles

### GET /api/roles
Get all roles.

### POST /api/roles
Create a new role.

### DELETE /api/roles/{id}
Delete a role.

---

## Users

### GET /api/users
Get all users.

### DELETE /api/users/{id}
Delete a user.

---

## Error Responses

All errors return JSON:

```json
{
  "error": "Error message here"
}
```

**HTTP Status Codes:**
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized (invalid/missing token)
- `404` - Not Found
- `405` - Method Not Allowed
- `500` - Internal Server Error

## CORS

All endpoints support CORS with:
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`
- `Access-Control-Allow-Headers: Content-Type, Authorization`
