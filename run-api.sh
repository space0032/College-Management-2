#!/bin/bash

# Configuration
OUT_DIR="target/classes"
LIB_DIR="lib"
MAIN_CLASS="com.college.api.ApiServer"

# Classpath
# Include compiled classes
CP="$OUT_DIR"

# Include local libs (JavaFX, MySQL)
CP="$CP:$LIB_DIR/*"

# Include Maven dependencies (Javalin, Kotlin, SLF4J, Jackson, Jetty, WebSocket, etc.)
# Since we might not have a full local repo structure usable by CP easily without shading,
# allow using `mvn exec:java` if available, OR try to resolve from local m2.

# However, the user environment seems to rely on `build.sh` manually managing classpath or a simple `mvn` build.
# The simplest restartable way for the API (which has significant transitive dependencies: javalin -> kotlin, jetty, etc.)
# is to use `mvn exec:java`.

echo "Starting API Server..."
java -cp "$CP" $MAIN_CLASS
