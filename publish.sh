#!/bin/bash

local_filename=mcts_java/dist/pacman_sandbox.jar

if [ ! -f "$local_filename" ]; then
	echo "Error: Missing pacman_sandbox.jar"	
	exit 1
else
	upload_filename=lacerta:/home/filip/dist/pacman_sandbox_`date +%Y%m%d_%H%M%S`.jar
	echo scp "$local_filename" "$upload_filename"
	if scp "$local_filename" "$upload_filename"; then
		echo "File $upload_filename successfully created"
		exit 0
	else
		echo "Error: Uploading failed"
		exit 1
	fi
fi
