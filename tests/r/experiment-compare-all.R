setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

i <- 0
n <- 10
legend = c()
colors <- rainbow(n)
linetype <- c(1:n)
plotchar <- seq(18,18+n,1)
wanted_cols = c("score", "ghost_time", "ghost_sims_per_sec", "ghost_avg_decision_sims")

plain_p.data <- read.delim("results/20130530-1000-plain-mcts-pesimistic.txt", row.names=NULL)
plain_p.data2 <- plain_p.data[c(wanted_cols)]
plain_p.data_melted <- melt(plain_p.data2, id=c("ghost_time"))
plain_p.scores <- cast(plain_p.data_melted, ghost_time~variable, mean)


i <- i+1
legend = c(legend, "Plain MCTS (pesimistic)")
plot(plain_p.scores$ghost_time, plain_p.scores$score, type="o",
	main="Plain MCTS strength",
	xlab="Time [s]",
	ylab="Average score",
	ylim=c(500,2200),
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])


i <- i+1
legend = c(legend, "Plain MCTS (optimistic)")
plain_o.data <- read.delim("results/20130525-0312-plain-mcts-optimistic.txt", row.names=NULL)
plain_o.data2 <- plain_o.data[c(wanted_cols)]
plain_o.melted_data <- melt(plain_o.data2, id=c("ghost_time"))
plain_o.scores <- cast(plain_o.melted_data, ghost_time~variable, mean)
lines(plain_o.scores$ghost_time, plain_o.scores$score, type="o",
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])

i <- i+1
legend = c(legend, "Dummy ghosts")
dummy.data <- read.delim("results/20130428-1305-dummy-ghosts-mt.txt", row.names=NULL)
dummy.data2 <- dummy.data[c(wanted_cols, "synchronization_ratio", "ghost_class")]
dummy.melted_data <- melt(dummy.data2, id=c("ghost_time", "ghost_class"))
dummy.scores <- cast(dummy.melted_data, ghost_time+ghost_class~variable, mean)
dummy_u.scores <- dummy.scores[dummy.scores$ghost_class=="DummyGhosts",]
dummy_s.scores <- dummy.scores[dummy.scores$ghost_class=="SynchronizedDummyGhosts",]
lines(dummy_s.scores$ghost_time, dummy_s.scores$score, type="o",
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])


i <- i+1
legend = c(legend, "Root exchanging ghosts")
root.data <- read.delim("results/20130512-2336-dummy-root-exchange-9.txt", row.names=NULL)
#root.data <- read.delim("results/20130526-1650-move-exchange-optimistic.txt", row.names=NULL)
root.data2 <- root.data[c(wanted_cols, "transmitted_per_second_total")]
root.melted_data <- melt(root.data2, id=c("ghost_time"))
root.scores <- cast(root.melted_data, ghost_time~variable, mean)
lines(root.scores$ghost_time, root.scores$score, type="o",
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])

i <- i+1
legend = c(legend, "Simulation results passing ghosts")
simpas.data <- read.delim("results/20130527-0352-simulation-passing-optimistic.txt", row.names=NULL)
simpas.data2 <- simpas.data[c(wanted_cols, "transmitted_per_second_total")]
simpas.melted_data <- melt(simpas.data2, id=c("ghost_time"))
simpas.scores <- cast(simpas.melted_data, ghost_time~variable, mean)
lines(simpas.scores$ghost_time, simpas.scores$score, type="o",
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])

legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

