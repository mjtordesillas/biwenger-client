#!/usr/bin/env bash
# RAT: Can we authenticate against Biwenger and retrieve the squad via reproducible HTTP calls?
#
# Usage:
#   export BIWENGER_EMAIL='you@example.com'
#   export BIWENGER_PASSWORD='your-password'
#   ./rat-biwenger.sh
#
# This script never prints your password. It prints a redacted preview of the
# token/response so you can sanity-check without exposing secrets in your
# terminal history/logs if you share output with someone else.
#
# Run this LOCALLY. Do not paste your email/password into any chat.

set -euo pipefail

: "${BIWENGER_EMAIL:?Set BIWENGER_EMAIL}"
: "${BIWENGER_PASSWORD:?Set BIWENGER_PASSWORD}"

WORKDIR="$(mktemp -d)"
echo "Working dir: $WORKDIR"

echo
echo "== Step 1: Login =="
curl -s -X POST 'https://biwenger.as.com/api/v2/auth/login' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d "$(jq -n --arg e "$BIWENGER_EMAIL" --arg p "$BIWENGER_PASSWORD" '{email:$e,password:$p}')" \
  -o "$WORKDIR/login.json" -w 'HTTP %{http_code}\n'

TOKEN=$(jq -r '.token // empty' "$WORKDIR/login.json")
if [ -z "$TOKEN" ]; then
  echo "FAILED: no token in login response. Contents (may include error message, no secrets):"
  jq . "$WORKDIR/login.json"
  exit 1
fi
echo "Login OK. Token preview: ${TOKEN:0:12}... (len=${#TOKEN})"
AUTH="Bearer $TOKEN"

echo
echo "== Step 2: Account (leagues, user id, league id) =="
curl -s 'https://biwenger.as.com/api/v2/account' \
  -H "Authorization: $AUTH" \
  -H 'Accept: application/json' \
  -o "$WORKDIR/account.json" -w 'HTTP %{http_code}\n'

jq '.data.leagues[] | {id, name, userId: .user.id, userName: .user.name}' "$WORKDIR/account.json"

LEAGUE_ID=$(jq -r '.data.leagues[0].id' "$WORKDIR/account.json")
USER_ID=$(jq -r '.data.leagues[0].user.id' "$WORKDIR/account.json")
echo "Using X-League=$LEAGUE_ID X-User=$USER_ID"

echo
echo "== Step 3: Squad (player ids owned by current user) =="
curl -s 'https://biwenger.as.com/api/v2/user?fields=players(*,clause,owner)' \
  -H "Authorization: $AUTH" \
  -H "X-League: $LEAGUE_ID" \
  -H "X-User: $USER_ID" \
  -H 'Accept: application/json' \
  -o "$WORKDIR/user.json" -w 'HTTP %{http_code}\n'

echo "Player entries returned:"
jq '.data.players | length' "$WORKDIR/user.json"
jq '.data.players[0]' "$WORKDIR/user.json"

echo
echo "== Step 4: Catalog (to resolve name/position/price if step 3 lacks them) =="
curl -s 'https://biwenger.as.com/api/v2/competitions/la-liga/data?lang=es&score=5' \
  -H 'Accept: application/json' \
  -o "$WORKDIR/catalog.json" -w 'HTTP %{http_code}\n'

echo "Sample catalog entry for first owned player id:"
FIRST_ID=$(jq -r '.data.players[0].id' "$WORKDIR/user.json")
jq --arg id "$FIRST_ID" '.data.players[$id] | {id, name, position, price}' "$WORKDIR/catalog.json"

echo
echo "== Done. Files kept in $WORKDIR for inspection. =="
echo "Inspect $WORKDIR/user.json and $WORKDIR/catalog.json to confirm name/position/price fields."
