#!/bin/bash

# College Management System - Run Script

echo "==================================="
echo "College Management System"
echo "==================================="
echo ""

# Check if compiled
if [ ! -d "bin" ] || [ ! "$(ls -A bin)" ]; then
    echo "Project not compiled. Running compile.sh first..."
    ./compile.sh
    echo ""
fi

# Check for MySQL connector
if [ ! -f "lib/mysql-connector-j-8.0.33.jar" ] && [ -z "$(ls lib/*.jar 2>/dev/null)" ]; then
    echo "ERROR: MySQL Connector JAR not found in lib/ directory!"
    echo "Please download from: https://dev.mysql.com/downloads/connector/j/"
    exit 1
fi

echo "Starting College Management System..."
echo ""

# Run the application
java -cp "bin:lib/*" com.college.Main
