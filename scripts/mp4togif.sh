#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 /path/to/input.mp4 /path/to/output.gif" >&2
  exit 1
}

die() {
  echo "Error: $*" >&2
  exit 1
}

input_file="${1:-}"
output_file="${2:-}"

[[ -n "$input_file" && -n "$output_file" ]] || usage

[[ -f "$input_file" ]] || die "'$input_file' is not a file"

# Validate input is a video file
if ! ffprobe -v error -select_streams v:0 -show_entries stream=codec_type -of csv=p=0 "$input_file" 2>/dev/null | grep -q "^video$"; then
  die "'$input_file' is not a valid video file"
fi

# Optional: override ffmpeg binary path
FFMPEG_BIN="${FFMPEG_BIN:-$(command -v ffmpeg 2>/dev/null || true)}"

if [[ -z "$FFMPEG_BIN" || ! -x "$FFMPEG_BIN" ]]; then
  command -v ffmpeg >/dev/null 2>&1 || die "ffmpeg is not installed or not executable"
  FFMPEG_BIN="$(command -v ffmpeg)"
fi

# Optional: override video filter parameters
FPS="${FPS:-30}"
SCALE="${SCALE:-1216:2688}"
FLAGS="${FLAGS:-lanczos}"
MAX_COLORS="${MAX_COLORS:-256}"
STATS_MODE="${STATS_MODE:-diff}"
DITHER="${DITHER:-none}"

# Create temp palette file
palette_file="$(mktemp -t gif_palette.XXXXXX.png)"
trap 'rm -f "$palette_file"' EXIT

echo "Generating palette for '$input_file'..."

"$FFMPEG_BIN" -y -i "$input_file" \
  -vf "fps=$FPS,scale=$SCALE:flags=$FLAGS,palettegen=max_colors=$MAX_COLORS:stats_mode=$STATS_MODE" \
  "$palette_file"

echo "Encoding GIF -> '$output_file'..."

"$FFMPEG_BIN" -y -i "$input_file" -i "$palette_file" \
  -lavfi "fps=$FPS,scale=$SCALE:flags=$FLAGS[x];[x][1:v]paletteuse=dither=$DITHER" \
  "$output_file"

echo "Done: '$output_file'"


# USAGE

#./mp4_to_gif.sh input.mp4 output.gif

## Override parameters
#FPS=60 SCALE=1920:1080 MAX_COLORS=128 ./mp4_to_gif.sh input.mp4 output.gif

## Override ffmpeg path
#FFMPEG_BIN=/opt/homebrew/bin/ffmpeg ./mp4_to_gif.sh input.mp4 output.gif