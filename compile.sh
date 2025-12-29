#!/bin/bash

# College Management System - Build Script
# This script compiles all Java source files and creates a runnable JAR

echo "==================================="
echo "College Management System - Build"
echo "==================================="

# Create bin directory if it doesn't exist
echo "Creating bin directory..."
mkdir -p bin

# Download MySQL Connector if not exists
if [ ! -f "lib/mysql-connector-j-8.0.33.jar" ]; then
    echo "MySQL Connector not found. Please download it manually."
    echo "Download from: https://dev.mysql.com/downloads/connector/j/"
    echo "Place the JAR file in the lib/ directory"
    echo ""
    echo "Quick download (if wget available):"
    echo "cd lib && wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
fi

# Find all Java files
echo "Finding Java source files..."
JAVA_FILES=$(find src -name "*.java")

# Compile all Java files
echo "Compiling Java files..."
javac -d bin -cp "lib/*" $JAVA_FILES

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "To run the application:"
    echo "  java -cp \"bin:lib/*\" com.college.Main"
    echo ""
    echo "Or use:"
    echo "  ./run.sh"
else
    echo "✗ Compilation failed!"
    exit 1
fi
