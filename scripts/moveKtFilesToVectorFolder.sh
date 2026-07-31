#!/bin/bash

# Define source and target directories
source_dir="/home/elnix/svg"
target_dir="/home/elnix/StudioProjects/Dragon-launcher-Horizons/ui/main/src/main/kotlin/org/elnix/dragonlauncher/ui/svg/vectors"

# Check if target directory exists; if not, ask user
if [ ! -d "$target_dir" ]; then
    read -p "Target directory does not exist. Create it? [y/N] " answer
    if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
        mkdir -p "$target_dir"
        echo "Created directory: $target_dir"
    else
        echo "Aborted: Target directory does not exist."
        exit 1
    fi
fi

# Find and move .kt files
find "$source_dir" -name "*.kt" | while read -r file; do
    filename=$(basename "$file")

    # Skip if file already exists in target
    if [ ! -e "$target_dir/$filename" ]; then
        mv "$file" "$target_dir/"
        echo "Moved: $file"
    else
        echo "Skipped (exists): $file"
    fi
done