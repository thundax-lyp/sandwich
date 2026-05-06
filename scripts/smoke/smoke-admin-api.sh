#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/form")"
smoke_expect_2xx "admin public api"
smoke_expect_body "admin public api"

if smoke_require_admin_token; then
    smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/info")" "" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "admin current user"
    smoke_expect_body "admin current user"
fi

smoke_log "admin api smoke completed"
