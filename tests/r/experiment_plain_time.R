setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

plot_data <- function(my_data) {
	melted_data <- melt(my_data, id=c("ghost_death_weight", "ghost_time", "ghost_class", "ghost_sim_depth"))

	scores <- cast(melted_data, ghost_death_weight+ghost_class+ghost_time+ghost_sim_depth~variable, mean)
	scores_matrix <- data.matrix(cast(scores, ghost_death_weight+ghost_time+ghost_sim_depth~ghost_class, value="score"))	
	#scores_matrix <- scores_matrix[,4:4]

	#cils <- cast(melted_data, ghost_death_weight+ghost_class~variable, cil)
	#cils_matrix <- data.matrix(cast(cils, ghost_death_weight~ghost_class,value="score"))
	
#cils_matrix <- cils_matrix[,4:4]

	#cius <- cast(melted_data, ghost_death_weight+ghost_class~variable, ciu)
	#cius_matrix <- data.matrix(cast(cius, ghost_death_weight~ghost_class,value="score"))
	#cius_matrix <- cius_matrix[,4:4]

	barplot2(scores_matrix,beside=TRUE, 
		space=c(0.2,0.8),
		border="black",
		col=c("blue", "red"),
		xlab="Ghost controller name",
		ylab="Average score with 95% confidence interval (less is better)",
		font.lab=2,
		plot.grid=TRUE
	)
}

data <- read.delim("results/20130413-1017-simdepth-tuning.txt", row.names=NULL)
data <- data[c("score", "ghost_class", "ghost_death_weight", "ghost_time", "ghost_sim_depth")]
plot_data(data)

#plot_data(data[data$pacman_class=="ICEP_IDDFS",])

#legend("topright", legend=c("ICEP_IDDFS", "StarterPacMan"), fill=c("blue", "red"), title="Opponent")