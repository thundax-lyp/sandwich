#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"${SCRIPT_DIR}/smoke-auth.sh"
"${SCRIPT_DIR}/smoke-admin-api.sh"
"${SCRIPT_DIR}/smoke-front-api.sh"
"${SCRIPT_DIR}/smoke-storage.sh"

echo "[smoke] all smoke checks completed"
