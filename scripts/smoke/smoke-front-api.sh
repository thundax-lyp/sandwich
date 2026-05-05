#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

smoke_get "$(smoke_url "${SANDWICH_FRONT_BASE_URL}" "/auth/check-login")"
smoke_expect_2xx "front login status"
smoke_expect_body "front login status"

smoke_log "front api smoke completed"
