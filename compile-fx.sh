#!/bin/bash
# JavaFX Compile Script for College Management System

echo "==================================="
echo "College Management System - JavaFX Build"
echo "==================================="

# JavaFX path
JAVAFX_PATH="lib/javafx-sdk-21.0.2/lib"

# Check if JavaFX exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "Error: JavaFX SDK not found at $JAVAFX_PATH"
    exit 1
fi

# Create bin directory
echo "Creating bin directory..."
mkdir -p bin

# Find all Java files
echo "Finding Java source files..."
find src -name "*.java" > sources.txt

# Compile with JavaFX
echo "Compiling Java files with JavaFX..."
javac --module-path "$JAVAFX_PATH" \
      --add-modules javafx.controls,javafx.fxml,javafx.graphics \
      -cp "lib/*:lib/javafx-sdk-21.0.2/lib/*" \
      -d bin \
      @sources.txt 2>&1

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "To run the application:"
    echo "  ./run-fx.sh"
else
    echo "✗ Compilation failed!"
    exit 1
fi

# Cleanup
rm -f sources.txt
