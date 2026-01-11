#!/bin/bash
set -e

M2_REPO="$HOME/.m2/repository"
JFX_VERSION="25.0.0"
BASE_URL="https://repo1.maven.org/maven2/org/openjfx"

echo "Creating local Maven repository structure for JavaFX $JFX_VERSION..."

download_jar() {
    MODULE=$1
    CLASSIFIER=$2
    
    DIR="$M2_REPO/org/openjfx/javafx-$MODULE/$JFX_VERSION"
    mkdir -p "$DIR"
    
    if [ -z "$CLASSIFIER" ]; then
        FILE="javafx-$MODULE-$JFX_VERSION.jar"
    else
        FILE="javafx-$MODULE-$JFX_VERSION-$CLASSIFIER.jar"
    fi
    
    URL="$BASE_URL/javafx-$MODULE/$JFX_VERSION/$FILE"
    
    if [ ! -f "$DIR/$FILE" ]; then
        echo "Downloading $FILE..."
        curl -s -L "$URL" -o "$DIR/$FILE"
    else
        echo "$FILE already exists."
    fi
}

# Download Core Jars and Linux Natives
for MODULE in controls fxml base graphics; do
    download_jar "$MODULE" ""
    download_jar "$MODULE" "linux"
done

echo "Done! JavaFX 25 dependencies are installed."
echo "Please Right-Click project -> Maven -> Reload Project to refresh."
