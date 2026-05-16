#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

smoke_post "$(smoke_url "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/pre-auth-session")"
smoke_expect_2xx "front mounted auth api"
smoke_expect_body "front mounted auth api"

smoke_post "$(smoke_url "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/check-login")"
smoke_expect_2xx "front check-login api"
smoke_expect_body "front check-login api"

smoke_log "front api smoke completed"
