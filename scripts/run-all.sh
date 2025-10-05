#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if command -v nc >/dev/null 2>&1; then
  if ! nc -z localhost 9092 >/dev/null 2>&1; then
    echo "[warn] Kafka no responde en localhost:9092. Ejecuta 'docker compose -f infra/kafka/docker-compose.yml up -d' antes de iniciar los servicios asíncronos." >&2
  fi
else
  echo "[info] No se pudo verificar Kafka (nc no disponible). Asegúrate de levantarlo manualmente." >&2
fi

start_module() {
  local dir="$1"
  local name="$2"
  cd "$ROOT_DIR/$dir"
  if [[ -f server.pid ]] && ps -p "$(cat server.pid)" >/dev/null 2>&1; then
    echo "[skip] $name ya está corriendo (PID $(cat server.pid))"
    return 0
  fi
  echo "[start] $name"
  nohup ./mvnw -q spring-boot:run > server.log 2>&1 & echo $! > server.pid
  for _ in {1..12}; do
    sleep 1
    if rg -n "Started" server.log >/dev/null 2>&1; then
      rg -n "Started" server.log | tail -n1 || true
      break
    fi
  done
}

start_module config-server "config-server"
start_module discovery-server "discovery-server"
start_module legacy-api "legacy-api"
start_module analytics-service "analytics-service"
start_module bff-web "bff-web"
start_module bff-mobile "bff-mobile"
start_module bff-atm "bff-atm"

echo "Listo. PIDs:"
for m in config-server discovery-server legacy-api analytics-service bff-web bff-mobile bff-atm; do
  if [[ -f "$ROOT_DIR/$m/server.pid" ]]; then
    printf "%-16s %s\n" "$m:" "$(cat "$ROOT_DIR/$m/server.pid")"
  fi
done
