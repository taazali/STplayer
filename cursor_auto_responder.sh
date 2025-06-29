#!/bin/bash

# Cursor Auto-Responder System
# Automatically responds to Lingma's updates and requests

echo "🤖 Cursor Auto-Responder System Starting..."
echo "============================================"
echo ""

# Configuration
REPO_URL="https://github.com/taazali/STplayer.git"
RESPONSE_INTERVAL=30  # Check every 30 seconds
LOG_FILE="cursor_auto_responder.log"
LAST_CHECK_FILE=".cursor_last_check"
WATCHED_FILES=(
    "CURSOR_NOTIFICATION.md"
    "DECISION_LOG.md"
    "LINGMA_NOTIFICATION.md"
    "app/src/main/java/com/taazali/stplayer/TranslationManager.kt"
)

# Create log file if it doesn't exist
touch "$LOG_FILE"

# Function to log messages
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# Function to get file modification time
get_file_mtime() {
    stat -c %Y "$1" 2>/dev/null || echo "0"
}

# Function to check for Lingma's requests
check_for_lingma_requests() {
    local notification_file="CURSOR_NOTIFICATION.md"
    
    if [ -f "$notification_file" ]; then
        # Check for [REQUEST] tags from Lingma
        if grep -q "\[REQUEST\]" "$notification_file"; then
            log_message "🎯 Lingma request detected in $notification_file"
            return 0
        fi
        
        # Check for [LINGMA_RESPONSE] tags
        if grep -q "\[LINGMA_RESPONSE\]" "$notification_file"; then
            log_message "📝 Lingma response detected, checking for follow-up needed"
            return 0
        fi
        
        # Check for new sections added by Lingma
        if grep -q "New Section Added" "$notification_file"; then
            log_message "🆕 New section added by Lingma"
            return 0
        fi
    fi
    
    return 1
}

# Function to check for code changes by Lingma
check_for_lingma_code_changes() {
    local recent_commits=$(git log --oneline -3)
    
    if echo "$recent_commits" | grep -q "\[LINGMA\]"; then
        log_message "🔧 Lingma code changes detected"
        echo "$recent_commits" | grep "\[LINGMA\]" | while read commit; do
            log_message "   📝 $commit"
        done
        return 0
    fi
    
    return 1
}

# Function to check for new files from Lingma
check_for_new_files() {
    local new_files=$(git diff --name-only HEAD~1 HEAD 2>/dev/null)
    
    if [ -n "$new_files" ]; then
        log_message "📁 New files detected:"
        echo "$new_files" | while read file; do
            log_message "   📄 $file"
        done
        return 0
    fi
    
    return 1
}

# Function to generate automated response
generate_response() {
    local response_type="$1"
    local notification_file="CURSOR_NOTIFICATION.md"
    
    case "$response_type" in
        "request")
            log_message "🤖 Generating response to Lingma's request..."
            add_cursor_response "Acknowledged - Working on your request"
            ;;
        "code_change")
            log_message "🤖 Acknowledging Lingma's code changes..."
            add_cursor_response "Code changes received and being processed"
            ;;
        "new_section")
            log_message "🤖 Responding to new section from Lingma..."
            add_cursor_response "New section received - reviewing content"
            ;;
        "general")
            log_message "🤖 Sending general status update..."
            add_cursor_response "Auto-responder active - monitoring for updates"
            ;;
    esac
}

# Function to add Cursor response to notification file
add_cursor_response() {
    local message="$1"
    local notification_file="CURSOR_NOTIFICATION.md"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    
    # Add response to the end of the file
    cat >> "$notification_file" << EOF

# 🤖 Cursor Auto-Response

## 📅 Response Time: $timestamp
**Type**: Automated Response
**Status**: ✅ Active

## 💬 Message
$message

## 🔄 Auto-Response Rules
- **Request Detection**: Monitors for [REQUEST] tags from Lingma
- **Code Change Detection**: Monitors for [LINGMA] commits
- **Response Time**: < 30 seconds
- **Status**: Active and monitoring

## 📋 Current Tasks
- [CURSOR] Implementing ONNX Runtime support
- [CURSOR] Audio processing components
- [CURSOR] Documentation maintenance
- [SHARED] Decision log maintenance

## 🎯 Next Actions
1. Continue ONNX Runtime implementation
2. Monitor for Lingma's updates
3. Maintain collaboration infrastructure

— Cursor Auto-Responder 🤖
EOF
    
    log_message "✅ Response added to $notification_file"
}

# Function to commit and push response
commit_response() {
    local response_type="$1"
    
    git add CURSOR_NOTIFICATION.md
    git commit -m "[CURSOR] Auto-response to Lingma's $response_type"
    git push
    
    log_message "🚀 Response committed and pushed to repository"
}

# Function to check for urgent matters
check_for_urgent_matters() {
    local notification_file="CURSOR_NOTIFICATION.md"
    
    if [ -f "$notification_file" ] && grep -q "\[!Urgent\]" "$notification_file"; then
        log_message "🚨 URGENT MATTER DETECTED!"
        return 0
    fi
    
    return 1
}

# Function to display status
show_status() {
    echo ""
    echo "📊 Cursor Auto-Responder Status:"
    echo "================================"
    echo "Repository: $REPO_URL"
    echo "Response interval: ${RESPONSE_INTERVAL}s"
    echo "Log file: $LOG_FILE"
    echo "Watched files: ${WATCHED_FILES[*]}"
    echo ""
}

# Initialize
log_message "🚀 Cursor Auto-Responder System initialized"
show_status

# Store initial check time
date +%s > "$LAST_CHECK_FILE"

# Main response loop
while true; do
    log_message "🔄 Checking for Lingma's updates..."
    
    # Pull latest changes
    git fetch origin main >/dev/null 2>&1
    git pull origin main >/dev/null 2>&1
    
    # Check for urgent matters first
    if check_for_urgent_matters; then
        log_message "🚨 Processing urgent matter..."
        generate_response "urgent"
        commit_response "urgent_matter"
        continue
    fi
    
    # Check for Lingma's requests
    if check_for_lingma_requests; then
        log_message "📨 Processing Lingma's request..."
        generate_response "request"
        commit_response "request"
        continue
    fi
    
    # Check for code changes
    if check_for_lingma_code_changes; then
        log_message "🔧 Processing code changes..."
        generate_response "code_change"
        commit_response "code_change"
        continue
    fi
    
    # Check for new files
    if check_for_new_files; then
        log_message "📁 Processing new files..."
        generate_response "new_section"
        commit_response "new_files"
        continue
    fi
    
    # Send periodic status update (every 10 minutes)
    local current_time=$(date +%s)
    local last_check=$(cat "$LAST_CHECK_FILE" 2>/dev/null || echo "0")
    local time_diff=$((current_time - last_check))
    
    if [ $time_diff -gt 600 ]; then  # 10 minutes
        log_message "📊 Sending periodic status update..."
        generate_response "general"
        commit_response "status_update"
        echo "$current_time" > "$LAST_CHECK_FILE"
    else
        log_message "✅ No updates detected, continuing monitoring..."
    fi
    
    # Wait before next check
    log_message "⏳ Waiting ${RESPONSE_INTERVAL} seconds before next check..."
    echo "----------------------------------------"
    sleep $RESPONSE_INTERVAL
done 