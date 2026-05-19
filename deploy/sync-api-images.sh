#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TAG="${SANDWISH_IMAGE_TAG:-dev}"
REGISTRY="${SANDWISH_IMAGE_REGISTRY:-sandwish}"
JRE_IMAGE="${SANDWISH_JRE_IMAGE:-eclipse-temurin:8-jre}"
RECREATE_CONTAINERS="${SANDWISH_SYNC_RECREATE_CONTAINERS:-false}"
COMPOSE_FILE="${SANDWISH_COMPOSE_FILE:-$ROOT_DIR/deploy/docker-compose.yml}"
COMPOSE_ENV_FILE="${SANDWISH_COMPOSE_ENV_FILE:-$ROOT_DIR/deploy/.env}"

ADMIN_API_IMAGE="$REGISTRY/admin-api:$TAG"
FRONT_API_IMAGE="$REGISTRY/front-api:$TAG"
OPEN_API_IMAGE="$REGISTRY/open-api:$TAG"

usage() {
    cat <<EOF
Usage: $(basename "$0") [all|admin-api|front-api|open-api]...

Environment:
  SANDWISH_IMAGE_TAG                 Image tag. Default: dev
  SANDWISH_IMAGE_REGISTRY            Image registry prefix. Default: sandwish
  SANDWISH_JRE_IMAGE                 Base JRE image. Default: eclipse-temurin:8-jre
  SANDWISH_SYNC_RECREATE_CONTAINERS  Recreate Compose API containers. Default: false
  SANDWISH_COMPOSE_ENV_FILE          Compose env file. Default: deploy/.env
  SANDWISH_COMPOSE_FILE              Compose file. Default: deploy/docker-compose.yml
EOF
}

contains_module() {
    target="$1"
    shift
    for module in "$@"; do
        if [ "$module" = "$target" ]; then
            return 0
        fi
    done
    return 1
}

append_csv() {
    current="$1"
    value="$2"
    if [ -z "$current" ]; then
        printf '%s' "$value"
    else
        printf '%s,%s' "$current" "$value"
    fi
}

selected_modules=""
selected_services=""

if [ "$#" -eq 0 ]; then
    set -- all
fi

for target in "$@"; do
    case "$target" in
        all)
            selected_modules="$(append_csv "$selected_modules" "sandwish-admin-api")"
            selected_modules="$(append_csv "$selected_modules" "sandwish-front-api")"
            selected_modules="$(append_csv "$selected_modules" "sandwish-open-api")"
            selected_services="$selected_services sandwish-admin-api sandwish-front-api sandwish-open-api"
            ;;
        admin-api | sandwish-admin-api)
            selected_modules="$(append_csv "$selected_modules" "sandwish-admin-api")"
            selected_services="$selected_services sandwish-admin-api"
            ;;
        front-api | sandwish-front-api)
            selected_modules="$(append_csv "$selected_modules" "sandwish-front-api")"
            selected_services="$selected_services sandwish-front-api"
            ;;
        open-api | sandwish-open-api)
            selected_modules="$(append_csv "$selected_modules" "sandwish-open-api")"
            selected_services="$selected_services sandwish-open-api"
            ;;
        -h | --help)
            usage
            exit 0
            ;;
        *)
            usage >&2
            exit 2
            ;;
    esac
done

cd "$ROOT_DIR"

echo "==> Building API jars: $selected_modules"
mvn -q -pl "$selected_modules" -am -DskipTests package

if contains_module "sandwish-admin-api" $(printf '%s' "$selected_modules" | tr ',' ' '); then
    echo "==> Building Docker image: $ADMIN_API_IMAGE"
    docker build \
        -f "$ROOT_DIR/deploy/images/admin-api.Dockerfile" \
        --build-arg "SANDWISH_JRE_IMAGE=$JRE_IMAGE" \
        -t "$ADMIN_API_IMAGE" \
        "$ROOT_DIR"
fi

if contains_module "sandwish-front-api" $(printf '%s' "$selected_modules" | tr ',' ' '); then
    echo "==> Building Docker image: $FRONT_API_IMAGE"
    docker build \
        -f "$ROOT_DIR/deploy/images/front-api.Dockerfile" \
        --build-arg "SANDWISH_JRE_IMAGE=$JRE_IMAGE" \
        -t "$FRONT_API_IMAGE" \
        "$ROOT_DIR"
fi

if contains_module "sandwish-open-api" $(printf '%s' "$selected_modules" | tr ',' ' '); then
    echo "==> Building Docker image: $OPEN_API_IMAGE"
    docker build \
        -f "$ROOT_DIR/deploy/images/open-api.Dockerfile" \
        --build-arg "SANDWISH_JRE_IMAGE=$JRE_IMAGE" \
        -t "$OPEN_API_IMAGE" \
        "$ROOT_DIR"
fi

if [ "$RECREATE_CONTAINERS" = "true" ]; then
    if [ ! -f "$COMPOSE_ENV_FILE" ]; then
        echo "Missing Compose env file: $COMPOSE_ENV_FILE" >&2
        echo "Copy deploy/.env.example to deploy/.env or set SANDWISH_COMPOSE_ENV_FILE." >&2
        exit 1
    fi

    export SANDWISH_ADMIN_API_IMAGE="$ADMIN_API_IMAGE"
    export SANDWISH_FRONT_API_IMAGE="$FRONT_API_IMAGE"
    export SANDWISH_OPEN_API_IMAGE="$OPEN_API_IMAGE"

    echo "==> Recreating Compose API containers:$selected_services"
    docker compose \
        --env-file "$COMPOSE_ENV_FILE" \
        -f "$COMPOSE_FILE" \
        up -d --no-deps --force-recreate $selected_services
fi

echo "==> Done"
