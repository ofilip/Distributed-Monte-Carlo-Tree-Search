#!/bin/bash

if [ $# -eq 0 ]; then
	echo usage: $0 RESULTS_FILE_PATTERN
	exit 0
fi

latest=`ssh lacerta ls results | grep -E "[0-9]{8}-[0-9]{4}-$1\.txt$" | sort | tail -1`
if [ "$?" -ne 0 ]; then
	echo "Error: Failed to connect to the server"
	exit 1
fi
if [ "$latest" = "" ]; then
	echo "Error: No file found"
	exit 1
fi 
if scp "lacerta:~/results/$latest" tests/results; then
	echo "Downloaded: lacerta:~/results/$latest"
	exit 0
else
	echo "Error: Failed to download file"
	exit 1
fi
