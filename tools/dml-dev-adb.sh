#!/usr/bin/env bash

package="com.shonkware.droidmodloader.dev"

adb_cmd=(adb)
if test -n "${ADB_SERIAL:-}"; then
  adb_cmd+=(-s "$ADB_SERIAL")
fi
usage() {
  printf '%s\n' \
    'DML Dev ADB helper' \
    '' \
    'Usage:' \
    '  ./tools/dml-dev-adb.sh check' \
    '  ./tools/dml-dev-adb.sh root' \
    '  ./tools/dml-dev-adb.sh storage-keys' \
    '  ./tools/dml-dev-adb.sh mods <storage-key>' \
    '  ./tools/dml-dev-adb.sh path <storage-key> <mod-id>' \
    '  ./tools/dml-dev-adb.sh tree <storage-key> <mod-id>' \
    '' \
    'Storage keys are materialized internal DML Dev storage scopes.' \
    'They are not an authoritative list of logical DML profiles.' \
    'Set ADB_SERIAL when more than one Android device is connected.'
}

safe_segment() {
  value="$1"
  case "$value" in
    ""|*[!A-Za-z0-9._-]*)
      return 1
      ;;
    *)
      return 0
      ;;
  esac
}

adb_ready() {
  if ! command -v adb >/dev/null 2>&1; then
    printf '%s\n' 'REFUSING: adb is unavailable'
    return 1
  fi

  state="$("${adb_cmd[@]}" get-state 2>/dev/null)"
  if test "$state" != "device"; then
    printf '%s\n' 'REFUSING: no selected ADB device is ready'
    return 1
  fi

  root="$(
    "${adb_cmd[@]}" shell run-as "$package" pwd 2>/dev/null |
      tr -d '\r'
  )"

  if test -z "$root"; then
    printf '%s\n' \
      'REFUSING: DML Dev is not installed or run-as is unavailable'
    return 1
  fi

  return 0
}

run_as() {
  "${adb_cmd[@]}" shell run-as "$package" "$@"
}

main() {
  action="${1:-help}"

  case "$action" in
    help|-h|--help)
      usage
      return 0
      ;;

    check)
      adb_ready || return 1
      root="$(run_as pwd | tr -d '\r')"
      printf 'package=%s\n' "$package"
      printf 'root=%s\n' "$root"
      printf '%s\n' 'DML DEV ADB CHECK PASS'
      return 0
      ;;

    root)
      adb_ready || return 1
      run_as pwd | tr -d '\r'
      return "${PIPESTATUS[0]}"
      ;;

    storage-keys)
      adb_ready || return 1

      raw_output="$(run_as ls -1 files/profiles 2>/dev/null)"
      status="$?"
      output="$(printf '%s' "$raw_output" | tr -d '\r')"

      if test "$status" -ne 0 || test -z "$output"; then
        printf '%s\n' 'No DML Dev storage keys found.'
        return 0
      fi

      printf '%s\n' "$output"
      return 0
      ;;

    mods)
      storage_key="${2:-}"

      if ! safe_segment "$storage_key"; then
        printf '%s\n' 'REFUSING: storage-key contains unsupported characters'
        return 1
      fi

      adb_ready || return 1

      storage_rel="files/profiles/$storage_key"
      if ! run_as ls -ld "$storage_rel" >/dev/null 2>&1; then
        printf 'REFUSING: profile storage key not found: %s\n' "$storage_key"
        return 1
      fi

      rel="$storage_rel/mods"
      raw_output="$(run_as ls -1 "$rel" 2>/dev/null)"
      status="$?"
      output="$(printf '%s' "$raw_output" | tr -d '\r')"

      if test "$status" -ne 0 || test -z "$output"; then
        printf 'No managed mods found for storage key: %s\n' "$storage_key"
        return 0
      fi

      printf '%s\n' "$output"
      return 0
      ;;

    path)
      storage_key="${2:-}"
      mod="${3:-}"

      if ! safe_segment "$storage_key"; then
        printf '%s\n' 'REFUSING: storage-key contains unsupported characters'
        return 1
      fi

      if ! safe_segment "$mod"; then
        printf '%s\n' 'REFUSING: mod-id contains unsupported characters'
        return 1
      fi

      adb_ready || return 1

      rel="files/profiles/$storage_key/mods/$mod"
      if ! run_as ls -ld "$rel" >/dev/null 2>&1; then
        printf 'REFUSING: managed mod path not found: %s\n' "$rel"
        return 1
      fi

      root="$(run_as pwd | tr -d '\r')"
      printf '%s/%s\n' "$root" "$rel"
      return 0
      ;;

    tree)
      storage_key="${2:-}"
      mod="${3:-}"

      if ! safe_segment "$storage_key"; then
        printf '%s\n' 'REFUSING: storage-key contains unsupported characters'
        return 1
      fi

      if ! safe_segment "$mod"; then
        printf '%s\n' 'REFUSING: mod-id contains unsupported characters'
        return 1
      fi

      adb_ready || return 1

      rel="files/profiles/$storage_key/mods/$mod"
      if ! run_as ls -ld "$rel" >/dev/null 2>&1; then
        printf 'REFUSING: managed mod path not found: %s\n' "$rel"
        return 1
      fi

      run_as find "$rel" -print |
        tr -d '\r' |
        sort
      return "${PIPESTATUS[0]}"
      ;;

    *)
      printf 'REFUSING: unknown action: %s\n' "$action"
      usage
      return 1
      ;;
  esac
}

main "$@"
