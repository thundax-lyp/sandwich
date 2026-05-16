#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/smoke-common.sh
. "${SCRIPT_DIR}/lib/smoke-common.sh"

smoke_require curl

open_api_page_path="/api/submission/submission/page"
open_api_page_url="$(smoke_url "${SANDWICH_OPEN_BASE_URL}" "${open_api_page_path}")"

smoke_post "${open_api_page_url}" '{"pageNo":1,"pageSize":1}'
smoke_expect_status "open api unsigned request boundary" "401"
smoke_expect_body "open api unsigned request boundary"

if smoke_require_open_api_credentials; then
    smoke_require openssl
    smoke_require awk

    body='{"pageNo":1,"pageSize":1}'
    timestamp="$(date +%s)"
    nonce="smoke-${timestamp}-$$"
    content_sha256="$(smoke_sha256_hex "${body}")"
    canonical_path="${SANDWICH_OPEN_CONTEXT_PATH:-/open-api}${open_api_page_path}"
    canonical_request="$(printf "POST\n%s\n\n%s\n%s\n%s" \
        "${canonical_path}" \
        "${timestamp}" \
        "${nonce}" \
        "${content_sha256}")"
    signature="$(smoke_hmac_sha256_hex "${SANDWICH_SMOKE_OPEN_API_SECRET}" "${canonical_request}")"

    smoke_http_with_headers \
        "POST" \
        "${open_api_page_url}" \
        "${body}" \
        "X-Sandwish-Api-Key: ${SANDWICH_SMOKE_OPEN_API_KEY}" \
        "X-Sandwish-Timestamp: ${timestamp}" \
        "X-Sandwish-Nonce: ${nonce}" \
        "X-Sandwish-Content-SHA256: ${content_sha256}" \
        "X-Sandwish-Signature: ${signature}"
    smoke_expect_2xx "open api signed submission page"
    smoke_expect_body "open api signed submission page"
fi

smoke_log "open api smoke completed"
