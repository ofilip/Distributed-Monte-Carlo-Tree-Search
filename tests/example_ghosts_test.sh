#!/bin/bash

echo -e "trial\tpacman_class\tghost_class\tscore"

for pacman in "pacman.controllers.examples.StarterPacMan" "pacman.controllers.ICEP_IDDFS"; do
    for ghost in RandomGhosts StarterGhosts Legacy Legacy2TheReckoning AggressiveGhosts; do
	    trial=1
	    err=0
	    while [ $trial -le 20 ]; do
	        res=`java -jar jars/ExampleGhostsTest.jar $pacman "pacman.controllers.examples.$ghost"`
		if [ "$?" -eq 0 ]; then
                    echo -e "$trial\t$res"
                    trial=`expr $trial + 1`
                    err=0
                else
                    if [ "$err" -eq 0 ]; then
                        err=1
                    else
                        echo "Trial $trial failed twice in a row, skipping" 1>&2
                        err=0
                        trial=`expr $trial + 1`
                    fi
                fi 
	    done
    done
done
