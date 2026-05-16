#!/usr/bin/env sh
set -eu

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TAG="${SANDWISH_IMAGE_TAG:-dev}"
REGISTRY="${SANDWISH_IMAGE_REGISTRY:-sandwish}"
OUTPUT_DIR="${SANDWISH_IMAGE_OUTPUT_DIR:-$ROOT_DIR/deploy/image-files}"
K6_SOURCE_IMAGE="${SANDWISH_K6_SOURCE_IMAGE:-grafana/k6:1.3.0}"
K6_IMAGE="$REGISTRY/k6:$TAG"

cd "$ROOT_DIR"

echo "==> Building Docker image: $K6_IMAGE"
docker build \
    -f "$ROOT_DIR/deploy/images/k6.Dockerfile" \
    --build-arg "SANDWISH_K6_IMAGE=$K6_SOURCE_IMAGE" \
    -t "$K6_IMAGE" \
    "$ROOT_DIR"

echo "==> Exporting Docker image file"
mkdir -p "$OUTPUT_DIR"
docker save -o "$OUTPUT_DIR/sandwish-k6-$TAG.tar" "$K6_IMAGE"

cat > "$OUTPUT_DIR/manifest-k6.txt" <<EOF
$K6_IMAGE -> sandwish-k6-$TAG.tar
EOF

echo "==> Done"
echo "Image file: $OUTPUT_DIR/sandwish-k6-$TAG.tar"
