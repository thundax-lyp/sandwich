#!/usr/bin/env bash

set -euo pipefail

SMOKE_ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SMOKE_REPO_DIR="$(cd "${SMOKE_ROOT_DIR}/../.." && pwd)"
SMOKE_TMP_FILES=()

smoke_cleanup() {
    if [ "${#SMOKE_TMP_FILES[@]}" -gt 0 ]; then
        rm -f "${SMOKE_TMP_FILES[@]}"
    fi
}

trap smoke_cleanup EXIT

smoke_load_env() {
    local env_file="${SANDWICH_SMOKE_ENV_FILE:-}"
    if [ -z "${env_file}" ] && [ -f "${SMOKE_REPO_DIR}/.env.smoke" ]; then
        env_file="${SMOKE_REPO_DIR}/.env.smoke"
    fi

    if [ -n "${env_file}" ]; then
        if [ ! -f "${env_file}" ]; then
            echo "[smoke] env file not found: ${env_file}" >&2
            exit 1
        fi
        set -a
        # shellcheck disable=SC1090
        . "${env_file}"
        set +a
    fi
}

smoke_defaults() {
    SANDWICH_ADMIN_BASE_URL="${SANDWICH_ADMIN_BASE_URL:-http://127.0.0.1:18080/admin-api}"
    SANDWICH_FRONT_BASE_URL="${SANDWICH_FRONT_BASE_URL:-http://127.0.0.1:18080/front-api}"
    SANDWICH_SMOKE_TIMEOUT="${SANDWICH_SMOKE_TIMEOUT:-10}"
    SANDWICH_SMOKE_TOKEN_HEADER="${SANDWICH_SMOKE_TOKEN_HEADER:-Access-Token}"
    SANDWICH_SMOKE_ACCESS_TOKEN="${SANDWICH_SMOKE_ACCESS_TOKEN:-}"
    SANDWICH_SMOKE_ADMIN_TOKEN="${SANDWICH_SMOKE_ADMIN_TOKEN:-${SANDWICH_SMOKE_ACCESS_TOKEN}}"
    SANDWICH_SMOKE_REQUIRE_AUTH="${SANDWICH_SMOKE_REQUIRE_AUTH:-false}"
    SANDWICH_SMOKE_STORAGE_UPLOAD="${SANDWICH_SMOKE_STORAGE_UPLOAD:-false}"
    SANDWICH_SMOKE_STORAGE_FILE="${SANDWICH_SMOKE_STORAGE_FILE:-}"
}

smoke_bootstrap() {
    smoke_load_env
    smoke_defaults
}

smoke_log() {
    echo "[smoke] $*"
}

smoke_fail() {
    echo "[smoke] FAIL: $*" >&2
    exit 1
}

smoke_require() {
    command -v "$1" >/dev/null 2>&1 || smoke_fail "missing command: $1"
}

smoke_url() {
    local base="${1%/}"
    local path="${2#/}"
    printf "%s/%s" "${base}" "${path}"
}

smoke_bool() {
    case "$1" in
        true | TRUE | yes | YES | 1)
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

smoke_http() {
    local method="$1"
    local url="$2"
    local body="${3:-}"
    local token="${4:-}"
    local body_file
    local status
    local -a curl_args

    body_file="$(mktemp "${TMPDIR:-/tmp}/sandwich-smoke.XXXXXX")"
    SMOKE_TMP_FILES+=("${body_file}")

    curl_args=(-sS -m "${SANDWICH_SMOKE_TIMEOUT}" -o "${body_file}" -w "%{http_code}" -X "${method}")
    curl_args+=(-H "Accept: application/json")
    if [ -n "${token}" ]; then
        curl_args+=(-H "${SANDWICH_SMOKE_TOKEN_HEADER}: ${token}")
    fi
    if [ -n "${body}" ]; then
        curl_args+=(-H "Content-Type: application/json" --data "${body}")
    fi

    smoke_log "${method} ${url}"
    if ! status="$(curl "${curl_args[@]}" "${url}")"; then
        cat "${body_file}" >&2 || true
        smoke_fail "request failed: ${method} ${url}"
    fi

    SMOKE_HTTP_STATUS="${status}"
    SMOKE_HTTP_BODY="$(cat "${body_file}")"
}

smoke_get() {
    smoke_http "GET" "$1" "" "${2:-}"
}

smoke_post() {
    smoke_http "POST" "$1" "${2:-}" "${3:-}"
}

smoke_expect_2xx() {
    local name="$1"
    case "${SMOKE_HTTP_STATUS}" in
        2*)
            smoke_log "OK ${name} (${SMOKE_HTTP_STATUS})"
            ;;
        *)
            echo "${SMOKE_HTTP_BODY}" >&2
            smoke_fail "${name} returned HTTP ${SMOKE_HTTP_STATUS}"
            ;;
    esac
}

smoke_expect_body() {
    local name="$1"
    if [ -z "${SMOKE_HTTP_BODY}" ]; then
        smoke_fail "${name} returned empty body"
    fi
}

smoke_require_admin_token() {
    if [ -n "${SANDWICH_SMOKE_ADMIN_TOKEN}" ]; then
        return 0
    fi

    if smoke_bool "${SANDWICH_SMOKE_REQUIRE_AUTH}"; then
        smoke_fail "SANDWICH_SMOKE_ADMIN_TOKEN or SANDWICH_SMOKE_ACCESS_TOKEN is required"
    fi

    smoke_log "SKIP authenticated smoke: token is not set"
    return 1
}

smoke_bootstrap
