#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"${SCRIPT_DIR}/smoke-admin-api-cache-sync.sh"
"${SCRIPT_DIR}/smoke-front-api-cache-sync.sh"
"${SCRIPT_DIR}/smoke-open-api-cache-sync.sh"

echo "[smoke] all cache sync smoke checks completed"
