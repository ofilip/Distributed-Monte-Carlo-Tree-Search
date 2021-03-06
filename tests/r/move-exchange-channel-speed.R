setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols <- c("score", "ghost_time")
export=FALSE



if (export) {
	setEPS()
	postscript(file="../text/img/move-exchange-channel-speed.eps", width=6, height=9)
}
par(mfrow=c(2,1))
# Distributed algorithm data

distr.data <- read.delim("results/20130627-1212-move-exchange-channel-speed.txt", row.names=NULL)
distr.data2 <- distr.data[c(wanted_cols, "sims_per_sec_calculated", "sims_per_sec_total", "channel_speed",
	"transmitted_per_second_total")]
distr.data_melted <- melt(distr.data2, id=c("ghost_time","channel_speed"))
distr.scores <- cast(distr.data_melted, ghost_time+channel_speed~variable, mean)
distr.scores$speedup <- plain_strength_inv(distr.scores$score)/distr.scores$ghost_time
times <- unique(distr.scores$ghost_time)
times2 <- seq(min(times),max(times))

# Absolute strength
plot(log(distr.scores$channel_speed/1024.0), distr.scores$score, type="o",
	lty=1,
	col="red",
	pch=18,
	xlab="channel speed [kbps]",
	ylab="average score",
	ylim=c(800,1400),
	axes=FALSE
)
axis(2, at=seq(800,1400,200))
axis(1, at=log(2^seq(1,8,1)), labels=2^seq(1,8,1))
box()

# Strength speedup
plot(log(distr.scores$channel_speed/1024.0), distr.scores$speedup,
axes=FALSE,
	type="o", lty=1, col="red", pch=18, xlab="channel speed [kbps]", 
	ylab="strength speedup",
	ylim=c(0,4))
abline(h=1,lty=2)
axis(2, at=seq(0.0,4.0,0.5))
axis(1, at=log(2^seq(1,8,1)), labels=2^seq(1,8,1))
box()

if (export) dev.off()


