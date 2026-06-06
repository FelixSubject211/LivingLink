#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(grep '^projectDir=' "$SCRIPT_DIR/local.properties" | cut -d'=' -f2-)"

cd "$PROJECT_DIR" || exit 1

set -a
# shellcheck disable=SC1091
source "$PROJECT_DIR/.env"
set +a

./server/build/install/server/bin/server \
    | sed -u '/^kotlin-logging: initializing/d'