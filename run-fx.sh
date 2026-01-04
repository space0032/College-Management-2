#!/bin/bash
# JavaFX Run Script for College Management System

echo "==================================="
echo "College Management System - JavaFX"
echo "==================================="

# JavaFX path
JAVAFX_PATH="lib/javafx-sdk-21.0.2/lib"

# Check if JavaFX exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo "Error: JavaFX SDK not found at $JAVAFX_PATH"
    exit 1
fi

echo "Starting College Management System (JavaFX)..."
echo ""

java --module-path "$JAVAFX_PATH" \
     --add-modules javafx.controls,javafx.fxml,javafx.graphics \
     -cp "bin:lib/*:lib/javafx-sdk-21.0.2/lib/*" \
     com.college.MainFX
