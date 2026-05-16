#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ENV_FILE="${SANDWICH_LOAD_ENV_FILE:-}"

if [ -z "${ENV_FILE}" ] && [ -f "${REPO_DIR}/.env.load" ]; then
    ENV_FILE="${REPO_DIR}/.env.load"
elif [ -z "${ENV_FILE}" ] && [ -f "${REPO_DIR}/.env.smoke" ]; then
    ENV_FILE="${REPO_DIR}/.env.smoke"
fi

if [ -n "${ENV_FILE}" ]; then
    if [ ! -f "${ENV_FILE}" ]; then
        echo "[load] env file not found: ${ENV_FILE}" >&2
        exit 1
    fi
    set -a
    # shellcheck disable=SC1090
    . "${ENV_FILE}"
    set +a
fi

SANDWICH_LOAD_REPORT_DIR="${SANDWICH_LOAD_REPORT_DIR:-${REPO_DIR}/reports/load}"
case "${SANDWICH_LOAD_REPORT_DIR}" in
    /*) ;;
    *) SANDWICH_LOAD_REPORT_DIR="${REPO_DIR}/${SANDWICH_LOAD_REPORT_DIR}" ;;
esac
SANDWICH_LOAD_SUMMARY_JSON="${SANDWICH_LOAD_SUMMARY_JSON:-${SANDWICH_LOAD_REPORT_DIR}/k6-summary.json}"
SANDWICH_LOAD_REPORT_MD="${SANDWICH_LOAD_REPORT_MD:-${SANDWICH_LOAD_REPORT_DIR}/k6-report.md}"
mkdir -p "${SANDWICH_LOAD_REPORT_DIR}"
export SANDWICH_LOAD_SUMMARY_JSON
export SANDWICH_LOAD_REPORT_MD

SANDWICH_LOAD_K6_IMAGE="${SANDWICH_LOAD_K6_IMAGE:-sandwish/k6:dev}"
SANDWICH_LOAD_USE_LOCAL_K6="${SANDWICH_LOAD_USE_LOCAL_K6:-false}"
SANDWICH_LOAD_DOCKER_NETWORK="${SANDWICH_LOAD_DOCKER_NETWORK:-host}"

if [ "${SANDWICH_LOAD_USE_LOCAL_K6}" = "true" ] && command -v k6 >/dev/null 2>&1; then
    k6 run "${SCRIPT_DIR}/k6-api-read.js"
    exit 0
fi

if command -v docker >/dev/null 2>&1; then
    docker_network_args=()
    if [ -n "${SANDWICH_LOAD_DOCKER_NETWORK}" ]; then
        docker_network_args=(--network "${SANDWICH_LOAD_DOCKER_NETWORK}")
    fi

    docker run --rm \
        "${docker_network_args[@]}" \
        -e SANDWICH_PUBLIC_BASE_URL \
        -e SANDWICH_ADMIN_BASE_URL \
        -e SANDWICH_FRONT_BASE_URL \
        -e SANDWICH_OPEN_BASE_URL \
        -e SANDWICH_OPEN_CONTEXT_PATH \
        -e SANDWICH_SMOKE_ACCESS_TOKEN \
        -e SANDWICH_SMOKE_ADMIN_TOKEN \
        -e SANDWICH_SMOKE_TOKEN_HEADER \
        -e SANDWICH_SMOKE_OPEN_API_KEY \
        -e SANDWICH_SMOKE_OPEN_API_SECRET \
        -e SANDWICH_LOAD_STAGES \
        -e SANDWICH_LOAD_THINK_TIME_SECONDS \
        -e SANDWICH_LOAD_FAILED_THRESHOLD \
        -e SANDWICH_LOAD_P95_THRESHOLD \
        -e SANDWICH_LOAD_P99_THRESHOLD \
        -e SANDWICH_LOAD_SUMMARY_JSON=/reports/k6-summary.json \
        -e SANDWICH_LOAD_REPORT_MD=/reports/k6-report.md \
        -v "${SCRIPT_DIR}:/scripts:ro" \
        -v "${SANDWICH_LOAD_REPORT_DIR}:/reports" \
        "${SANDWICH_LOAD_K6_IMAGE}" run /scripts/k6-api-read.js
    exit 0
fi

echo "[load] missing docker; set SANDWICH_LOAD_USE_LOCAL_K6=true to use a local k6 binary" >&2
exit 1
