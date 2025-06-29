#!/bin/bash

# STPlayer Git Sync Script
# Automatically commits and pushes changes to GitHub

# Usage: ./git_sync.sh "Commit message"

# Check if a commit message was provided
if [ -z "$1" ]; then
  echo "Error: Please provide a commit message"
  exit 1
fi

# Navigate to project directory
cd /home/user/AndroidStudioProjects/STplayer || { echo "Failed to navigate to project directory"; exit 1; }

# Pull latest changes from remote
tput setaf 2; echo "Pulling latest changes from GitHub..."; tput sgr0
git pull origin main --rebase

# Add all changed files
tput setaf 2; echo "Adding all changed files..."; tput sgr0
git add .

# Commit with provided message
tput setaf 2; echo "Committing changes..."; tput sgr0
git commit -m "$1"

# Push to GitHub
tput setaf 2; echo "Pushing to GitHub..."; tput sgr0
git push origin main

# Print success message
tput setaf 2; echo "Changes successfully pushed to GitHub!"; tput sgr0
tput setaf 2; echo "Cursor can now access the updated code."; tput sgr0