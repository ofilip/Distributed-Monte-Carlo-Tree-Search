setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/mcts-tuning.eps", width=6, height=8.5)
}

par(mfrow=c(3,1))

# C tuning
data <- read.delim("results/20130411-c-tuning.txt", row.names=NULL)
data <- data[c("score", "ghost_ucb_coef", "ghost_sims_per_sec")]
melted_data <- melt(data, id=c("ghost_ucb_coef"))
scores <- cast(melted_data, ghost_ucb_coef~variable, mean)
plot(scores$ghost_ucb_coef, scores$score, log="x", type="o",
	main="Tuning of UCB1 coefficient",
	xlab="UCB1 coefficient",
	ylab="Average score",
	ylim=c(800,1600),
	col="red",
	axes=FALSE
	)
axis(1, at=scores$ghost_ucb_coef)
axis(2, at=seq(800,1600,by=200))
box()

# Death weight tuning
data <- read.delim("results/20130408-2112-dw-test.txt", row.names=NULL)
data <- data[c("score", "ghost_death_weight", "ghost_sims_per_sec")]
melted_data <- melt(data, id=c("ghost_death_weight"))
scores <- cast(melted_data, ghost_death_weight~variable, mean)
plot(scores$ghost_death_weight, scores$score, type="o",
	main="Tuning of death_weight coefficient",
	xlab="death_weight",
	ylab="Average score",
	col="red"
	)




# Simulation depth tuning
data <- read.delim("results/20130413-1017-simdepth-tuning.txt", row.names=NULL)
data <- data[c("score", "ghost_sim_depth", "ghost_sims_per_sec")]
melted_data <- melt(data, id=c("ghost_sim_depth"))
scores <- cast(melted_data, ghost_sim_depth~variable, mean)
plot(scores$ghost_sim_depth, scores$score, type="o",
	main="Tuning of simulation depth",
	xlab="Simulation depth (in ticks)",
	ylab="Average score",
	col="red"
	)

if (export) dev.off()
