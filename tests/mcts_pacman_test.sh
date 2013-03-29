#!/bin/bash

print_usage() {
	echo "usage: $0 TEST_JAR"
}

if [ "$#" -ne 1 ]; then
	print_usage
	exit 0
fi

TEST_JAR=jars/MCTSPacmanTest.jar

echo -e "trial\tpacman_class\tghost_class\tghost_time\tucb_coef\tsim_depth\tsim_random_prob\tscore\tsimulations_per_second"

for ghost in "pacman.controllers.examples.StarterGhosts" "pacman.controllers.examples.Legacy" "pacman.controllers.examples.Legacy2TheReckoning" "pacman.controllers.examples.RandomGhosts" "mcts.entries.ghosts.MCTSGhosts"; do
    for ucb in 0.05 0.3 0.8 1.5; do
        for pacman_time in 20 80 200 1000; do
	    trial=1
	    err=0
	    while [ $trial -le 20 ]; do
	        res=`java -jar $TEST_JAR $pacman_time $ghost $ucb`
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
