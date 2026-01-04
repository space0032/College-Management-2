#!/bin/bash
# Quick Setup Script for College Management System

echo "=========================================="
echo "College Management System - Quick Setup"
echo "=========================================="
echo ""

echo "Step 1: Creating database..."
echo ""

# Try without password first
if mysql -u root -e "SOURCE database/schema.sql" 2>/dev/null; then
    echo "✓ Database created successfully (no password)!"
else
    echo "Trying with password..."
    
    if mysql -u root -p -e "SOURCE database/schema.sql"; then
        echo "✓ Database created successfully!"
    else
        echo "✗ Failed to create database. Please run manually:"
        echo "  mysql -u root -p < database/schema.sql"
        exit 1
    fi
fi

echo ""
echo "Step 2: Verifying database..."
if mysql -u root -e "USE college_management; SHOW TABLES;" 2>/dev/null; then
    echo "✓ Database verified!"
    echo ""
    echo "Step 3: You can now run the application:"
    echo "  ./run.sh"
    echo ""
    echo "Default login credentials:"
    echo "  Username: admin"
    echo "  Password: admin123"
    echo "  Role: ADMIN"
else
    echo "Note: Update password in .env file with your database credentials"
fi
