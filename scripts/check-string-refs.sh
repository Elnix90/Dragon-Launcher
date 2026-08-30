#!/usr/bin/env bash
set -euo pipefail

STRINGS_XML="core/i18n/src/main/res/values/strings.xml"

if [[ ! -f "$STRINGS_XML" ]]; then
  echo "Warning: $STRINGS_XML not found, skipping string check"
  exit 0
fi

# Extract defined string names (default locale only)
DEFINED=$(grep -oP '<string\s+name="\K[^"]+' "$STRINGS_XML" | sort -u)

ERRORS=0

# Check R.string.xxx references in staged Kotlin files
for file in $(git diff --cached --name-only --diff-filter=ACM -- '*.kt'); do
  [[ -f "$file" ]] || continue
  REFS=$(grep -oP 'R\.string\.\K[a-zA-Z_]+' "$file" 2>/dev/null | sort -u)
  for ref in $REFS; do
    if ! echo "$DEFINED" | grep -qx "$ref"; then
      echo "ERROR: $file references R.string.$ref but it doesn't exist in strings.xml"
      ERRORS=$((ERRORS + 1))
    fi
  done
done

# Check @string/xxx references in staged XML files
for file in $(git diff --cached --name-only --diff-filter=ACM -- '*.xml'); do
  [[ -f "$file" ]] || continue
  REFS=$(grep -oP '@string/\K[a-zA-Z_]+' "$file" 2>/dev/null | sort -u)
  for ref in $REFS; do
    # Skip android: framework references
    if [[ "$ref" == android:* ]]; then continue; fi
    if ! echo "$DEFINED" | grep -qx "$ref"; then
      echo "ERROR: $file references @string/$ref but it doesn't exist in strings.xml"
      ERRORS=$((ERRORS + 1))
    fi
  done
done

if [[ $ERRORS -gt 0 ]]; then
  echo ""
  echo "$ERRORS string reference(s) not found in strings.xml"
  exit 1
fi

exit 0
