#!/bin/bash

function print_usage() {
    echo "usage: $0 NAME COMMAND [FLAG1 [FLAG2...]]"
}

if [ $# -lt 2 ]; then
    print_usage
    exit 1
fi

NAME=$1
shift
COMMAND=$@

echo "$NAME;$COMMAND" >> queue
