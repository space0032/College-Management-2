#!/bin/bash

# Configuration
DB_NAME="college_db"
DB_USER="root"

if [ -z "$1" ]; then
    echo "Usage: ./restore.sh <backup_file_path>"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: File '$BACKUP_FILE' not found!"
    exit 1
fi

echo "⚠️  WARNING: This will OVERWRITE the '$DB_NAME' database."
read -r -p "Are you sure you want to continue? (y/N): " confirm

if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "Restore cancelled."
    exit 0
fi

echo "Restoring from $BACKUP_FILE..."
if mysql -u "$DB_USER" -p "$DB_NAME" < "$BACKUP_FILE"; then
    echo "✅ Restore successful!"
else
    echo "❌ Restore failed!"
    exit 1
fi
