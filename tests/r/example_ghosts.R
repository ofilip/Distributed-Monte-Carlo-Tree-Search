setwd("d:/diplomka/git/Distributed-Monte-Carlo-Tree-Search/tests")
library(reshape)
library(gplots)
source(file="r/utils.r")

data <- read.delim("results/example_ghosts_20130322.txt")
data <- data[c("pacman_class","ghost_class","score")]
melted_data <- melt(data, id=c("pacman_class", "ghost_class"))

scores <- cast(melted_data, pacman_class+ghost_class~variable, mean)
scores_matrix <- data.matrix(cast(scores, pacman_class~ghost_class,value="score"))
scores_matrix <- scores_matrix[,2:6]

cils <- cast(melted_data, pacman_class+ghost_class~variable, cil)
cils_matrix <- data.matrix(cast(cils, pacman_class~ghost_class,value="score"))
cils_matrix <- cils_matrix[,2:6]

cius <- cast(melted_data, pacman_class+ghost_class~variable, ciu)
cius_matrix <- data.matrix(cast(cius, pacman_class~ghost_class,value="score"))
cius_matrix <- cius_matrix[,2:6]

barplot2(scores_matrix,beside=TRUE, 
	space=c(0.2,0.8),
	plot.ci=TRUE, 
	ci.l=cils_matrix, 
	ci.u=cius_matrix,
	border="black",
	main=c("Average Scores of Example Ghost Controllers"),
	col=c("blue", "red"),
	xlab="Ghost controller name",
	ylab="Average score with 95% confidence interval (less is better)",
	font.lab=2,
	plot.grid=TRUE
)
legend("topright", legend=c("ICEP_IDDFS", "StarterPacMan"), fill=c("blue", "red"), title="Opponent")