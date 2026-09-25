#!/usr/bin/env bash
# Reset demo passwords after realm import (Keycloak often ignores plaintext
# credentials in realm JSON). Run with Keycloak up on :8081.
set -euo pipefail

ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASS="${KEYCLOAK_ADMIN_PASSWORD:-change-me-local-only}"
KC_URL="${KEYCLOAK_URL:-http://localhost:8081}"
DEMO_PASSWORD="${DEMO_PASSWORD:-demo}"

TOKEN=$(curl -sf -X POST "$KC_URL/realms/master/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASS" | python -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

USERS=$(curl -sf -H "Authorization: Bearer $TOKEN" \
  "$KC_URL/admin/realms/logiflow/users?max=50")

python - "$TOKEN" "$KC_URL" "$DEMO_PASSWORD" "$USERS" <<'PY'
import json, sys, urllib.request

token, kc, password, users_json = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
users = json.loads(users_json)
body = json.dumps({"type": "password", "value": password, "temporary": False}).encode()
for user in users:
    req = urllib.request.Request(
        f"{kc}/admin/realms/logiflow/users/{user['id']}/reset-password",
        data=body,
        headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
        },
        method="PUT",
    )
    with urllib.request.urlopen(req) as resp:
        print(f"{user['username']}: {resp.status}")
PY
