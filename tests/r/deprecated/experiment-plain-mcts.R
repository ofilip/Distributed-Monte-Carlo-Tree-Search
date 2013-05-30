setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

# Death weight tuning
data <- read.delim("results/20130414-0624-plain-mcts.txt", row.names=NULL)
data <- data[c("score", "ghost_time", "ghost_sims_per_sec")]
melted_data <- melt(data, id=c("ghost_time"))
scores <- cast(melted_data, ghost_time~variable, mean)
plot(scores$ghost_time, scores$score, type="o",
	main="Plain MCTS strength",
	xlab="Time [s]",
	ylab="Average score",
	ylim=c(700,2200)
	)

data <- read.delim("results/20130421-2145-dummy-ghosts.txt", row.names=NULL)
data <- data[c("score", "ghost_time", "ghost_sims_per_sec")]
melted_data <- melt(data, id=c("ghost_time"))
scores <- cast(melted_data, ghost_time~variable, mean)
lines(scores$ghost_time, scores$score, type="p")