#!/bin/bash

QUEUE_FILE=queue
RESULTS_DIR=results
REFRESH_SECONDS=5

while true; do
    if [ ! -e "$QUEUE_FILE" ]; then
        touch "$QUEUE_FILE"
    fi
    if [ `wc -l "$QUEUE_FILE"|cut -d' ' -f 1` -eq 0 ]; then
        sleep $REFRESH_SECONDS
    else
        action=`head -n 1 "$QUEUE_FILE"`
        action_name=`echo $action|cut -d\; -f 1`
        action_cmd=`echo $action|cut -d\; -f 2`
        timestamp=`date +%Y%m%d-%H%M`
        output_file=$timestamp-$action_name.txt
        output_err_file=$timestamp-$action_name.err
        $action_cmd > "$RESULTS_DIR/$output_file" 2> "$RESULTS_DIR/$output_err_file"
        sed -i 1d "$QUEUE_FILE"
    fi
done
