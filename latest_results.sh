#!/bin/bash

if [ $# -eq 0 ]; then
	echo usage: $0 RESULTS_FILE_PATTERN
	exit 0
fi

results_dir=tests/results/
latest=`ls $results_dir | grep -E "[0-9]{8}-[0-9]{4}-$1\.txt$" | sort | tail -1`
if [ "$latest" = "" ]; then
	echo "No matching file found"
else
	echo $results_dir$latest
	exit 0
fi
