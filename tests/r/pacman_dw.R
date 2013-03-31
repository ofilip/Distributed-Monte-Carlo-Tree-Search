setwd("d:/diplomka/git/Distributed-Monte-Carlo-Tree-Search/tests")
library(reshape)
library(gplots)
source(file="r/utils.r")

plot_data <- function(my_data) {
	melted_data <- melt(my_data, id=c("pacman_time", "ucb_coef", "death_weight"))

	scores <- cast(melted_data, ucb_coef+pacman_time+death_weight~variable, mean)
	scores_matrix <- data.matrix(cast(scores, ucb_coef~death_weight, value="score"))
	scores_matrix <- scores_matrix[,2:4]

	cils <- cast(melted_data, ucb_coef+pacman_time+death_weight~variable, cil)
	cils_matrix <- data.matrix(cast(cils, ucb_coef~death_weight,value="score"))
	cils_matrix <- cils_matrix[,2:4]

	cius <- cast(melted_data, ucb_coef+pacman_time+death_weight~variable, ciu)
	cius_matrix <- data.matrix(cast(cius, ucb_coef~death_weight,value="score"))
	cius_matrix <- cius_matrix[,2:4]

	barplot2(scores_matrix,beside=TRUE, 
		space=c(0.2,0.8),
		plot.ci=TRUE, 
		ci.l=cils_matrix, 
		ci.u=cius_matrix,
		border="black",
		col=c("blue", "red"),
		xlab="",
		ylab="Average score with 95% confidence interval",
		font.lab=2,
		plot.grid=TRUE
	)
}

data <- read.delim("results/pacman_dw_test_1_20130330.txt", row.names=NULL)
data <- data[c("score", "pacman_time", "ucb_coef", "death_weight")]
plot_data(data)

#legend("topright", legend=c("0", "0.2", "0.5"), fill=c("blue", "red"), title="death_weight")