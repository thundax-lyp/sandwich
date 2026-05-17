#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl
smoke_require openssl
smoke_require awk

open_base_a="${SANDWICH_OPEN_API_A_BASE_URL:-}"
open_base_b="${SANDWICH_OPEN_API_B_BASE_URL:-}"
open_base_c="${SANDWICH_OPEN_API_C_BASE_URL:-}"

smoke_require_three_instance_urls "open api cache sync" "${open_base_a}" "${open_base_b}" "${open_base_c}"

if ! smoke_require_open_api_credentials; then
    smoke_matrix_skip "OpenApiNonceStore" "SANDWICH_SMOKE_OPEN_API_KEY or SANDWICH_SMOKE_OPEN_API_SECRET is not set"
    smoke_matrix_summary
    smoke_log "open api cache sync smoke completed"
    exit 0
fi

open_api_page_path="/api/submission/submission/page"
open_api_body='{"pageNo":1,"pageSize":1}'
open_api_content_sha256="$(smoke_sha256_hex "${open_api_body}")"

open_signed_page() {
    local base_url="$1"
    local nonce="$2"
    local name="$3"
    local expected_status="$4"
    local timestamp
    local canonical_path
    local canonical_request
    local signature

    timestamp="$(date +%s)"
    canonical_path="${SANDWICH_OPEN_CONTEXT_PATH:-/open-api}${open_api_page_path}"
    canonical_request="$(printf "POST\n%s\n\n%s\n%s\n%s" \
        "${canonical_path}" \
        "${timestamp}" \
        "${nonce}" \
        "${open_api_content_sha256}")"
    signature="$(smoke_hmac_sha256_hex "${SANDWICH_SMOKE_OPEN_API_SECRET}" "${canonical_request}")"

    smoke_http_with_headers \
        "POST" \
        "$(smoke_url "${base_url}" "${open_api_page_path}")" \
        "${open_api_body}" \
        "X-Sandwish-Api-Key: ${SANDWICH_SMOKE_OPEN_API_KEY}" \
        "X-Sandwish-Timestamp: ${timestamp}" \
        "X-Sandwish-Nonce: ${nonce}" \
        "X-Sandwish-Content-SHA256: ${open_api_content_sha256}" \
        "X-Sandwish-Signature: ${signature}"
    smoke_expect_status "${name}" "${expected_status}"
    smoke_expect_body "${name}"
}

open_verify_cycle() {
    local write_base="$1"
    local read_base_first="$2"
    local read_base_second="$3"
    local name="$4"
    local nonce

    nonce="cache-sync-${name}-$(date +%s)-$$"
    open_signed_page "${write_base}" "${nonce}" "${name} writer accepts nonce" "200"
    open_signed_page "${read_base_first}" "${nonce}" "${name} first reader rejects replayed nonce" "401"
    open_signed_page "${read_base_second}" "${nonce}" "${name} second reader rejects replayed nonce" "401"
}

open_verify_cycle "${open_base_a}" "${open_base_b}" "${open_base_c}" "A-to-B-C"
open_verify_cycle "${open_base_b}" "${open_base_c}" "${open_base_a}" "B-to-C-A"
open_verify_cycle "${open_base_c}" "${open_base_a}" "${open_base_b}" "C-to-A-B"
smoke_matrix_cover "OpenApiNonceStore" "signed nonce accepted once and rejected as replay on peer instances"
smoke_matrix_summary

smoke_log "open api cache sync smoke completed"
