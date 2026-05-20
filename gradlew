#!/usr/bin/env bash
# Gradle wrapper delegating to bundled local installation
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GRADLE_HOME="$SCRIPT_DIR/.gradle-bin/gradle-8.5"
exec "$GRADLE_HOME/bin/gradle" "$@"
