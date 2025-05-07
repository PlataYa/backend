#!/bin/bash

# File containing the current version
VERSION_FILE="VERSION"

# Check if the version file exists
if [ ! -f "$VERSION_FILE" ]; then
  echo "0.0.0" > "$VERSION_FILE"
fi

# Read the current version
CURRENT_VERSION=$(cat "$VERSION_FILE")

# Split the version into major, minor, and patch
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Increment the version based on the argument
case "$1" in
  major)
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
    ;;
  minor)
    MINOR=$((MINOR + 1))
    PATCH=0
    ;;
  patch)
    PATCH=$((PATCH + 1))
    ;;
  *)
    echo "Usage: $0 {major|minor|patch}"
    exit 1
    ;;
esac

# Construct the new version
NEW_VERSION="$MAJOR.$MINOR.$PATCH"

# Update the version file
echo "$NEW_VERSION" > "$VERSION_FILE"

# Output the new version
echo "Updated version: $NEW_VERSION"