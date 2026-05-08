#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(grep '^projectDir=' "$SCRIPT_DIR/local.properties" | cut -d'=' -f2-)"

cd "$PROJECT_DIR" || exit 1

./build/install/livinglink/bin/livinglink | sed -u '/^kotlin-logging: initializing/d'