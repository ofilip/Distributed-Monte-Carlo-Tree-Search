#!/bin/bash

for pacman in "pacman.controllers.examples.StarterPacMan" "pacman.controllers.ICEP_IDDFS"; do
    for ucb in 0.4 0.8 1.2; do
        for ghost_time in 40 80 120 160 200; do
	    trial=1
	    err=0
	    while [ $trial -le 20 ]; do
	        res=`java -jar jars/PlainMCTSTest $pacman $ghost_time $ucb`
		if 
	    done

	done
    done
done
