#!/bin/sh
set -eu

export PORT="${PORT:-8080}"
case "${SPRING_JPA_HIBERNATE_DDL_AUTO:-}" in
""|validate)
  export SPRING_JPA_HIBERNATE_DDL_AUTO=update
  ;;
esac
export SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE="${SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE:-1}"
export SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE="${SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE:-5}"

: "${APP_JWT_SECRET:?APP_JWT_SECRET must be set}"

datasource_url="${SPRING_DATASOURCE_URL:-${DATABASE_URL:-}}"

case "$datasource_url" in
postgresql://*|postgres://*)
  db_url="${datasource_url#postgresql://}"
  db_url="${db_url#postgres://}"
  credentials="${db_url%@*}"
  host_and_db="${db_url#*@}"
  user="${credentials%%:*}"
  password="${credentials#*:}"
  host_port="${host_and_db%%/*}"
  database_with_params="${host_and_db#*/}"
  database="${database_with_params%%\?*}"

  export SPRING_DATASOURCE_URL="jdbc:postgresql://${host_port}/${database}"
  export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-$user}"
  export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-$password}"
  ;;
esac

: "${SPRING_DATASOURCE_URL:?SPRING_DATASOURCE_URL or DATABASE_URL must be set}"
: "${SPRING_DATASOURCE_USERNAME:?SPRING_DATASOURCE_USERNAME must be set}"
: "${SPRING_DATASOURCE_PASSWORD:?SPRING_DATASOURCE_PASSWORD must be set}"

exec java $JAVA_OPTS -jar /app/app.jar
