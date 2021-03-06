#!/bin/bash

print_usage() {
	echo "usage: $0 TEST_JAR"
}

if [ "$#" -ne 1 ]; then
	print_usage
	exit 0
fi

TEST_JAR=$1

echo -e "trial\tpacman_class\tghost_class\tghost_time\tucb_coef\tsim_depth\tsim_random_prob\tscore\tsimulations_per_second"

for pacman in "pacman.controllers.examples.StarterPacMan"; do
    for ucb in 0.4 0.8 1.2; do
        for ghost_time in 40 80 120 160 200; do
	    trial=1
	    err=0
	    while [ $trial -le 20 ]; do
	        res=`java -jar $TEST_JAR $pacman $ghost_time $ucb`
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
