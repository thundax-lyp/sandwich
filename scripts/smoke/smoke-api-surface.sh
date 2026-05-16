#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

admin_token="${SANDWICH_SMOKE_ADMIN_TOKEN:-}"

surface_post() {
    local name="$1"
    local base_url="$2"
    local path="$3"
    local body="${4:-{}}"
    local token="${5:-}"
    shift 5 || true

    smoke_post "$(smoke_url "${base_url}" "${path}")" "${body}" "${token}"
    smoke_expect_status_in "${name}" "$@"
    smoke_expect_body "${name}"
}

surface_get() {
    local name="$1"
    local base_url="$2"
    local path="$3"
    local token="${4:-}"
    shift 4 || true

    smoke_get "$(smoke_url "${base_url}" "${path}")" "${token}"
    smoke_expect_status_in "${name}" "$@"
    smoke_expect_body "${name}"
}

surface_multipart_post() {
    local name="$1"
    local base_url="$2"
    local path="$3"
    local token="${4:-}"
    shift 4 || true
    local body_file
    local status
    local -a curl_args

    body_file="$(mktemp "${TMPDIR:-/tmp}/sandwich-smoke.XXXXXX")"
    SMOKE_TMP_FILES+=("${body_file}")

    curl_args=(-sS -m "${SANDWICH_SMOKE_TIMEOUT}" -o "${body_file}" -w "%{http_code}" -X POST)
    curl_args+=(-H "Accept: application/json")
    if [ -n "${token}" ]; then
        curl_args+=(-H "${SANDWICH_SMOKE_TOKEN_HEADER}: ${token}")
    fi
    curl_args+=(-F "file=")

    smoke_log "POST $(smoke_url "${base_url}" "${path}")"
    if ! status="$(curl "${curl_args[@]}" "$(smoke_url "${base_url}" "${path}")")"; then
        cat "${body_file}" >&2 || true
        smoke_fail "request failed: POST $(smoke_url "${base_url}" "${path}")"
    fi

    SMOKE_HTTP_STATUS="${status}"
    SMOKE_HTTP_BODY="$(cat "${body_file}")"
    smoke_expect_status_in "${name}" "$@"
    smoke_expect_body "${name}"
}

surface_open_unsigned_post() {
    local name="$1"
    local path="$2"
    local body="${3:-{}}"

    smoke_post "$(smoke_url "${SANDWICH_OPEN_BASE_URL}" "${path}")" "${body}"
    smoke_expect_status_in "${name}" "401"
    smoke_expect_body "${name}"
}

smoke_log "probing admin api surface"
surface_get "admin auth captcha" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/captcha" "" "200"
surface_post "admin auth captcha refresh" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/captcha/refresh" "{}" "" "400"
surface_post "admin auth pre-auth-session" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/pre-auth-session" "" "" "200"
surface_post "admin auth pre-auth-session refresh" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/pre-auth-session/refresh" "{}" "" "400"
surface_post "admin auth login" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/login" "{}" "" "400"
surface_post "admin auth sms login" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/login/sms" "{}" "" "400"
surface_post "admin auth wecom login" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/login/wecom" "{}" "" "400"
surface_post "admin auth github login" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/login/github" "{}" "" "400"
surface_post "admin auth logout" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/logout" "{}" "" "400"
surface_post "admin auth token verify" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/token/verify" "{}" "" "400"
surface_post "admin auth oauth2 introspect" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/oauth2/introspect" "{}" "" "400"
surface_post "admin auth oauth2 userinfo" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/oauth2/userinfo" "{}" "" "400"
surface_post "admin auth token refresh" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/token/refresh" "{}" "" "400"
surface_post "admin auth oauth2 authorize" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/oauth2/authorize" "{}" "" "400"
surface_post "admin auth oauth2 decision" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/oauth2/decision" "{}" "" "400"
surface_post "admin auth oauth2 token" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/oauth2/token" "{}" "" "400"
surface_post "admin auth oauth2 revoke" "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/oauth2/revoke" "{}" "" "400"

surface_post "admin current user info" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/info" "" "${admin_token}" "200" "401" "403"
surface_post "admin current user info update" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/info/update" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin current user password update" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/password/update" "{}" "${admin_token}" "400" "401" "403"
surface_multipart_post "admin current user avatar upload" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/avatar/upload" "${admin_token}" "400" "401" "403"
surface_post "admin current user avatar delete" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/avatar/delete" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin current user menus" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/menus" "" "${admin_token}" "200" "401" "403"
surface_post "admin current user perms" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/perms" "" "${admin_token}" "200" "401" "403"

surface_post "admin sys user get" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/get" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys user list" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/list" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys user page" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys user create" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/create" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys user update" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/update" "{}" "${admin_token}" "400" "401" "403"
surface_multipart_post "admin sys user avatar upload" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/avatar/upload" "${admin_token}" "400" "401" "403"
surface_post "admin sys user avatar delete" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/avatar/delete" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys user avatar post" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/avatar" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys user enable" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/enable" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys user delete" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/delete" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys user check" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/check" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys user department tree" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/department/tree" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys user role list" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/role/list" "{}" "${admin_token}" "200" "400" "401" "403"
surface_get "admin sys user avatar get" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/user/avatar?id=0" "${admin_token}" "200" "400" "401" "403" "404"

for resource in role menu dict department; do
    base_path="/api/sys/${resource}"
    surface_post "admin sys ${resource} get" "${SANDWICH_ADMIN_BASE_URL}" "${base_path}/get" "{}" "${admin_token}" "400" "401" "403"
    surface_post "admin sys ${resource} list" "${SANDWICH_ADMIN_BASE_URL}" "${base_path}/list" "{}" "${admin_token}" "200" "400" "401" "403"
    surface_post "admin sys ${resource} create" "${SANDWICH_ADMIN_BASE_URL}" "${base_path}/create" "{}" "${admin_token}" "400" "401" "403"
    surface_post "admin sys ${resource} update" "${SANDWICH_ADMIN_BASE_URL}" "${base_path}/update" "{}" "${admin_token}" "400" "401" "403"
    surface_post "admin sys ${resource} delete" "${SANDWICH_ADMIN_BASE_URL}" "${base_path}/delete" "{}" "${admin_token}" "400" "401" "403"
done

surface_post "admin sys role enable" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/role/enable" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys role sort" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/role/sort" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys role menu tree" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/role/menu/tree" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys role user tree" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/role/user/tree" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys role user list" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/role/user/list" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys role user assign" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/role/user/assign" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys menu display" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/menu/display" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys menu tree" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/menu/tree" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys menu move" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/menu/move" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys dict page" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/dict/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys dict sort" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/dict/sort" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys department tree" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/department/tree" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin sys department move" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/department/move" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin sys log page" "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/log/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"

surface_post "admin storage object page" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_multipart_post "admin storage object upload" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/upload" "${admin_token}" "400" "401" "403"
surface_get "admin storage object content" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/0/content" "${admin_token}" "200" "400" "401" "403" "404"
surface_post "admin storage object delete" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/delete" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin storage object sort" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/sort" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin storage object tree" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/tree" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin storage multipart create" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/multipart-upload" "{}" "${admin_token}" "400" "401" "403"
surface_multipart_post "admin storage multipart part" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/multipart-upload/0/parts" "${admin_token}" "400" "401" "403" "404"
surface_post "admin storage multipart complete" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/multipart-upload/0/complete" "{}" "${admin_token}" "400" "401" "403" "404"
surface_post "admin storage multipart abort" "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/multipart-upload/0/abort" "{}" "${admin_token}" "200" "400" "401" "403" "404"

surface_post "admin submission create" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/create" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin submission page" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_post "admin submission get" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/get" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin submission change status" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/change-status" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin submission delete" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/delete" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin submission sort" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/sort" "{}" "${admin_token}" "400" "401" "403"
surface_multipart_post "admin submission image upload" "${SANDWICH_ADMIN_BASE_URL}" "/api/submission/submission/image/upload" "${admin_token}" "400" "401" "403"

surface_post "admin open client create" "${SANDWICH_ADMIN_BASE_URL}" "/api/open/client/create" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin open client update" "${SANDWICH_ADMIN_BASE_URL}" "/api/open/client/update" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin open client page" "${SANDWICH_ADMIN_BASE_URL}" "/api/open/client/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_post "admin open client get" "${SANDWICH_ADMIN_BASE_URL}" "/api/open/client/get" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin open client change status" "${SANDWICH_ADMIN_BASE_URL}" "/api/open/client/change-status" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin open client secret reset" "${SANDWICH_ADMIN_BASE_URL}" "/api/open/client/secret/reset" "{}" "${admin_token}" "400" "401" "403"

surface_post "admin audit meta" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/meta" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin audit history" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/history" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin audit detail" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/detail" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin audit object overview" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/object/overview" "{}" "${admin_token}" "400" "401" "403"
surface_post "admin audit object page" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/object/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_post "admin audit page" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/page" '{"pageNo":1,"pageSize":1}' "${admin_token}" "200" "400" "401" "403"
surface_post "admin audit options" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/options" "{}" "${admin_token}" "200" "400" "401" "403"
surface_post "admin audit fields" "${SANDWICH_ADMIN_BASE_URL}" "/api/audit/log/fields" "{}" "${admin_token}" "400" "401" "403"

smoke_log "probing front api surface"
surface_post "front auth pre-auth-session" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/pre-auth-session" "" "" "200"
surface_post "front auth pre-auth-session refresh" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/pre-auth-session/refresh" "{}" "" "400"
surface_post "front auth login" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/login" "{}" "" "400"
surface_post "front auth sms login" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/login/sms" "{}" "" "400"
surface_post "front auth token refresh" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/token/refresh" "{}" "" "400"
surface_post "front auth login status" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/login/status" "" "" "200"
surface_post "front auth check login" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/check-login" "" "" "200"
surface_post "front auth logout" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/session/logout" "{}" "" "400"
surface_post "front register account" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/register/account" "{}" "" "400"
surface_post "front register mobile code" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/register/mobile/code" "{}" "" "400"
surface_post "front register mobile" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/register/mobile" "{}" "" "400"
surface_post "front register email code" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/register/email/code" "{}" "" "400"
surface_post "front register email" "${SANDWICH_FRONT_BASE_URL}" "/api/auth/register/email" "{}" "" "400"

smoke_log "probing open api surface"
surface_open_unsigned_post "open submission create" "/api/submission/submission/create" "{}"
surface_open_unsigned_post "open submission page" "/api/submission/submission/page" '{"pageNo":1,"pageSize":1}'
surface_open_unsigned_post "open submission change status" "/api/submission/submission/change-status" "{}"
surface_open_unsigned_post "open submission image upload" "/api/submission/submission/image/upload" ""

smoke_log "api surface smoke completed"
