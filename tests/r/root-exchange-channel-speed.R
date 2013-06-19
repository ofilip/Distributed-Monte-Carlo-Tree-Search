setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols <- c("score", "ghost_time")
export=FALSE

if (export) {
	setEPS()
	postscript(file="../text/img/root-exchange-channel-speed.eps", width=6, height=4.5)
}

# reference plain MCTS data
plain_p.data <- read.delim("results/20130530-1000-plain-mcts-pesimistic.txt", row.names=NULL)
plain_p.data2 <- plain_p.data[c(wanted_cols)]
plain_p.data_melted <- melt(plain_p.data2, id=c("ghost_time"))
plain_p.scores <- cast(plain_p.data_melted, ghost_time~variable, mean)

lm <- lm( plain_p.scores$score ~ 1 + I(1/sqrt(plain_p.scores$ghost_time)))
c0 <- coef(lm )["(Intercept)"]
c1 <- coef(lm )["I(1/sqrt(plain_p.scores$ghost_time))"]

plain_strength <- function(t) { c0 + c1/sqrt(t) }
plain_strength_inv <- function(s) { (c1 / (s-c0))^2 }

par(mfrow=c(2,1))

# TODO: actualize data

# Distributed algorithm data
distr.data <- read.delim("results/20130618-1715-root-exchange-channel-speed.txt", row.names=NULL)
distr.data2 <- distr.data[c(wanted_cols, "channel_speed", "sims_per_sec_calculated", "sims_per_sec_total")]
distr.data_melted <- melt(distr.data2, id=c("ghost_time", "channel_speed"))
distr.scores <- cast(distr.data_melted, ghost_time+channel_speed~variable, mean)
distr.scores20 <- distr.scores[distr.scores$ghost_time==20,][distr.scores$channel_speed>=16,]
distr.scores40 <- distr.scores[distr.scores$ghost_time==40,][distr.scores$channel_speed>=16,]
times <- unique(distr.scores$ghost_time)
times2 <- seq(min(times),max(times))

# Absolute strength
plot(log(distr.scores20$channel_speed), distr.scores20$score, type="o",
	main="Root exchanging agents depending on channel speed",
	axes=FALSE,
	lty=1,
	col="red",
	pch=18,
	xlab="channel speed [bytes per second]",
	ylab="average score",
	ylim=c(600,2000)
)
lines(log(distr.scores40$channel_speed), distr.scores40$score, type="o",
	lty=1, col="green", pch=20)
axis(2, at=seq(600,2000,200))
axis(1, at=log(2^seq(4,18,2)), labels=2^seq(4,18,2))
box()
abline(h=plain_strength(20), lty=2, col="red")
abline(h=plain_strength(40), lty=2, col="green")

legend("topright", lty = c(1,1,2,2), pch=c(18,20,-1,-1), col=c("red","green","red","green"), 
	legend = c("20ms per tick",
			"40ms per tick", "Plain MCTS (20 ms)", "Plain MCTS (40 ms)"))


# Strength speedup
plot(log(distr.scores20$channel_speed), plain_strength_inv(distr.scores20$score)/20,
	main="Root exchanging agents - strength speedup", axis=FALSE,
	type="o", lty=1, col="red", pch=18, xlab="channel speed [bytes per second]", 
	ylab="strength speedup",
	ylim=c(0,2))
abline(h=1,lty=2)
axis(2, at=seq(600,2000,200))
axis(1, at=log(2^seq(4,18,2)), labels=2^seq(4,18,2))
box()

legend("topleft", lty = c(1,1,2), pch=c(18,20,-1), col=c("red","green","black"), 
	legend = c("20ms per tick",
			"40ms per tick", "Plain MCTS"))




#legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

if (export) dev.off()


