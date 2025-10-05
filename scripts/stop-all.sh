#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

stop_module() {
  local dir="$1"
  local name="$2"
  local pid_file="$ROOT_DIR/$dir/server.pid"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file")"
    if ps -p "$pid" >/dev/null 2>&1; then
      echo "[stop] $name (PID $pid)"
      kill "$pid" || true
      for _ in {1..10}; do
        sleep 1
        ps -p "$pid" >/dev/null 2>&1 || break
      done
      if ps -p "$pid" >/dev/null 2>&1; then
        echo "[warn] $name aún vivo, intenta 'kill -9 $pid'"
      else
        echo "[ok] $name detenido"
      fi
    else
      echo "[info] $name no está corriendo"
    fi
    rm -f "$pid_file"
  else
    echo "[info] $name sin PID (no iniciado)"
  fi
}

stop_module bff-atm "bff-atm"
stop_module bff-mobile "bff-mobile"
stop_module bff-web "bff-web"
stop_module analytics-service "analytics-service"
stop_module legacy-api "legacy-api"
stop_module discovery-server "discovery-server"
stop_module config-server "config-server"

echo "Listo."
