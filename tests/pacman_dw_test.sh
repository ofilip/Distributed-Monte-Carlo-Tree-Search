#!/bin/bash

TEST_JAR=jars/MCTSPacmanTest.jar

echo -e "trial\tpacman_class\tpacman_class\tghost_time\tucb_coef\tdeath_weight\tsim_depth\tsim_random_prob\tscore\tsimulations_per_second\tavg_decision_simulations\ttotal_time"

for ghost in "pacman.controllers.examples.StarterGhosts"; do
    for ucb in 0.05 0.5 1.5; do
    	for death_weight in 0 0.2; do
		for pacman_time in 40; do
		    trial=1
		    err=0
		    while [ $trial -le 10 ]; do
			res=`java -jar $TEST_JAR $pacman_time $ghost $ucb 1.0 $death_weight`
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
    done
done
