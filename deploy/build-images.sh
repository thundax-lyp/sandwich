#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TAG="${SANDWISH_IMAGE_TAG:-dev}"
REGISTRY="${SANDWISH_IMAGE_REGISTRY:-sandwish}"
NPM_INSTALL_MODE="${SANDWISH_NPM_INSTALL_MODE:-ci}"
OUTPUT_DIR="${SANDWISH_IMAGE_OUTPUT_DIR:-$ROOT_DIR/deploy/image-files}"
PULL_INFRA_IMAGES="${SANDWISH_PULL_INFRA_IMAGES:-false}"

JRE_IMAGE="${SANDWISH_JRE_IMAGE:-eclipse-temurin:8-jre}"
NGINX_IMAGE="${SANDWISH_NGINX_IMAGE:-nginx:1.27-alpine}"
MYSQL_SOURCE_IMAGE="${SANDWISH_MYSQL_SOURCE_IMAGE:-mysql:8.4}"
REDIS_SOURCE_IMAGE="${SANDWISH_REDIS_SOURCE_IMAGE:-redis:7.4-alpine}"
ROCKETMQ_SOURCE_IMAGE="${SANDWISH_ROCKETMQ_SOURCE_IMAGE:-apache/rocketmq:5.4.0}"
MINIO_SOURCE_IMAGE="${SANDWISH_MINIO_SOURCE_IMAGE:-minio/minio:RELEASE.2025-02-28T09-55-16Z}"
MINIO_MC_SOURCE_IMAGE="${SANDWISH_MINIO_MC_SOURCE_IMAGE:-minio/mc:RELEASE.2025-02-21T16-00-46Z}"
K6_SOURCE_IMAGE="${SANDWISH_K6_SOURCE_IMAGE:-grafana/k6:1.3.0}"

ADMIN_API_IMAGE="$REGISTRY/admin-api:$TAG"
FRONT_API_IMAGE="$REGISTRY/front-api:$TAG"
OPEN_API_IMAGE="$REGISTRY/open-api:$TAG"
NGINX_RUNTIME_IMAGE="$REGISTRY/nginx:$TAG"
MYSQL_IMAGE="$REGISTRY/mysql:8.4"
REDIS_IMAGE="$REGISTRY/redis:7.4-alpine"
ROCKETMQ_IMAGE="$REGISTRY/rocketmq:5.4.0"
MINIO_IMAGE="$REGISTRY/minio:RELEASE.2025-02-28T09-55-16Z"
MINIO_MC_IMAGE="$REGISTRY/minio-mc:RELEASE.2025-02-21T16-00-46Z"
K6_IMAGE="$REGISTRY/k6:$TAG"

cd "$ROOT_DIR"

echo "==> Building API jars"
mvn -q -pl sandwish-admin-api,sandwish-front-api,sandwish-open-api -am -DskipTests package

echo "==> Building admin web"
cd "$ROOT_DIR/sandwish-admin-web"
if [ "$NPM_INSTALL_MODE" = "ci" ]; then
    npm ci
else
    npm install
fi
npm run build
cd "$ROOT_DIR"

echo "==> Building Docker image: $REGISTRY/admin-api:$TAG"
docker build \
    -f "$ROOT_DIR/deploy/images/admin-api.Dockerfile" \
    --build-arg "SANDWISH_JRE_IMAGE=$JRE_IMAGE" \
    -t "$ADMIN_API_IMAGE" \
    "$ROOT_DIR"

echo "==> Building Docker image: $REGISTRY/front-api:$TAG"
docker build \
    -f "$ROOT_DIR/deploy/images/front-api.Dockerfile" \
    --build-arg "SANDWISH_JRE_IMAGE=$JRE_IMAGE" \
    -t "$FRONT_API_IMAGE" \
    "$ROOT_DIR"

echo "==> Building Docker image: $REGISTRY/open-api:$TAG"
docker build \
    -f "$ROOT_DIR/deploy/images/open-api.Dockerfile" \
    --build-arg "SANDWISH_JRE_IMAGE=$JRE_IMAGE" \
    -t "$OPEN_API_IMAGE" \
    "$ROOT_DIR"

echo "==> Building Docker image: $REGISTRY/nginx:$TAG"
docker build \
    -f "$ROOT_DIR/deploy/images/nginx.Dockerfile" \
    --build-arg "SANDWISH_NGINX_IMAGE=$NGINX_IMAGE" \
    -t "$NGINX_RUNTIME_IMAGE" \
    "$ROOT_DIR"

echo "==> Building Docker image: $REGISTRY/k6:$TAG"
docker build \
    -f "$ROOT_DIR/deploy/images/k6.Dockerfile" \
    --build-arg "SANDWISH_K6_IMAGE=$K6_SOURCE_IMAGE" \
    -t "$K6_IMAGE" \
    "$ROOT_DIR"

ensure_infra_image() {
    source_image="$1"
    target_image="$2"

    if [ "$PULL_INFRA_IMAGES" = "true" ]; then
        docker pull "$source_image"
    fi

    if docker image inspect "$source_image" >/dev/null 2>&1; then
        docker tag "$source_image" "$target_image"
        return
    fi

    if docker image inspect "$target_image" >/dev/null 2>&1; then
        echo "==> Reusing existing Docker image: $target_image"
        return
    fi

    echo "Missing Docker image: $source_image or $target_image" >&2
    echo "Set SANDWISH_PULL_INFRA_IMAGES=true when the network can pull images." >&2
    exit 1
}

if [ "$PULL_INFRA_IMAGES" = "true" ]; then
    echo "==> Pulling infrastructure images"
else
    echo "==> Skipping infrastructure image pull"
fi

echo "==> Preparing infrastructure images in $REGISTRY/*"
ensure_infra_image "$MYSQL_SOURCE_IMAGE" "$MYSQL_IMAGE"
ensure_infra_image "$REDIS_SOURCE_IMAGE" "$REDIS_IMAGE"
ensure_infra_image "$ROCKETMQ_SOURCE_IMAGE" "$ROCKETMQ_IMAGE"
ensure_infra_image "$MINIO_SOURCE_IMAGE" "$MINIO_IMAGE"
ensure_infra_image "$MINIO_MC_SOURCE_IMAGE" "$MINIO_MC_IMAGE"

echo "==> Exporting Docker image files"
mkdir -p "$OUTPUT_DIR"
docker save -o "$OUTPUT_DIR/sandwish-admin-api-$TAG.tar" "$ADMIN_API_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-front-api-$TAG.tar" "$FRONT_API_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-open-api-$TAG.tar" "$OPEN_API_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-nginx-$TAG.tar" "$NGINX_RUNTIME_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-k6-$TAG.tar" "$K6_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-mysql.tar" "$MYSQL_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-redis.tar" "$REDIS_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-rocketmq.tar" "$ROCKETMQ_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-minio.tar" "$MINIO_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-minio-mc.tar" "$MINIO_MC_IMAGE"

cat > "$OUTPUT_DIR/manifest.txt" <<EOF
$ADMIN_API_IMAGE -> sandwish-admin-api-$TAG.tar
$FRONT_API_IMAGE -> sandwish-front-api-$TAG.tar
$OPEN_API_IMAGE -> sandwish-open-api-$TAG.tar
$NGINX_RUNTIME_IMAGE -> sandwish-nginx-$TAG.tar
$K6_IMAGE -> sandwish-k6-$TAG.tar
$MYSQL_IMAGE -> sandwish-mysql.tar
$REDIS_IMAGE -> sandwish-redis.tar
$ROCKETMQ_IMAGE -> sandwish-rocketmq.tar
$MINIO_IMAGE -> sandwish-minio.tar
$MINIO_MC_IMAGE -> sandwish-minio-mc.tar
EOF

echo "==> Done"
echo "Image files: $OUTPUT_DIR"
