#!/bin/bash

# Timetable Migration Script
# This script applies the database changes needed for the timetable enhancement

echo "========================================="
echo "Timetable Migration Script"
echo "========================================="
echo ""
echo "This will add department and semester support to the database."
echo ""

# Check if MySQL is accessible
if ! command -v mysql &> /dev/null; then
    echo "Error: mysql command not found. Please install MySQL client."
    exit 1
fi

echo "Enter your MySQL root password when prompted..."
echo ""

# Run the migration
mysql -u root -p college_management < database/apply_timetable_migrations.sql

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================="
    echo "Migration completed successfully!"
    echo "========================================="
    echo ""
    echo"You can now run the application:"
    echo "  ./run.sh"
    echo ""
else
    echo ""
    echo "========================================="
    echo "Migration failed!"
    echo "========================================="
    echo ""
    echo "Please check:"
    echo "1. MySQL is running"
    echo "2. Database 'college_management' exists"
    echo "3. You entered the correct password"
    echo ""
    exit 1
fi
