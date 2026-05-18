#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_bootstrap
smoke_require curl

check_public_block() {
    local name="$1"
    local base_url="$2"

    smoke_get "$(smoke_url "${base_url}" "/actuator/health")"
    smoke_expect_status "${name} public health blocked" "404"

    smoke_get "$(smoke_url "${base_url}" "/swagger-ui.html")"
    smoke_expect_status "${name} public swagger ui blocked" "404"

    smoke_get "$(smoke_url "${base_url}" "/v2/api-docs")"
    smoke_expect_status "${name} public swagger docs blocked" "404"
}

check_internal_health() {
    local name="$1"
    local context_path="$2"

    smoke_get "$(smoke_url "${SANDWICH_INTERNAL_BASE_URL}" "${context_path}/actuator/health")"
    smoke_expect_2xx "${name} internal health"
    smoke_expect_body "${name} internal health"
    if [[ "${SMOKE_HTTP_BODY}" != *"UP"* ]]; then
        echo "${SMOKE_HTTP_BODY}" >&2
        smoke_fail "${name} internal health did not report UP"
    fi
}

check_internal_swagger() {
    local name="$1"
    local context_path="$2"

    if ! smoke_bool "${SANDWICH_SMOKE_SWAGGER_ENABLED}"; then
        smoke_log "SKIP ${name} internal swagger disabled by SANDWICH_SMOKE_SWAGGER_ENABLED"
        return
    fi

    smoke_get "$(smoke_url "${SANDWICH_INTERNAL_BASE_URL}" "${context_path}/swagger-ui.html")"
    smoke_expect_2xx "${name} internal swagger ui"

    smoke_get "$(smoke_url "${SANDWICH_INTERNAL_BASE_URL}" "${context_path}/v2/api-docs")"
    smoke_expect_2xx "${name} internal swagger docs"
    smoke_expect_body "${name} internal swagger docs"
}

check_public_block "admin-api" "${SANDWICH_ADMIN_BASE_URL}"
check_public_block "front-api" "${SANDWICH_FRONT_BASE_URL}"
check_public_block "open-api" "${SANDWICH_OPEN_BASE_URL}"

check_internal_health "admin-api" "/admin-api"
check_internal_health "front-api" "/front-api"
check_internal_health "open-api" "/open-api"

check_internal_swagger "admin-api" "/admin-api"
check_internal_swagger "front-api" "/front-api"
check_internal_swagger "open-api" "/open-api"

smoke_log "observability smoke completed"
