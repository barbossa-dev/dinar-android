#!/usr/bin/env bash
# Builds signed release artifacts for Dinar:
#   release/dinar-<version>.aab   → Google Play
#   release/dinar-<version>.apk   → Myket, Cafe Bazaar and direct installs
#   release/mapping-<version>.txt → upload to the store to deobfuscate crash reports
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ ! -f keystore.properties && -z "${DINAR_KEYSTORE_FILE:-}" ]]; then
  echo "✋ No release key. Run scripts/create-upload-keystore.sh first."
  exit 1
fi

VERSION=$(grep '^dinar.versionName=' gradle.properties | cut -d= -f2)
CODE=$(grep '^dinar.versionCode=' gradle.properties | cut -d= -f2)
echo "▶ Building Dinar $VERSION ($CODE)"

./gradlew --stop >/dev/null 2>&1 || true
./gradlew clean testDebugUnitTest :app:bundleRelease :app:assembleRelease

mkdir -p release
cp app/build/outputs/bundle/release/app-release.aab "release/dinar-$VERSION.aab"
cp app/build/outputs/apk/release/app-release.apk "release/dinar-$VERSION.apk"
cp app/build/outputs/mapping/release/mapping.txt "release/mapping-$VERSION.txt"

echo
echo "✅ Done:"
ls -lh release/*"$VERSION"*
