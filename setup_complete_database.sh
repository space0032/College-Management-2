#!/bin/bash
# Complete Database Setup Script for College Management System
# This script applies ALL necessary schema files in the correct order

echo "=========================================="
echo "College Management System"
echo "Complete Database Setup"
echo "=========================================="
echo ""

# Check if MySQL is accessible
if ! command -v mysql &> /dev/null; then
    echo "Error: mysql command not found. Please install MySQL client."
    exit 1
fi

echo "This script will set up the complete database schema."
echo "It will apply all necessary migrations and schema files."
echo ""
echo "You may be prompted for your sudo password..."
echo ""

# Function to execute SQL file
execute_sql() {
    local file=$1
    local description=$2
    
    echo "→ $description"
    sudo mysql college_management < "$file" 2>&1
    
    if [ $? -eq 0 ]; then
        echo "  ✓ Success"
        return 0
    else
        echo "  ✗ Failed"
        return 1
    fi
}

# Apply schemas in order
echo "Step 1: Creating base schema..."
sudo mysql < database/schema.sql

if [ $? -ne 0 ]; then
    echo "Failed to create base schema. Exiting."
    exit 1
fi

echo ""
echo "Step 2: Applying additional schemas..."
echo ""

# Apply all the required schema files
execute_sql "database/department_schema.sql" "Adding departments table"
execute_sql "rbac_schema.sql" "Adding RBAC (roles and permissions)"
execute_sql "database/audit_logs_schema.sql" "Adding audit logs"
execute_sql "database/warden_schema.sql" "Adding wardens table"
execute_sql "assignments.sql" "Adding assignments and submissions tables"
execute_sql "database/gate_pass_schema.sql" "Updating gate passes table"
execute_sql "database/apply_timetable_migrations.sql" "Updating timetable schema"
execute_sql "database/update_student_hostel.sql" "Adding hostelite flag to students"

echo ""
echo "=========================================="
echo "Database Setup Complete!"
echo "=========================================="
echo ""
echo "You can now run the application:"
echo "  ./run.sh"
echo ""
echo "Default login credentials:"
echo "  Username: admin"
echo "  Password: admin123"
echo "  Role: ADMIN"
echo ""
