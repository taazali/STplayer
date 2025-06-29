#!/bin/bash

# Start Cursor Auto-Responder System in background
if [ -f "cursor_auto_responder.sh" ]; then
    echo "Starting Cursor Auto-Responder System..."
    nohup ./cursor_auto_responder.sh > cursor_auto_responder.log 2>&1 &
    echo "Cursor Auto-Responder PID: $!"
else
    echo "Error: cursor_auto_responder.sh not found"
fi

# Start Git Sync Monitor in background
if [ -f "git_sync_monitor.sh" ]; then
    echo "Starting Git Sync Monitor..."
    nohup ./git_sync_monitor.sh > git_sync_monitor.log 2>&1 &
    echo "Git Sync Monitor PID: $!"
else
    echo "Info: git_sync_monitor.sh not found (this is normal if you haven't created it yet)"
fi

# Create a symlink to STplayer directory if it doesn't exist
if [ ! -d "~/STplayer" ]; then
    ln -s /home/user/AndroidStudioProjects/STplayer ~/STplayer
fi

# Change to STplayer directory
cd /home/user/AndroidStudioProjects/STplayer || { echo "Failed to navigate to STplayer directory"; exit 1; }

# Set up environment variables
export STPLAYER_ROOT=/home/user/AndroidStudioProjects/STplayer
export PATH="$PATH:$STPLAYER_ROOT"