#!/bin/bash

DIR="fastlane/metadata/android/en-US/changelogs"

# Find latest numeric version file
FILE=$(ls "$DIR"/*.txt 2>/dev/null | sed 's#.*/##' | sed 's/\.txt$//' | grep -E '^[0-9]+$' | sort -n | tail -1)

if [ -z "$FILE" ] || [ ! -f "$DIR/$FILE.txt" ]; then
  echo "No valid changelog file found"
  cat << EOF
No changelog available
EOF
  exit 0
fi

CHANGELOG_FILE="$DIR/$FILE.txt"
echo "Using changelog: $CHANGELOG_FILE"

# Extract version and date, then process with awk
awk '
NR==1 { version=$0; next }
NR==2 { date=$0; print "# " version " (" date ")"; print ""; next }
NR>=3 {
  gsub(/^[ \t]+|[ \t]+$/, "")
  if ($0 ~ /^\[NEW\]$/) { print "### NEW"; next }
  if ($0 ~ /^\[IMPROVED\]$/) { print "### IMPROVED"; next }
  if ($0 ~ /^\[FIXED\]$/) { print "### FIXED"; next }
  if ($0 ~ /^\[ISSUES\]$/) { print "### ISSUES"; next }
  if ($0 ~ /^\* /) { sub(/^\* /, "* "); print }
}
' "$CHANGELOG_FILE"
