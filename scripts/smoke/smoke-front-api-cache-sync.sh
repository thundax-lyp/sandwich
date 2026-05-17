#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl
smoke_require python3

front_base_a="${SANDWICH_FRONT_API_A_BASE_URL:-}"
front_base_b="${SANDWICH_FRONT_API_B_BASE_URL:-}"
front_base_c="${SANDWICH_FRONT_API_C_BASE_URL:-}"

smoke_require_three_instance_urls "front api cache sync" "${front_base_a}" "${front_base_b}" "${front_base_c}"

front_refresh_token() {
    smoke_json_value "data.refreshToken" 2>/dev/null || smoke_json_value "refreshToken"
}

front_create_pre_auth_session() {
    local base_url="$1"
    local name="$2"

    smoke_post "$(smoke_url "${base_url}" "/api/auth/session/pre-auth-session")"
    smoke_expect_2xx "${name} create pre-auth session"
    smoke_expect_body "${name} create pre-auth session"
    CACHE_SYNC_REFRESH_TOKEN="$(front_refresh_token)"
}

front_refresh_pre_auth_session() {
    local base_url="$1"
    local refresh_token="$2"
    local name="$3"

    smoke_post "$(smoke_url "${base_url}" "/api/auth/session/pre-auth-session/refresh")" \
        "{\"refreshToken\":\"${refresh_token}\"}"
    smoke_expect_2xx "${name} refresh pre-auth session"
    smoke_expect_body "${name} refresh pre-auth session"
    CACHE_SYNC_REFRESH_TOKEN="$(front_refresh_token)"
}

front_verify_cycle() {
    local write_base="$1"
    local read_base_first="$2"
    local read_base_second="$3"
    local name="$4"
    local refresh_token

    front_create_pre_auth_session "${write_base}" "${name} writer"
    refresh_token="${CACHE_SYNC_REFRESH_TOKEN}"
    front_refresh_pre_auth_session "${read_base_first}" "${refresh_token}" "${name} first reader"
    refresh_token="${CACHE_SYNC_REFRESH_TOKEN}"
    front_refresh_pre_auth_session "${read_base_second}" "${refresh_token}" "${name} second reader"
}

front_call_all_instances_with_token() {
    local path="$1"
    local token="$2"
    local name="$3"

    smoke_post "$(smoke_url "${front_base_a}" "${path}")" "" "${token}"
    smoke_expect_2xx "${name} on A"
    smoke_expect_body "${name} on A"
    smoke_post "$(smoke_url "${front_base_b}" "${path}")" "" "${token}"
    smoke_expect_2xx "${name} on B"
    smoke_expect_body "${name} on B"
    smoke_post "$(smoke_url "${front_base_c}" "${path}")" "" "${token}"
    smoke_expect_2xx "${name} on C"
    smoke_expect_body "${name} on C"
}

front_refresh_access_token() {
    local base_url="$1"
    local refresh_token="$2"
    local name="$3"

    smoke_post "$(smoke_url "${base_url}" "/api/auth/session/token/refresh")" \
        "{\"refreshToken\":\"${refresh_token}\"}"
    smoke_expect_2xx "${name} refresh access token"
    smoke_expect_body "${name} refresh access token"
    CACHE_SYNC_REFRESH_TOKEN="$(front_refresh_token)"
}

front_cover_auth_cache_matrix() {
    if [ -z "${SANDWICH_SMOKE_FRONT_ACCESS_TOKEN}" ]; then
        smoke_matrix_skip "PrincipalAuthSessionDaoImpl" "SANDWICH_SMOKE_FRONT_ACCESS_TOKEN is not set"
        smoke_matrix_skip "PrincipalAccessTokenDaoImpl" "SANDWICH_SMOKE_FRONT_ACCESS_TOKEN is not set"
    else
        front_call_all_instances_with_token \
            "/api/auth/session/check-login" \
            "${SANDWICH_SMOKE_FRONT_ACCESS_TOKEN}" \
            "front check login"
        smoke_matrix_cover "PrincipalAuthSessionDaoImpl" "front token resolved on A/B/C"
        smoke_matrix_cover "PrincipalAccessTokenDaoImpl" "front token resolved on A/B/C"
    fi

    if [ -z "${SANDWICH_SMOKE_FRONT_REFRESH_TOKEN}" ]; then
        smoke_matrix_skip "PrincipalRefreshTokenDaoImpl" "SANDWICH_SMOKE_FRONT_REFRESH_TOKEN is not set"
    else
        local refresh_token="${SANDWICH_SMOKE_FRONT_REFRESH_TOKEN}"
        front_refresh_access_token "${front_base_a}" "${refresh_token}" "front refresh token A"
        refresh_token="${CACHE_SYNC_REFRESH_TOKEN}"
        front_refresh_access_token "${front_base_b}" "${refresh_token}" "front refresh token B"
        refresh_token="${CACHE_SYNC_REFRESH_TOKEN}"
        front_refresh_access_token "${front_base_c}" "${refresh_token}" "front refresh token C"
        smoke_matrix_cover "PrincipalRefreshTokenDaoImpl" "front refresh token consumed across A/B/C"
    fi
}

front_verify_cycle "${front_base_a}" "${front_base_b}" "${front_base_c}" "A-to-B-C"
front_verify_cycle "${front_base_b}" "${front_base_c}" "${front_base_a}" "B-to-C-A"
front_verify_cycle "${front_base_c}" "${front_base_a}" "${front_base_b}" "C-to-A-B"
smoke_matrix_cover "PreAuthSessionDaoImpl" "front pre-auth refresh token created on A/B/C and consumed by peer instances"

front_cover_auth_cache_matrix
smoke_matrix_summary

smoke_log "front api cache sync smoke completed"
