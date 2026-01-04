# Database Setup Scripts

This folder contains all the SQL scripts that were used to set up and fix the College Management System database. These scripts have already been executed and are kept here for reference only.

## ✅ Successfully Applied Scripts

### Initial Setup Scripts
- `faculty_data.sql` - Created 30 faculty members across all departments
- `sample_data.sql` - Generated 100 students, courses, timetables, and fees
- `rbac_schema.sql` - Set up Role-Based Access Control system

### Fix Scripts (Applied in Order)
1. `fix_all_issues.sql` - **CRITICAL** - Fixed password hashes for all users + audit log constraints
2. `fix_data.sql` - Generated missing fees records for all students
3. `fix_hostel_data.sql` - Created hostel rooms and allocated to students
4. `fix_audit_constraint.sql` - Fixed foreign key constraint for audit logging
5. `fix_timetable.sql` - Timetable schema fixes
6. `add_semester_to_assignments.sql` - Added semester column to assignments
7. `add_attendance_columns.sql` - Updated attendance table structure

### Debug/Development Scripts
- `check_attendance_schema.sql` - Schema verification script
- `debug_data.sql` - Data debugging queries
- `final_fix.sql` - Final adjustments

## 🚨 Important Notes

1. **These scripts have already been executed** - The database is fully set up and working
2. **Do not re-run these scripts** unless you're completely resetting the database
3. **The application does not use these files** - They were only for initial setup

## 📊 Current Database State

After all scripts have been applied:
- **Users:** 158 total (1 Admin, 31 Faculty, 125 Students, 1 Warden)
- **Students:** 243 total in database
- **Fees:** 243 records (all students)
- **Hostel Rooms:** 150 rooms
- **Hostel Allocations:** 142 students
- **Courses:** 124 courses
- **Timetables:** 256 schedules

## 🔄 If You Need to Reset

If you want to completely reset and reapply everything:

```bash
# 1. Drop and recreate database
sudo mysql -e "DROP DATABASE IF EXISTS college_management; CREATE DATABASE college_management;"

# 2. Apply base schema
sudo mysql college_management < database/schema.sql

# 3. Apply additional schemas (in order)
sudo mysql college_management < database/department_schema.sql
sudo mysql college_management < ../rbac_schema.sql  # Now in root
sudo mysql college_management < ../assignments.sql  # Now in root
sudo mysql college_management < database/audit_logs_schema.sql
sudo mysql college_management < database/warden_schema.sql
sudo mysql college_management < database/gate_pass_schema.sql
sudo mysql college_management < database/apply_timetable_migrations.sql
sudo mysql college_management < database/update_student_hostel.sql

# 4. Apply data and fixes
sudo mysql college_management < setup_scripts/faculty_data.sql
sudo mysql college_management < setup_scripts/sample_data.sql
sudo mysql college_management < setup_scripts/fix_all_issues.sql
sudo mysql college_management < setup_scripts/fix_data.sql
sudo mysql college_management < setup_scripts/fix_hostel_data.sql
```

## 📝 Login Credentials

After setup, you can login with:
- **Admin:** admin / admin123
- **Faculty:** dr.amit.sharma / faculty123 (or any faculty)
- **Student:** cs001 / student123 (or cs001-cs030, ee001-ee030, etc.)

See `CREDENTIALS.txt` in the root directory for all 158 accounts.
