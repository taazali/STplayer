#!/bin/bash

# Cursor Auto-Sync Script
# Continuously syncs with GitHub to get updates from Lingma and other collaborators

echo "🔄 Cursor Auto-Sync System Starting..."
echo "======================================"
echo ""

# Configuration
REPO_URL="git@github.com:taazali/STplayer.git"
SYNC_INTERVAL=30  # Sync every 30 seconds
LOG_FILE="cursor_sync.log"
LAST_COMMIT_FILE=".cursor_last_commit"

# Create log file if it doesn't exist
touch "$LOG_FILE"

# Function to log messages
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Function to get current commit hash
get_current_commit() {
    git rev-parse HEAD 2>/dev/null || echo "none"
}

# Function to check for new commits
check_for_updates() {
    local current_commit=$(get_current_commit)
    local last_commit=""
    
    # Read last known commit
    if [ -f "$LAST_COMMIT_FILE" ]; then
        last_commit=$(cat "$LAST_COMMIT_FILE")
    fi
    
    # Fetch latest changes
    git fetch origin main >/dev/null 2>&1
    
    # Check if we're behind
    local behind_count=$(git rev-list --count HEAD..origin/main 2>/dev/null || echo "0")
    
    if [ "$behind_count" -gt 0 ]; then
        log_message "🆕 New updates detected! ($behind_count commits behind)"
        
        # Pull changes
        if git pull origin main >/dev/null 2>&1; then
            local new_commit=$(get_current_commit)
            log_message "✅ Successfully synced to commit: ${new_commit:0:8}"
            
            # Check for specific collaborators
            local recent_commits=$(git log --oneline -5)
            if echo "$recent_commits" | grep -q "Lingma\|lingma"; then
                log_message "🎯 Lingma's changes detected!"
                echo "$recent_commits" | head -3 | while read line; do
                    log_message "   📝 $line"
                done
            fi
            
            # Update last commit file
            echo "$new_commit" > "$LAST_COMMIT_FILE"
            
            # Check for new files
            local new_files=$(git diff --name-only "$last_commit" "$new_commit" 2>/dev/null)
            if [ -n "$new_files" ]; then
                log_message "📁 New/Modified files:"
                echo "$new_files" | while read file; do
                    log_message "   📄 $file"
                done
            fi
            
        else
            log_message "❌ Failed to pull changes"
        fi
    else
        log_message "✅ Repository is up to date"
    fi
}

# Function to check for specific files
check_for_specific_files() {
    local files_to_check=(
        "CURSOR_NOTIFICATION.md"
        "LINGMA_NOTIFICATION.md"
        "COLLABORATION_GUIDE.md"
        "DECISION_LOG.md"
        "EvaluationReport.md"
    )
    
    for file in "${files_to_check[@]}"; do
        if [ -f "$file" ]; then
            local file_age=$(($(date +%s) - $(stat -c %Y "$file" 2>/dev/null || echo 0)))
            if [ $file_age -lt 300 ]; then  # Less than 5 minutes old
                log_message "📋 New/Updated file detected: $file"
            fi
        fi
    done
}

# Function to display status
show_status() {
    echo ""
    echo "📊 Current Status:"
    echo "=================="
    echo "Repository: $REPO_URL"
    echo "Current commit: $(get_current_commit | cut -c1-8)"
    echo "Branch: $(git branch --show-current)"
    echo "Sync interval: ${SYNC_INTERVAL}s"
    echo "Log file: $LOG_FILE"
    echo ""
}

# Initialize
log_message "🚀 Cursor Auto-Sync System initialized"
show_status

# Store initial commit
get_current_commit > "$LAST_COMMIT_FILE"

# Main sync loop
while true; do
    log_message "🔄 Checking for updates..."
    
    # Check for updates
    check_for_updates
    
    # Check for specific collaboration files
    check_for_specific_files
    
    # Wait before next sync
    log_message "⏳ Waiting ${SYNC_INTERVAL} seconds before next sync..."
    echo "----------------------------------------"
    sleep $SYNC_INTERVAL
done 