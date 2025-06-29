#!/bin/bash
while true; do
    echo "$(date): Checking for updates..."
    git fetch origin
    
    # Check if we're behind remote
    if [ "$(git rev-list HEAD...origin/main --count)" != "0" ]; then
        echo "Updates found! Attempting to pull changes..."
        
        # Check if branches have diverged
        if [ "$(git rev-list --left-right --count HEAD...origin/main | cut -f1)" != "0" ]; then
            echo "Branches diverged. Resetting to remote..."
            git reset --hard origin/main
            echo "Reset complete!"
        else
            echo "Pulling changes..."
            git pull origin main
            echo "Pull complete!"
        fi
        echo "Sync complete!"
    else
        echo "No updates found."
    fi
    sleep 30  # Check every 30 seconds
done
