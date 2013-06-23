setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols <- c("score", "ghost_time")
export=FALSE

if (export) {
	setEPS()
	postscript(file="../text/img/tree-cut-cut-size.eps", width=6, height=9)
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
distr.data <- read.delim("results/20130623-0408-tree-cut-cut-size.txt", row.names=NULL)

# correct data
distr.data$cut_size <- 0
distr.data[1:10,]$cut_size <- 1
distr.data[11:20,]$cut_size <- 2
distr.data[21:30,]$cut_size <- 4
distr.data[31:40,]$cut_size <- 8
distr.data[41:50,]$cut_size <- 16
distr.data[51:60,]$cut_size <- 32
distr.data[61:70,]$cut_size <- 64
distr.data[71:160,]$cut_size <- 1
distr.data[161:250,]$cut_size <- 2
distr.data[251:340,]$cut_size <- 4
distr.data[341:430,]$cut_size <- 8
distr.data[431:520,]$cut_size <- 16
distr.data[521:610,]$cut_size <- 32
distr.data[611:700,]$cut_size <- 64


distr.data2 <- distr.data[c(wanted_cols, "channel_speed", "sims_per_sec_calculated", "sims_per_sec_total","transmitted_per_second_total", "cut_size")]
distr.data_melted <- melt(distr.data2, id=c("ghost_time", "channel_speed","cut_size"))
distr.scores <- cast(distr.data_melted, ghost_time+cut_size+channel_speed~variable, mean)
distr.scores$speedup <- plain_strength_inv(distr.scores$score)/distr.scores$ghost_time
times <- unique(distr.scores$ghost_time)
times2 <- seq(min(times),max(times))

# Absolute strength
plot(log(distr.scores$channel_speed), distr.scores$score, type="o",
	main="Tree cut exchanging agents in dependence on channel speed",
	axes=FALSE,
	lty=1,
	col="red",
	pch=18,
	xlab="channel speed [bytes per second]",
	ylab="average score",
	ylim=c(600,2000)
)
axis(2, at=seq(600,2000,200))
axis(1, at=log(2^seq(4,18,2)), labels=2^seq(4,18,2))
box()
abline(h=plain_strength(40), lty=2, col="black")


legend("topright", lty = c(1,2), pch=c(18,-1), col=c("red","black"), 
	legend = c("Tree cut exchanging agents", "Plain MCTS"))


# Strength speedup
plot(log(distr.scores$channel_speed), plain_strength_inv(distr.scores$score)/40,
	main="Tree cut exchanging agents - strength speedup", axes=FALSE,
	type="o", lty=1, col="red", pch=18, xlab="channel speed [bytes per second]", 
	ylab="strength speedup",
	ylim=c(0,4))
abline(h=1,lty=2)
axis(2, at=seq(0.0,4.0,0.5))
axis(1, at=log(2^seq(4,18,2)), labels=2^seq(4,18,2))
box()

legend("topright", lty = c(1,2), pch=c(18,-1), col=c("red","black"), 
	legend = c("Tree cut exchanging agents", "Plain MCTS"))


if (export) dev.off()


