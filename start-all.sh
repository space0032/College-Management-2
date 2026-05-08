#!/bin/bash
# Start both backend and frontend servers at the same time

echo "Starting Backend API Server..."
./run-api.sh &
BACKEND_PID=$!

echo "Starting Frontend Web App..."
cd web-app
npm start &
FRONTEND_PID=$!

echo "Both servers are starting."
echo "Press Ctrl+C to stop both servers."

# Trap SIGINT (Ctrl+C) to terminate both background processes gracefully
trap "echo -e '\nShutting down both servers...'; kill $BACKEND_PID $FRONTEND_PID; exit 0" SIGINT SIGTERM

# Wait for background processes to finish
wait $BACKEND_PID $FRONTEND_PID
