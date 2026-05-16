#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/auth/session/pre-auth-session")"
smoke_expect_2xx "admin mounted auth api"
smoke_expect_body "admin mounted auth api"

if smoke_require_admin_token; then
    smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/info")" "" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "admin current user"
    smoke_expect_body "admin current user"

    smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/menus")" "" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "admin current user menus"
    smoke_expect_body "admin current user menus"

    smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/current-user/perms")" "" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "admin current user permissions"
    smoke_expect_body "admin current user permissions"

    smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/sys/dict/page")" '{"pageNo":1,"pageSize":1}' "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "admin system dictionary page"
    smoke_expect_body "admin system dictionary page"

    smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/tree")" "" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
    smoke_expect_2xx "admin storage object tree"
    smoke_expect_body "admin storage object tree"

    if smoke_bool "${SANDWICH_SMOKE_STORAGE_UPLOAD}"; then
        storage_file="${SANDWICH_SMOKE_STORAGE_FILE}"
        if [ -z "${storage_file}" ]; then
            storage_file="$(mktemp "${TMPDIR:-/tmp}/sandwich-storage-smoke.XXXXXX.txt")"
            SMOKE_TMP_FILES+=("${storage_file}")
            printf "sandwich smoke\n" > "${storage_file}"
        elif [ ! -f "${storage_file}" ]; then
            smoke_fail "storage upload file not found: ${storage_file}"
        fi

        smoke_log "POST $(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/upload")"
        upload_body_file="$(mktemp "${TMPDIR:-/tmp}/sandwich-storage-upload.XXXXXX")"
        SMOKE_TMP_FILES+=("${upload_body_file}")
        upload_status="$(curl -sS -m "${SANDWICH_SMOKE_TIMEOUT}" \
            -o "${upload_body_file}" \
            -w "%{http_code}" \
            -H "Accept: application/json" \
            -H "${SANDWICH_SMOKE_TOKEN_HEADER}: ${SANDWICH_SMOKE_ADMIN_TOKEN}" \
            -F "file=@${storage_file};type=text/plain" \
            "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/storage/object/upload")")"

        if [ "${upload_status#2}" = "${upload_status}" ]; then
            cat "${upload_body_file}" >&2
            smoke_fail "admin storage upload returned HTTP ${upload_status}"
        fi
        if grep -q '"error"[[:space:]]*:[[:space:]]*"[^"]' "${upload_body_file}"; then
            cat "${upload_body_file}" >&2
            smoke_fail "admin storage upload returned business error"
        fi
        smoke_log "OK admin storage upload (${upload_status})"
    fi
fi

smoke_log "admin api smoke completed"
