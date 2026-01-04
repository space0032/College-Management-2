#!/bin/bash
set -e

# Configuration
SRC_DIR="src/main/java"
TEST_SRC_DIR="src/test/java"
OUT_DIR="target/classes"
LIB_DIR="lib"

# Clean
echo "Cleaning target..."
rm -rf target
mkdir -p "$OUT_DIR"

# Classpath (include current dir and all jars in lib)
CP=".:$OUT_DIR"
if [ -d "$LIB_DIR" ]; then
    # Use wildcard for classpath to avoid manual listing
    CP="$CP:$LIB_DIR/*"
fi

# Compile Sources
echo "Compiling sources..."
find "$SRC_DIR" -name "*.java" > sources.txt
if [ -s sources.txt ]; then
    javac -d "$OUT_DIR" -cp "$CP" -sourcepath "$SRC_DIR" @sources.txt
    echo "Compilation successful."
else
    echo "No source files found."
fi

# Copy Resources (fxml, images, css) if any
# Copy Resources (fxml, images, css) if any
echo "Copying resources..."
# Copy embedded resources from src/main/java (legacy)
find "$SRC_DIR" -type f \( -name "*.fxml" -o -name "*.css" -o -name "*.png" -o -name "*.jpg" \) -exec cp --parents {} "$OUT_DIR" \;

# Copy standard resources (src/main/resources)
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* "$OUT_DIR" 2>/dev/null || :
fi

# Cleanup temp file
rm sources.txt

echo "Build Complete. Classes in $OUT_DIR"
