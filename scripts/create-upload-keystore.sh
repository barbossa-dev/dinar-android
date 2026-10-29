#!/usr/bin/env bash
# Creates the upload key used to sign Dinar release builds and writes keystore.properties.
# Run once, then BACK UP keystore/dinar-upload.jks and the passwords somewhere safe:
# losing them means you can no longer publish updates to stores without Play App Signing.
set -euo pipefail
cd "$(dirname "$0")/.."

KEYSTORE="keystore/dinar-upload.jks"
ALIAS="dinar-upload"

if [[ -f "$KEYSTORE" ]]; then
  echo "✋ $KEYSTORE already exists. Delete it first if you really want a new key."
  exit 1
fi

# Prefer JAVA_HOME, then Android Studio's bundled JDK, then whatever is on PATH.
KEYTOOL="keytool"
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/keytool" ]]; then
  KEYTOOL="$JAVA_HOME/bin/keytool"
elif [[ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" ]]; then
  KEYTOOL="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool"
fi

read -r -p "Your name or company (e.g. Fama Studio): " CN
read -r -s -p "Keystore password (min 6 chars): " STORE_PASS; echo
read -r -s -p "Repeat password: " STORE_PASS2; echo
[[ "$STORE_PASS" == "$STORE_PASS2" ]] || { echo "Passwords do not match."; exit 1; }
[[ ${#STORE_PASS} -ge 6 ]] || { echo "Password is too short."; exit 1; }

mkdir -p keystore
"$KEYTOOL" -genkeypair -v \
  -keystore "$KEYSTORE" -storetype PKCS12 \
  -alias "$ALIAS" -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$STORE_PASS" -keypass "$STORE_PASS" \
  -dname "CN=$CN, O=$CN, C=IR"

cat > keystore.properties <<PROPS
storeFile=$KEYSTORE
storePassword=$STORE_PASS
keyAlias=$ALIAS
keyPassword=$STORE_PASS
PROPS
chmod 600 keystore.properties "$KEYSTORE"

echo
echo "✅ Created $KEYSTORE and keystore.properties (both are git-ignored)."
echo "   Back them up now. Then run: scripts/build-release.sh"
