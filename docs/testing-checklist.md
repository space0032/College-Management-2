# Testing Checklist for Web Application

## Functional Testing

### Authentication
- [ ] Login with valid credentials (Admin: admin/admin123)
- [ ] Login with valid credentials (Faculty: FAC001/123)
- [ ] Login with valid credentials (Student: TES2026001/123)
- [ ] Login with invalid credentials returns 401 error
- [ ] Logout clears token and redirects to login
- [ ] Session persistence (page reload keeps user logged in)
- [ ] Expired/invalid token redirects to login page

### For Each Page (14 pages total):
- [ ] Page loads correctly without errors
- [ ] All UI elements are rendered
- [ ] All buttons are clickable
- [ ] Forms submit correctly
- [ ] Validation works (required fields)
- [ ] Data displays in tables correctly
- [ ] Edit/Delete operations work
- [ ] Role-based access works correctly

### Student Management
- [ ] Student list loads
- [ ] Add new student works
- [ ] Search students works

### Faculty Management
- [ ] Faculty list loads
- [ ] Add new faculty works
- [ ] Update faculty works
- [ ] Delete faculty works
- [ ] Search faculty works

### Course Management
- [ ] Course list loads
- [ ] Add new course works
- [ ] Update course works
- [ ] Delete course works

### Attendance
- [ ] Attendance loads with filter by courseId and date
- [ ] Mark single attendance works
- [ ] Bulk mark attendance works

### Library
- [ ] Book list loads
- [ ] Add book works

### Fees
- [ ] Pending fees list loads

### Timetable
- [ ] Timetable loads with department/semester filter
- [ ] Add timetable entry works
- [ ] Delete timetable entry works

### Placement
- [ ] Company list loads
- [ ] Add company works
- [ ] Delete company works
- [ ] Drive list loads
- [ ] Add drive works
- [ ] Delete drive works

### Hostel
- [ ] Hostel list loads
- [ ] Room list loads
- [ ] Allocation list loads
- [ ] Allocate room works
- [ ] Vacate room works

### Announcements
- [ ] Announcement list loads
- [ ] Add announcement works
- [ ] Update announcement works
- [ ] Delete announcement works

### Notifications
- [ ] Notification list loads

### Departments
- [ ] Department list loads
- [ ] Add department works
- [ ] Update department works
- [ ] Delete department works

## UI/UX Testing
- [ ] Sidebar navigation works for all pages
- [ ] Header shows correct username and role
- [ ] Logout button works
- [ ] Modal dialogs open and close correctly
- [ ] Loading spinners appear during API calls
- [ ] Error messages display correctly
- [ ] DataTable shows empty state message when no data
- [ ] Responsive design at 768px breakpoint
- [ ] Colors match AtlantaFX theme (blues, grays)

## API Testing
- [ ] POST /api/auth/login returns token + user
- [ ] POST /api/auth/logout removes session
- [ ] GET /api/auth/session returns current user
- [ ] GET /api/students returns list
- [ ] POST /api/students creates student
- [ ] GET /api/faculty returns list
- [ ] POST /api/faculty creates faculty
- [ ] PUT /api/faculty/{id} updates faculty
- [ ] DELETE /api/faculty/{id} deletes faculty
- [ ] GET /api/courses returns list
- [ ] POST /api/courses creates course
- [ ] GET /api/attendance returns records
- [ ] POST /api/attendance marks attendance
- [ ] POST /api/attendance/bulk bulk marks attendance
- [ ] GET /api/library/books returns books
- [ ] GET /api/fees/pending returns pending fees
- [ ] GET /api/timetable returns entries
- [ ] GET /api/placements/drives returns drives
- [ ] GET /api/placements/companies returns companies
- [ ] GET /api/hostels returns hostels
- [ ] GET /api/hostels/rooms returns rooms
- [ ] GET /api/hostels/allocations returns allocations
- [ ] GET /api/announcements returns announcements
- [ ] GET /api/notifications returns notifications
- [ ] GET /api/departments returns departments
- [ ] GET /api/roles returns roles
- [ ] GET /api/users returns users
- [ ] Protected endpoints return 401 without token
- [ ] CORS headers present on all responses

## Security Testing
- [ ] Tokens validated on protected endpoints
- [ ] Invalid token rejected
- [ ] Missing token rejected
- [ ] CORS headers set correctly (Access-Control-Allow-Origin)

## Cross-browser Testing
- [ ] Chrome
- [ ] Firefox
- [ ] Safari
- [ ] Edge

## Local Desktop App Testing
- [ ] JavaFX app still launches correctly
- [ ] No breaking changes to existing functionality
- [ ] Login still works in desktop app
- [ ] All modules still work in desktop app

## Running Tests

### Start the API Server
```bash
# Compile first
./build.sh

# Start API server
java -cp target/classes:lib/* com.college.api.ApiServer
# API runs on http://localhost:7000
```

### Start the Web App
```bash
cd web-app
npm install
npm start
# Web app runs on http://localhost:3000
```

### Test API Manually
```bash
# Test login
curl -X POST http://localhost:7000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Use returned token for other requests
TOKEN="<token-from-login>"

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:7000/api/students
```
