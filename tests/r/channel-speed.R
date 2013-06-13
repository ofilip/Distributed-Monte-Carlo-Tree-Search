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
wanted_cols = c("score", "ghost_time", "ghost_sims_per_sec", "ghost_avg_decision_sims", "channel_speed", "transmitted_per_second_successfully")

root.data <- read.delim("results/20130531-2251-simulation-passing-channel-speed.txt", row.names=NULL)
root.data2 <- root.data[c(wanted_cols)]
root.data_melted <- melt(root.data2, id=c("channel_speed","ghost_time"))
root.scores <- cast(root.data_melted, ghost_time+channel_speed~variable, mean)
root.scores40 <- root.scores[root.scores$ghost_time==40,]
root.scores200 <- root.scores[root.scores$ghost_time==200,]

i <- i+1
legend = c(legend, "Root exchanging ghosts (40 ms)")
plot(root.scores40$channel_speed, root.scores40$score, type="o",
	main="Strength of distributed MCTS algorithms",
	xlab="Channel speed [bps]",
	ylab="Average score",
	xlim=c(0,100000),
	ylim=c(500,2200),
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])
i <- i+1
legend = c(legend, "Root exchanging ghosts (200 ms)")
lines(root.scores200$channel_speed, root.scores200$score, type="o", lty=linetype[i], col=colors[i], pch=plotchar[i])


i <- i+1
legend = c(legend, "Simulation passing ghosts (20 ms)")
simpas.data <- read.delim("results/20130611-1601-simulation-passing-channel-speed-2.txt", row.names=NULL)
simpas.data2 <- simpas.data[c(wanted_cols)]
simpas.melted_data <- melt(simpas.data2, id=c("ghost_time","channel_speed"))
simpas.scores <- cast(simpas.melted_data, ghost_time+channel_speed~variable, mean)
simpas.scores20 <- simpas.scores[simpas.scores$ghost_time==20,]
lines(simpas.scores20$channel_speed, simpas.scores20$score, type="o", lty = linetype[i], col = colors[i], pch = plotchar[i])

i <- i+1
legend = c(legend, "Simulation passing ghosts (40 ms)")
simpas.scores40 <- simpas.scores[simpas.scores$ghost_time==40,]
lines(simpas.scores40$channel_speed, simpas.scores40$score, type="o", lty = linetype[i], col = colors[i], pch = plotchar[i])

legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

