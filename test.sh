#!/bin/bash
set -e

# Configuration
OUT_DIR="target/classes"
TEST_OUT_DIR="target/test-classes"
LIB_DIR="lib"

# Clean
mkdir -p "$TEST_OUT_DIR"

# Classpath
CP="$OUT_DIR:$TEST_OUT_DIR"
CP="$OUT_DIR:$TEST_OUT_DIR"
if [ -d "$LIB_DIR" ]; then
    # Explicitly add every jar to classpath
    for jar in "$LIB_DIR"/*.jar; do
        CP="$CP:$jar"
    done
fi

# Compile Tests
echo "Compiling tests..."
find "src/test/java" -name "*.java" > test_sources.txt
if [ -s test_sources.txt ]; then
    javac -d "$TEST_OUT_DIR" -cp "$CP" -sourcepath "src/test/java" @test_sources.txt
else
    echo "No test files found."
    exit 0
fi

# Run Tests
echo "Running tests..."
# Using JUnit Console Launcher if available, or a simple custom runner if not.
# Since we don't have junit-platform-console-standalone jar, we'll assume we might need to download it or run a main class if one exists.
# For now, let's try to run known test classes directly or use java -jar junit-platform-console-standalone.jar
# Checking if junit console is in lib...

if [ -f "lib/junit-platform-console-standalone.jar" ]; then
    java -jar lib/junit-platform-console-standalone.jar -cp "$CP" --scan-classpath
else
    echo "JUnit Console Runner not found. Attempting to run specific test verification class if it exists."
    # If the user has a specific test suite main class
    if [ -f "$TEST_OUT_DIR/com/college/ReportsVerificationTest.class" ]; then
        java -cp "$CP" com.college.ReportsVerificationTest
    else 
         echo "No verification test found to run."
    fi
fi
