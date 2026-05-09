#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/pre-auth-session")"
smoke_expect_2xx "admin auth form"
smoke_expect_body "admin auth form"

smoke_log "auth smoke completed"
