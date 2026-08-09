#!/usr/bin/env bash
set -euo pipefail

fail() {
  echo "ERROR: $1"
  exit 1
}

check_file() {
  local path="$1"
  [[ -f "$path" ]] || fail "Missing required documentation file: $path"
}

check_contains() {
  local file="$1"
  local text="$2"
  grep -Fq "$text" "$file" || fail "$file does not contain required text: $text"
}

echo "Checking documentation files..."

check_file "README.md"
check_file "CONTRIBUTING.md"
check_file "docs/user-guide.md"
check_file "docs/troubleshooting.md"
check_file "releases/changelog.md"
check_file "releases/templates/release-notes-template.md"
check_file "LICENSE"

check_contains "README.md" "docs/user-guide.md"
check_contains "README.md" "docs/troubleshooting.md"
check_contains "README.md" "releases/changelog.md"
check_contains "README.md" "CONTRIBUTING.md"

echo "Documentation check passed."
