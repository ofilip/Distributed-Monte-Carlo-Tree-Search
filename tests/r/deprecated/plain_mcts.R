setwd("d:/diplomka/git/Distributed-Monte-Carlo-Tree-Search/tests")
library(reshape)
library(gplots)
source(file="r/utils.r")

plot_data <- function(my_data) {


	melted_data <- melt(my_data, id=c("pacman_class", "ghost_class", "ghost_time", "ucb_coef"))

	scores <- cast(melted_data, pacman_class+ghost_class+ucb_coef+ghost_time~variable, mean)
	scores_matrix <- data.matrix(cast(scores, pacman_class+ghost_time~ghost_class+ucb_coef, value="score"))
	scores_matrix <- scores_matrix[,3:3]

	cils <- cast(melted_data, pacman_class+ghost_class+ucb_coef+ghost_time~variable, cil)
	cils_matrix <- data.matrix(cast(cils, pacman_class+ghost_time~ghost_class+ucb_coef,value="score"))
	cils_matrix <- cils_matrix[,3:3]

	cius <- cast(melted_data, pacman_class+ghost_class+ucb_coef+ghost_time~variable, ciu)
	cius_matrix <- data.matrix(cast(cius, pacman_class+ghost_time~ghost_class+ucb_coef,value="score"))
	cius_matrix <- cius_matrix[,3:3]

	barplot2(scores_matrix,beside=TRUE, 
		space=c(0.2,0.8),
		plot.ci=TRUE, 
		ci.l=cils_matrix, 
		ci.u=cius_matrix,
		border="black",
		col=c("blue", "red"),
		xlab="Ghost controller name",
		ylab="Average score with 95% confidence interval (less is better)",
		font.lab=2,
		plot.grid=TRUE
	)
}

data <- read.delim("results/plain_mcts_dw_test_20130402.txt", row.names=NULL)
data <- data[c("pacman_class","ghost_class","score", "ghost_time", "ucb_coef")]
plot_data(data)
my_data<-
#plot_data(data[data$pacman_class=="ICEP_IDDFS",])

#legend("topright", legend=c("ICEP_IDDFS", "StarterPacMan"), fill=c("blue", "red"), title="Opponent")