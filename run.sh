#!/bin/bash
set -e

# Configuration
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi
OUT_DIR="target/classes"
LIB_DIR="lib"
MAIN_CLASS="com.college.Launcher"

# Classpath
CP="$OUT_DIR"
if [ -d "$LIB_DIR" ]; then
    # Use wildcard for classpath to avoid manual listing
    CP="$CP:$LIB_DIR/*"
fi

# Run
# Note: For JavaFX 11+, we typically need modules, but running from classpath 
# with the jars usually works if we main class extends Application properly 
# or if we use a helper launcher. 
# Attempting direct run first.
java --enable-native-access=ALL-UNNAMED \
     -cp "$CP" "$MAIN_CLASS"
