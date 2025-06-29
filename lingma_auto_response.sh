#!/bin/bash

# Lingma Auto-Response Script
# Monitors for changes from Cursor and responds automatically

# Configuration
REPO_DIR="/home/user/AndroidStudioProjects/STplayer"
NOTIFICATION_FILE="$REPO_DIR/CURSOR_NOTIFICATION.md"
DECISION_LOG="$REPO_DIR/DECISION_LOG.md"
LAST_MODIFIED_FILE="$REPO_DIR/.lingma_last_modified"
SYNC_INTERVAL=30  # Check every 30 seconds
GIT_USER="Lingma-Auto"
GIT_EMAIL="lingma-auto@example.com"
CODE_CHANGE_MARKER="LINGMA_CODE_CHANGE"
COMMAND_EXECUTION_MARKER="LINGMA_AUTO_EXECUTE"

# Initialize last modified time
if [ ! -f "$LAST_MODIFIED_FILE" ]; then
    touch "$LAST_MODIFIED_FILE"
fi

# Function to configure git
configure_git() {
    cd "$REPO_DIR"
    git config user.name "$GIT_USER"
    git config user.email "$GIT_EMAIL"
}

# Function to apply code changes based on requests
apply_code_changes() {
    # Check for code change requests in CURSOR_NOTIFICATION.md
    if grep -q "\[$CODE_CHANGE_MARKER\]" "$NOTIFICATION_FILE"; then
        echo "$(date): Found code change request"
        
        # Extract file path and changes
        REQUEST_SECTION=$(grep -A 10 "\[$CODE_CHANGE_MARKER\]" "$NOTIFICATION_FILE")
        
        # Parse file path and changes
        FILE_PATH=$(echo "$REQUEST_SECTION" | grep "File:" | cut -d ':' -f2- | xargs)
        CHANGE_TYPE=$(echo "$REQUEST_SECTION" | grep "Type:" | cut -d ':' -f2 | xargs)
        DESCRIPTION=$(echo "$REQUEST_SECTION" | grep "Description:" | cut -d ':' -f2 | xargs)
        
        if [ -n "$FILE_PATH" ] && [ -f "$FILE_PATH" ]; then
            echo "$(date): Applying $CHANGE_TYPE to $FILE_PATH"
            echo "$(date): Description: $DESCRIPTION"
            
            # Create backup
            cp "$FILE_PATH" "$FILE_PATH.bak"
            
            # Apply changes (this is a simplified example)
            # In a real implementation, this would be more sophisticated
            LINE_NUMBER=$(grep -n "// ... existing code ..." "$FILE_PATH" | head -1 | cut -d ':' -f1)
            
            if [ -n "$LINE_NUMBER" ]; then
                # Extract the code block to insert
                CODE_TO_INSERT=$(echo "$REQUEST_SECTION" | sed -n '/```/,/```/p' | grep -v '```')
                
                if [ -n "$CODE_TO_INSERT" ]; then
                    # Replace the placeholder with actual code
                    sed -i "${LINE_NUMBER}s|// ... existing code ...|$CODE_TO_INSERT|" "$FILE_PATH"
                    
                    # Commit the change
                    cd "$REPO_DIR" && \
                        git add "$FILE_PATH" && \
                        git commit -m "[LINGMA] Automated code change: $DESCRIPTION" && \
                        git push origin main
                    
                    echo "$(date): Code change applied successfully"
                fi
            fi
        fi
        
        # Clear the request marker
        sed -i '/\['$CODE_CHANGE_MARKER'\]/,+10 d' "$NOTIFICATION_FILE"
    fi
}

# Function to check for Cursor requests
check_cursor_requests() {
    # Check if CURSOR_NOTIFICATION.md was modified
    if [ "$NOTIFICATION_FILE" -nt "$LAST_MODIFIED_FILE" ]; then
        echo "$(date): Detected change in CURSOR_NOTIFICATION.md"
        
        # Check for [REQUEST] tags
        if grep -q "\[REQUEST\]" "$NOTIFICATION_FILE"; then
            echo "$(date): Found Cursor request, preparing response..."
            
            # Add automated response
            sed -i '/\[REQUEST\]/a \[LINGMA_RESPONSE\]: Acknowledged - Working on your request' "$NOTIFICATION_FILE"
            
            # Commit and push the response
            cd "$REPO_DIR" && \
                git add "$NOTIFICATION_FILE" && \
                git commit -m "[LINGMA] Automated response to Cursor request" && \
                git push origin main
        fi
    fi
    
    # Update last modified time
    touch "$LAST_MODIFIED_FILE"
}

# Function to execute terminal commands
execute_terminal_commands() {
    # Check for auto-execute requests in CURSOR_NOTIFICATION.md
    if grep -q "\[$COMMAND_EXECUTION_MARKER\]" "$NOTIFICATION_FILE"; then
        echo "$(date): Found auto-execute request"
        
        # Extract command section
        REQUEST_SECTION=$(grep -A 10 "\[$COMMAND_EXECUTION_MARKER\]" "$NOTIFICATION_FILE")
        
        # Parse command details
        COMMAND=$(echo "$REQUEST_SECTION" | grep "Command:" | cut -d ':' -f2- | xargs)
        DESCRIPTION=$(echo "$REQUEST_SECTION" | grep "Description:" | cut -d ':' -f2 |xargs)
        
        if [ -n "$COMMAND" ]; then
            echo "$(date): Executing command: $COMMAND"
            echo "$(date): Description: $DESCRIPTION"
            
            # Execute the command
            eval "$COMMAND"
            
            # Record the execution result
            RESULT=$?
            if [ $RESULT -eq 0 ]; then
                echo "$(date): Command executed successfully"
                sed -i '/\['$COMMAND_EXECUTION_MARKER'\]/a \[LINGMA_RESPONSE\]: Successfully executed: $DESCRIPTION' "$NOTIFICATION_FILE"
            else
                echo "$(date): Command failed with error: $RESULT"
                sed -i '/\['$COMMAND_EXECUTION_MARKER'\]/a \[LINGMA_RESPONSE\]: Failed to execute: $DESCRIPTION (Error: $RESULT)' "$NOTIFICATION_FILE"
            fi
            
            # Commit and push the response
            cd "$REPO_DIR" && \
                git add "$NOTIFICATION_FILE" && \
                git commit -m "[LINGMA] Automated response: $DESCRIPTION" && \
                git push origin main
        fi
        
        # Clear the request marker
        sed -i '/\['$COMMAND_EXECUTION_MARKER'\]/,+10 d' "$NOTIFICATION_FILE"
    fi
}

# Function to handle decision log updates
handle_decision_log() {
    # If decision log was modified
    if [ "$DECISION_LOG" -nt "$LAST_MODIFIED_FILE" ]; then
        echo "$(date): Decision log updated, checking format..."
        # Add any decision log validation logic here
    fi
}

# Main loop
configure_git
echo "$(date): Starting Lingma auto-response system"
while true; do
    check_cursor_requests
    handle_decision_log
    apply_code_changes
    execute_terminal_commands
    sleep $SYNC_INTERVAL
done