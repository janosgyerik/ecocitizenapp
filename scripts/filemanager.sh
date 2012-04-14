#!/bin/sh
#
# SCRIPT: download.sh
# AUTHOR: Janos Gyerik <info@titan2x.com>
# DATE:   2011-06-25
# REV:    1.0.D (Valid are A, B, D, T and P)
#               (For Alpha, Beta, Dev, Test and Production)
#
# PLATFORM: Not platform dependent
#
# PURPOSE: Download EcoCitizen session files from Android to specified folder.
#
# set -n   # Uncomment to check your syntax, without execution.
#          # NOTE: Do not forget to put the comment back in or
#          #       the shell script will not execute!
# set -x   # Uncomment to debug this shell script (Korn shell only)
#

usage() {
    test $# = 0 || echo $@
    echo "Usage: $0 [OPTION]... DIR"
    echo
    echo Download EcoCitizen session files from Android to specified folder.
    echo
    echo Options:
    echo
    echo "  -h, --help           Print this help"
    echo
    echo "  --download, --pull   Download all files"
    echo "  -l, --list, --ls     List files"
    echo "  --delete, --del      Delete all files"
    echo
    exit 1
}

args=
#arg=
#flag=off
mode=list
#param=
while [ $# != 0 ]; do
    case $1 in
    -h|--help) usage ;;
#    -f|--flag) flag=on ;;
    --download|--pull|--dl) mode=download ;;
    -l|--list|--ls) mode=list ;;
    --delete|--del) mode=delete ;;
#    --no-flag) flag=off ;;
#    -p|--param) shift; param=$1 ;;
#    --) shift; while [ $# != 0 ]; do args="$args \"$1\""; shift; done; break ;;
    -) usage "Unknown option: $1" ;;
    -?*) usage "Unknown option: $1" ;;
    *) args="$args \"$1\"" ;;  # script that takes multiple arguments
#    *) test "$arg" && usage || arg=$1 ;;  # strict with excess arguments
#    *) arg=$1 ;;  # forgiving with excess arguments
    esac
    shift
done

eval "set -- $args"  # save arguments in $@. Use "$@" in for loops, not $@ 

test "$1" && download_dir=$1 || download_dir=EcoCitizen-files

basedir=/sdcard/download
pattern='^(EcoCitizen|session)_'

list() {
    adb shell ls $basedir | grep -E $pattern | tr -d '\r'
}

delete() {
    echo rm $basedir/$1
    adb shell rm $basedir/$1
}

pull() {
    adb pull $basedir/$1 "$download_dir"/$1
    echo saved to $download_dir/$1
}

case $mode in
    download)
        mkdir -p "$download_dir"
        list | while read file; do pull $file; done
        rmdir "$download_dir" 2>/dev/null
        ;;
    list)
        list
        ;;
    delete)
        list | while read file; do delete $file; done
        ;;
esac

# eof
