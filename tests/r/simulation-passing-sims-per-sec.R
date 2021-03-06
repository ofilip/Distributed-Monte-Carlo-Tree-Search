setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols <- c("score", "ghost_time")
export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/simulation-passing-sims-per-sec.eps", width=6, height=4.5)
}

# Distributed algorithm data
distr.data <- read.delim("results/20130531-2251-simulation-passing-channel-speed.txt", row.names=NULL)
distr.data2 <- distr.data[c(wanted_cols, "sims_per_sec_calculated", "sims_per_sec_total", "channel_speed")]
distr.data_melted <- melt(distr.data2, id=c("ghost_time","channel_speed"))
distr.scores <- cast(distr.data_melted, ghost_time+channel_speed~variable, mean)
distr.scores40 <- distr.scores[distr.scores$ghost_time==40,]
distr.scores200 <- distr.scores[distr.scores$ghost_time==200,]
times <- unique(distr.scores$ghost_time)
times2 <- seq(min(times),max(times))

plot(distr.scores40$channel_speed/1000.0, distr.scores40$sims_per_sec_calculated, type="o",
	main="Simulation results passing agents - simulations per second",
	lty=1,
	col="red",
	pch=18,
	xlab="channel speed [kbps]",
	ylab="average score",
	ylim=c(30000,50000)
)
lines(distr.scores200$channel_speed/1000.0, distr.scores200$sims_per_sec_calculated, type="o",
	main="Simulation results passing agents - simulations per second",
	lty=2,
	col="red",
	pch=18
)

if (export) dev.off()


