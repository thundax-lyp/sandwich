#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

if ! smoke_require_admin_token; then
    smoke_log "storage smoke completed with authenticated checks skipped"
    exit 0
fi

smoke_post "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/assist/storage/treeData")" "" "${SANDWICH_SMOKE_ADMIN_TOKEN}"
smoke_expect_2xx "storage tree"
smoke_expect_body "storage tree"

if smoke_bool "${SANDWICH_SMOKE_STORAGE_UPLOAD}"; then
    storage_file="${SANDWICH_SMOKE_STORAGE_FILE}"
    if [ -z "${storage_file}" ]; then
        storage_file="$(mktemp "${TMPDIR:-/tmp}/sandwich-storage-smoke.XXXXXX.txt")"
        SMOKE_TMP_FILES+=("${storage_file}")
        printf "sandwich smoke\n" > "${storage_file}"
    elif [ ! -f "${storage_file}" ]; then
        smoke_fail "storage upload file not found: ${storage_file}"
    fi

    smoke_log "POST $(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/assist/storage/upload")"
    upload_body_file="$(mktemp "${TMPDIR:-/tmp}/sandwich-storage-upload.XXXXXX")"
    SMOKE_TMP_FILES+=("${upload_body_file}")
    upload_status="$(curl -sS -m "${SANDWICH_SMOKE_TIMEOUT}" \
        -o "${upload_body_file}" \
        -w "%{http_code}" \
        -H "Accept: application/json" \
        -H "${SANDWICH_SMOKE_TOKEN_HEADER}: ${SANDWICH_SMOKE_ADMIN_TOKEN}" \
        -F "file=@${storage_file};type=text/plain" \
        "$(smoke_url "${SANDWICH_ADMIN_BASE_URL}" "/api/assist/storage/upload")")"

    if [ "${upload_status#2}" = "${upload_status}" ]; then
        cat "${upload_body_file}" >&2
        smoke_fail "storage upload returned HTTP ${upload_status}"
    fi
    if grep -q '"error"[[:space:]]*:[[:space:]]*"[^"]' "${upload_body_file}"; then
        cat "${upload_body_file}" >&2
        smoke_fail "storage upload returned business error"
    fi
    smoke_log "OK storage upload (${upload_status})"
fi

smoke_log "storage smoke completed"
