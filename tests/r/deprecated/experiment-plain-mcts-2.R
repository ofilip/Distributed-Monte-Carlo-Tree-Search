setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

data <- read.delim("results/20130427-plain-mcts-parallel.txt", row.names=NULL)
data <- data[c("score", "ghost_time", "ghost_sims_per_sec")]
melted_data <- melt(data, id=c("ghost_time"))
scores <- cast(melted_data, ghost_time~variable, mean)
plot(scores$ghost_time, scores$score, type="o",
	main="Plain MCTS strength",
	xlab="Time [s]",
	ylab="Average score",
	ylim=c(500,2000)
	)

data <- read.delim("results/20130428-1305-dummy-ghosts-mt.txt", row.names=NULL)
data <- data[c("score", "ghost_time", "ghost_sims_per_sec", "ghost_class","synchronization_ratio")]
melted_data <- melt(data, id=c("ghost_time", "ghost_class"))
scores <- cast(melted_data, ghost_time+ghost_class~variable, mean)
scores_u <- scores[scores$ghost_class=="DummyGhosts",]
scores_s <- scores[scores$ghost_class=="SynchronizedDummyGhosts",]
lines(scores_s$ghost_time, scores_s$score, type="o")