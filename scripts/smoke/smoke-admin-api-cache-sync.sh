#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl
smoke_require python3

admin_base_a="${SANDWICH_ADMIN_API_A_BASE_URL:-}"
admin_base_b="${SANDWICH_ADMIN_API_B_BASE_URL:-}"
admin_base_c="${SANDWICH_ADMIN_API_C_BASE_URL:-}"

smoke_require_three_instance_urls "admin api cache sync" "${admin_base_a}" "${admin_base_b}" "${admin_base_c}"

admin_create_pre_auth_session() {
    local base_url="$1"
    local name="$2"

    smoke_post "$(smoke_url "${base_url}" "/api/auth/session/pre-auth-session")"
    smoke_expect_2xx "${name} create pre-auth session"
    smoke_expect_body "${name} create pre-auth session"
    CACHE_SYNC_REFRESH_TOKEN="$(smoke_json_value "data.refreshToken")"
}

admin_refresh_pre_auth_session() {
    local base_url="$1"
    local refresh_token="$2"
    local name="$3"

    smoke_post "$(smoke_url "${base_url}" "/api/auth/session/pre-auth-session/refresh")" \
        "{\"refreshToken\":\"${refresh_token}\"}"
    smoke_expect_2xx "${name} refresh pre-auth session"
    smoke_expect_body "${name} refresh pre-auth session"
    CACHE_SYNC_REFRESH_TOKEN="$(smoke_json_value "data.refreshToken")"
}

admin_verify_cycle() {
    local write_base="$1"
    local read_base_first="$2"
    local read_base_second="$3"
    local name="$4"
    local refresh_token

    admin_create_pre_auth_session "${write_base}" "${name} writer"
    refresh_token="${CACHE_SYNC_REFRESH_TOKEN}"
    admin_refresh_pre_auth_session "${read_base_first}" "${refresh_token}" "${name} first reader"
    refresh_token="${CACHE_SYNC_REFRESH_TOKEN}"
    admin_refresh_pre_auth_session "${read_base_second}" "${refresh_token}" "${name} second reader"
}

admin_call_all_instances() {
    local path="$1"
    local body="$2"
    local name="$3"

    smoke_post "$(smoke_url "${admin_base_a}" "${path}")" "${body}" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "${name} on A"
    smoke_expect_body "${name} on A"
    smoke_post "$(smoke_url "${admin_base_b}" "${path}")" "${body}" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "${name} on B"
    smoke_expect_body "${name} on B"
    smoke_post "$(smoke_url "${admin_base_c}" "${path}")" "${body}" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "${name} on C"
    smoke_expect_body "${name} on C"
}

admin_cover_auth_cache_matrix() {
    if ! smoke_require_admin_token; then
        smoke_matrix_skip "PrincipalAuthSessionDaoImpl" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "PrincipalAccessTokenDaoImpl" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "PrincipalRefreshTokenDaoImpl" "login/refresh fixture is not set"
        smoke_matrix_skip "UserCacheSupport" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "RoleCacheSupport" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "MenuCacheSupport" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "DepartmentCacheSupport" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "DictCacheSupport" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "StorageCacheSupport" "SANDWICH_SMOKE_ADMIN_TOKEN is not set"
        smoke_matrix_skip "SmsValidateCodeDaoImpl" "safe sms fixture is not set"
        return 0
    fi

    admin_call_all_instances "/api/sys/current-user/info" "" "current user info"
    smoke_matrix_cover "PrincipalAuthSessionDaoImpl" "admin token resolved on A/B/C"
    smoke_matrix_cover "PrincipalAccessTokenDaoImpl" "admin token resolved on A/B/C"
    smoke_matrix_cover "UserCacheSupport" "current user loaded on A/B/C"

    smoke_matrix_skip "PrincipalRefreshTokenDaoImpl" "login/refresh fixture is not set"

    if [ -n "${SANDWICH_CACHE_SYNC_USER_ID}" ]; then
        admin_call_all_instances "/api/sys/user/get" "{\"id\":\"${SANDWICH_CACHE_SYNC_USER_ID}\"}" "user cache by id"
        smoke_matrix_cover "UserCacheSupport" "SANDWICH_CACHE_SYNC_USER_ID=${SANDWICH_CACHE_SYNC_USER_ID} read on A/B/C"
    fi
    if [ -n "${SANDWICH_CACHE_SYNC_ROLE_ID}" ]; then
        admin_call_all_instances "/api/sys/role/get" "{\"id\":\"${SANDWICH_CACHE_SYNC_ROLE_ID}\"}" "role cache by id"
        smoke_matrix_cover "RoleCacheSupport" "SANDWICH_CACHE_SYNC_ROLE_ID=${SANDWICH_CACHE_SYNC_ROLE_ID} read on A/B/C"
    else
        smoke_matrix_skip "RoleCacheSupport" "SANDWICH_CACHE_SYNC_ROLE_ID is not set"
    fi
    if [ -n "${SANDWICH_CACHE_SYNC_MENU_ID}" ]; then
        admin_call_all_instances "/api/sys/menu/get" "{\"id\":\"${SANDWICH_CACHE_SYNC_MENU_ID}\"}" "menu cache by id"
        smoke_matrix_cover "MenuCacheSupport" "SANDWICH_CACHE_SYNC_MENU_ID=${SANDWICH_CACHE_SYNC_MENU_ID} read on A/B/C"
    else
        smoke_matrix_skip "MenuCacheSupport" "SANDWICH_CACHE_SYNC_MENU_ID is not set"
    fi
    if [ -n "${SANDWICH_CACHE_SYNC_DEPARTMENT_ID}" ]; then
        admin_call_all_instances "/api/sys/department/get" "{\"id\":\"${SANDWICH_CACHE_SYNC_DEPARTMENT_ID}\"}" "department cache by id"
        smoke_matrix_cover "DepartmentCacheSupport" "SANDWICH_CACHE_SYNC_DEPARTMENT_ID=${SANDWICH_CACHE_SYNC_DEPARTMENT_ID} read on A/B/C"
    else
        smoke_matrix_skip "DepartmentCacheSupport" "SANDWICH_CACHE_SYNC_DEPARTMENT_ID is not set"
    fi
    if [ -n "${SANDWICH_CACHE_SYNC_DICT_ID}" ]; then
        admin_call_all_instances "/api/sys/dict/get" "{\"id\":\"${SANDWICH_CACHE_SYNC_DICT_ID}\"}" "dict cache by id"
        smoke_matrix_cover "DictCacheSupport" "SANDWICH_CACHE_SYNC_DICT_ID=${SANDWICH_CACHE_SYNC_DICT_ID} read on A/B/C"
    else
        smoke_matrix_skip "DictCacheSupport" "SANDWICH_CACHE_SYNC_DICT_ID is not set"
    fi
    if [ -n "${SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID}" ]; then
        smoke_get "$(smoke_url "${admin_base_a}" "/api/storage/object/${SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID}/content")" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
        if [ "${SMOKE_HTTP_STATUS}" != "200" ]; then
            smoke_matrix_skip "StorageCacheSupport" "SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID did not return 200 on A"
        else
            smoke_log "OK storage content on A (${SMOKE_HTTP_STATUS})"
            smoke_get "$(smoke_url "${admin_base_b}" "/api/storage/object/${SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID}/content")" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
            smoke_expect_status "storage content on B" "200"
            smoke_get "$(smoke_url "${admin_base_c}" "/api/storage/object/${SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID}/content")" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
            smoke_expect_status "storage content on C" "200"
            smoke_matrix_cover "StorageCacheSupport" "SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID=${SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID} read on A/B/C"
        fi
    else
        smoke_matrix_skip "StorageCacheSupport" "SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID is not set"
    fi

    smoke_matrix_skip "SmsValidateCodeDaoImpl" "safe sms fixture is not set"
}

admin_verify_cycle "${admin_base_a}" "${admin_base_b}" "${admin_base_c}" "A-to-B-C"
admin_verify_cycle "${admin_base_b}" "${admin_base_c}" "${admin_base_a}" "B-to-C-A"
admin_verify_cycle "${admin_base_c}" "${admin_base_a}" "${admin_base_b}" "C-to-A-B"
smoke_matrix_cover "PreAuthSessionDaoImpl" "admin pre-auth refresh token created on A/B/C and consumed by peer instances"

admin_cover_auth_cache_matrix
smoke_matrix_summary

smoke_log "admin api cache sync smoke completed"
