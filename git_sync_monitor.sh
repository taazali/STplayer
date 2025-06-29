#!/bin/bash

# Git Sync Monitor Script
# Monitors for changes and automatically syncs with GitHub

# Configuration
REPO_DIR="/home/user/AndroidStudioProjects/STplayer"
COMMIT_MESSAGE="[AUTO] Periodic sync: $(date '+%Y-%m-%d %H:%M:%S')"
CHECK_INTERVAL=30  # Check every 30 seconds
LOG_FILE="$REPO_DIR/git_sync_monitor.log"

# Function to log messages
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Initialize log file
log_message "🚀 Git Sync Monitor Starting..."

# Main monitoring loop
while true; do
    # Navigate to repo directory
    cd "$REPO_DIR" || { log_message "❌ Failed to navigate to repo directory"; exit 1; }
    
    # Pull latest changes from remote
    if git pull origin main >/dev/null 2>&1; then
        log_message "✅ Successfully pulled latest changes from GitHub"
    else
        log_message "❌ Failed to pull changes from GitHub"
    fi
    
    # Check for changes
    if [ -n "$(git status -s)" ]; then
        log_message "🔄 Changes detected, syncing with GitHub..."
        
        # Add all changes
        git add .
        
        # Commit with timestamp
        git commit -m "$COMMIT_MESSAGE"
        
        # Push to GitHub
        if git push origin main; then
            log_message "✅ Changes successfully pushed to GitHub"
        else
            log_message "❌ Failed to push changes to GitHub"
        fi
    else
        log_message "✅ No changes detected, continuing monitoring..."
    fi
    
    # Wait before next check
    sleep $CHECK_INTERVAL
done