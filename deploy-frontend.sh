#!/bin/bash
# Build and deploy the React web frontend

set -e

cd "$(dirname "$0")/web-app"

echo "Installing dependencies..."
npm install

echo "Building React application..."
npm run build

echo ""
echo "Build complete! Files are in web-app/build/"
echo "Deploy the build/ folder to any static hosting service."
echo ""
echo "For local serving:"
echo "  npx serve -s build -l 3000"
