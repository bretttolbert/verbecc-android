#!/usr/bin/env bash
set -euo pipefail

sdk="$HOME/Android/Sdk"
adb="$sdk/platform-tools/adb"
avd="Pixel_API_36"
serial="emulator-5554"
emulator_pid=""

fail() {
    echo "${BASH_SOURCE[0]}:1: ERROR: $*" >&2
    exit 1
}

cleanup() {
    if [[ -n "$emulator_pid" ]] && kill -0 "$emulator_pid" 2>/dev/null; then
        kill "$emulator_pid"
        wait "$emulator_pid" || true
    fi
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

[[ -x "$adb" && -x "$sdk/emulator/emulator" ]] || fail "Android SDK tools not found in $sdk"
echo "Starting $avd"
"$adb" start-server

if "$adb" devices | awk '{print $1}' | grep -qx "$serial"; then
    running_avd=$("$adb" -s "$serial" emu avd name | head -n 1 | tr -d '\r')
    [[ "$running_avd" == "$avd" ]] || fail "$serial is not $avd. Stop that emulator before launching."
else
    "$sdk/emulator/emulator" -avd "$avd" -port 5554 &
    emulator_pid=$!
fi

deadline=$((SECONDS + 300))
while true; do
    if [[ -n "$emulator_pid" ]] && ! kill -0 "$emulator_pid" 2>/dev/null; then
        fail "Emulator exited before finishing startup. Check the emulator output above."
    fi
    # ADB can temporarily fail while the emulator is starting.
    boot_completed=$("$adb" -s "$serial" shell getprop sys.boot_completed 2>/dev/null) || boot_completed=""
    if [[ "${boot_completed//$'\r'/}" == "1" ]]; then
        break
    fi
    (( SECONDS < deadline )) || fail "$avd did not finish booting within 300 seconds."
    sleep 2
done

echo "$avd ready"
if [[ -n "$emulator_pid" ]]; then
    wait "$emulator_pid"
fi
