#!/bin/bash

# Configuration
DB_NAME="college_db"
DB_USER="root"
BACKUP_DIR="backups"
DATE=$(date +%Y%m%d_%H%M%S)
FILENAME="$BACKUP_DIR/${DB_NAME}_backup_$DATE.sql"

# Ensure backup directory exists
mkdir -p "$BACKUP_DIR"

# Perform Backup
echo "Creating backup for database: $DB_NAME..."
# Note: Providing password on command line can be insecure, better to use .my.cnf
# For this setup, we assume user might need to enter password or has configured .my.cnf
mysqldump -u "$DB_USER" -p "$DB_NAME" > "$FILENAME"

if [ $? -eq 0 ]; then
    echo "✅ Backup successful: $FILENAME"
    echo "Size: $(du -h "$FILENAME" | cut -f1)"
else
    echo "❌ Backup failed!"
    rm -f "$FILENAME"
    exit 1
fi
