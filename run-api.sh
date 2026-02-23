#!/bin/bash
# Start the College Management REST API Server
# The API runs on port 7000 and is consumed by the React web frontend

set -e

OUT_DIR="target/classes"
MAIN_CLASS="com.college.api.ApiServer"

# Build if not already built
if [ ! -d "$OUT_DIR" ]; then
    echo "Compiling project..."
    mvn -B compile -DskipTests --file pom.xml
fi

echo "Starting College Management API Server on port 7000..."
echo "API URL: http://localhost:7000/api"
mvn -q exec:java -Dexec.mainClass="$MAIN_CLASS" --file pom.xml
