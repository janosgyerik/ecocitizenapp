#!/bin/sh

cd $(dirname "$0")/..

test -f local.properties || cp samples/local.properties .

test -f "$1" && grep map_server_url "$1" >/dev/null 2>/dev/null && cp "$1" res/values/props.xml

ant debug
adb install -r bin/EcoCitizen-debug.apk

# eof
