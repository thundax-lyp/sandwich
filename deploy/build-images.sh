#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TAG="${SANDWISH_IMAGE_TAG:-dev}"
REGISTRY="${SANDWISH_IMAGE_REGISTRY:-sandwish}"
NPM_INSTALL_MODE="${SANDWISH_NPM_INSTALL_MODE:-ci}"
OUTPUT_DIR="${SANDWISH_IMAGE_OUTPUT_DIR:-$ROOT_DIR/deploy/image-files}"
PULL_INFRA_IMAGES="${SANDWISH_PULL_INFRA_IMAGES:-true}"

JRE_IMAGE="${SANDWISH_JRE_IMAGE:-eclipse-temurin:8-jre}"
NGINX_IMAGE="${SANDWISH_NGINX_IMAGE:-nginx:1.27-alpine}"
MYSQL_IMAGE="${SANDWISH_MYSQL_IMAGE:-mysql:8.4}"
REDIS_IMAGE="${SANDWISH_REDIS_IMAGE:-redis:7.4-alpine}"
MINIO_IMAGE="${SANDWISH_MINIO_IMAGE:-minio/minio:RELEASE.2025-02-28T09-55-16Z}"
MINIO_MC_IMAGE="${SANDWISH_MINIO_MC_IMAGE:-minio/mc:RELEASE.2025-02-21T16-00-46Z}"

ADMIN_API_IMAGE="$REGISTRY/admin-api:$TAG"
FRONT_API_IMAGE="$REGISTRY/front-api:$TAG"
NGINX_RUNTIME_IMAGE="$REGISTRY/nginx:$TAG"

cd "$ROOT_DIR"

echo "==> Building API jars"
mvn -q -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package

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

echo "==> Building Docker image: $REGISTRY/nginx:$TAG"
docker build \
    -f "$ROOT_DIR/deploy/images/nginx.Dockerfile" \
    --build-arg "SANDWISH_NGINX_IMAGE=$NGINX_IMAGE" \
    -t "$NGINX_RUNTIME_IMAGE" \
    "$ROOT_DIR"

if [ "$PULL_INFRA_IMAGES" = "true" ]; then
    echo "==> Pulling infrastructure images"
    docker pull "$MYSQL_IMAGE"
    docker pull "$REDIS_IMAGE"
    docker pull "$MINIO_IMAGE"
    docker pull "$MINIO_MC_IMAGE"
else
    echo "==> Skipping infrastructure image pull"
fi

echo "==> Exporting Docker image files"
mkdir -p "$OUTPUT_DIR"
docker save -o "$OUTPUT_DIR/sandwish-admin-api-$TAG.tar" "$ADMIN_API_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-front-api-$TAG.tar" "$FRONT_API_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-nginx-$TAG.tar" "$NGINX_RUNTIME_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-mysql.tar" "$MYSQL_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-redis.tar" "$REDIS_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-minio.tar" "$MINIO_IMAGE"
docker save -o "$OUTPUT_DIR/sandwish-minio-mc.tar" "$MINIO_MC_IMAGE"

cat > "$OUTPUT_DIR/manifest.txt" <<EOF
$ADMIN_API_IMAGE -> sandwish-admin-api-$TAG.tar
$FRONT_API_IMAGE -> sandwish-front-api-$TAG.tar
$NGINX_RUNTIME_IMAGE -> sandwish-nginx-$TAG.tar
$MYSQL_IMAGE -> sandwish-mysql.tar
$REDIS_IMAGE -> sandwish-redis.tar
$MINIO_IMAGE -> sandwish-minio.tar
$MINIO_MC_IMAGE -> sandwish-minio-mc.tar
EOF

echo "==> Done"
echo "Image files: $OUTPUT_DIR"
