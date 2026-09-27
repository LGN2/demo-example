#!/bin/sh
set -eu
[ "${1-}" = RESET_DEMO ] || { echo 'Usage: sh scripts/reset-dev.sh RESET_DEMO (deletes demo volumes)' >&2; exit 1; }
[ -f .env ] && grep -Eq '^SPRING_PROFILES_ACTIVE=dev[[:space:]]*$' .env || { echo 'Refusing: .env must explicitly select dev.' >&2; exit 1; }
docker compose down --volumes
docker compose up --build -d
