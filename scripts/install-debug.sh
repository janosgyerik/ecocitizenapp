#!/bin/sh

cd $(dirname "$0")/..

test -f local.properties || cp samples/local.properties .

ant debug
adb install -r bin/EcoCitizen-debug.apk

# eof
