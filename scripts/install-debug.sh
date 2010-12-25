#!/bin/sh

cd $(dirname "$0")/..

ant debug
adb install -r bin/SenspodApp-debug.apk

# eof
