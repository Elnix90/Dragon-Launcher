#!/usr/bin/env bash
# Usage: ./rename_images.sh /path/to/folder

dir="${1:-.}"
cd "$dir" || exit 1

n=1
for f in *.{png,jpg,jpeg,webp,gif,JPG,JPEG}; do
  [ -f "$f" ] || continue  # Skip if no files match
  ext="${f##*.}"
  mv -- "$f" "$n.$ext"
  echo "Renamed $f -> $n.$ext"
  n=$((n+1))
done
