#!/bin/sh -e
#
# SCRIPT: publish.sh
# AUTHOR: Janos Gyerik <info@titan2x.com>
# DATE:   2012-03-31
# REV:    1.0.D (Valid are A, B, D, T and P)
#               (For Alpha, Beta, Dev, Test and Production)
#
# PLATFORM: Not platform dependent
#
# PURPOSE: Upload the latest build to the download site
#
# set -n   # Uncomment to check your syntax, without execution.
#          # NOTE: Do not forget to put the comment back in or
#          #       the shell script will not execute!
# set -x   # Uncomment to debug this shell script (Korn shell only)
#

cd $(dirname "$0")/..

build_id=$(grep build_id res/values/props.xml | head -n 1 | sed -e 's/[^>]*>//' -e 's/<.*//')
apk=bin/EcoCitizen-release.apk
apk_release=EcoCitizen-$build_id.apk

remote_host=titan2x
remote_dir=webapps/blog.ecomobilecitizen.com/download

rsync --progress -v $apk $remote_host:$remote_dir/$apk_release

# eof
