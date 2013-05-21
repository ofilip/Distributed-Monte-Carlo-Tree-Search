setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

n <- 10
colors <- rainbow(n)
linetype <- c(1:n)
plotchar <- seq(18,18+n,1)

plain.data <- read.delim("results/20130427-plain-mcts-parallel.txt", row.names=NULL)
plain.data2 <- plain.data[c("score", "ghost_time", "ghost_sims_per_sec")]
plain.data_melted <- melt(plain.data2, id=c("ghost_time"))
plain.scores <- cast(plain.data_melted, ghost_time~variable, mean)

plot(plain.scores$ghost_time, plain.scores$score, type="o",
	main="Plain MCTS strength",
	xlab="Time [s]",
	ylab="Average score",
	ylim=c(500,2200),
	lty = linetype[1],
	col = colors[1],
	pch = plotchar[1])

dummy.data <- read.delim("results/20130428-1305-dummy-ghosts-mt.txt", row.names=NULL)
dummy.data2 <- dummy.data[c("score", "ghost_time", "ghost_sims_per_sec", "ghost_class","synchronization_ratio")]
dummy.melted_data <- melt(dummy.data2, id=c("ghost_time", "ghost_class"))
dummy.scores <- cast(dummy.melted_data, ghost_time+ghost_class~variable, mean)
dummy_u.scores <- dummy.scores[dummy.scores$ghost_class=="DummyGhosts",]
dummy_s.scores <- dummy.scores[dummy.scores$ghost_class=="SynchronizedDummyGhosts",]
lines(dummy_s.scores$ghost_time, dummy_s.scores$score, type="o",
	lty = linetype[2],
	col = colors[2],
	pch = plotchar[2])


root.data <- read.delim("results/20130513-0709-simulation-passing.txt", row.names=NULL)
root.data2 <- root.data[c("score", "ghost_time", "ghost_sims_per_sec","synchronization_ratio")]
root.melted_data <- melt(root.data2, id=c("ghost_time"))
root.scores <- cast(root.melted_data, ghost_time~variable, mean)
lines(root.scores$ghost_time, root.scores$score, type="o",
	lty = linetype[3],
	col = colors[3],
	pch = plotchar[3])

simpas.data <- read.delim("results/20130513-1933-move-exchange.txt", row.names=NULL)
simpas.data2 <- simpas.data[c("score", "ghost_time", "ghost_sims_per_sec","synchronization_ratio")]
simpas.melted_data <- melt(simpas.data2, id=c("ghost_time"))
simpas.scores <- cast(simpas.melted_data, ghost_time~variable, mean)
lines(simpas.scores$ghost_time, simpas.scores$score, type="o",
	lty = linetype[4],
	col = colors[4],
	pch = plotchar[4])