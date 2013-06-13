setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols = c("score", "ghost_time")
export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/dummy-ghosts-strength.eps", width=6, height=4.5)
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

par(mfrow=c(1,2))

# Distributed algorithm data
dummy.data <- read.delim("results/20130606-1724-dummy.txt", row.names=NULL)
dummy.data2 <- dummy.data[c(wanted_cols)]
dummy.data_melted <- melt(dummy.data2, id=c("ghost_time"))
dummy.scores <- cast(dummy.data_melted, ghost_time~variable, mean)

sdummy.data <- read.delim("results/20130610-0251-dummy-synchronized.txt", row.names=NULL)
sdummy.data2 <- sdummy.data[c(wanted_cols)]
sdummy.data_melted <- melt(sdummy.data2, id=c("ghost_time"))
sdummy.scores <- cast(sdummy.data_melted, ghost_time~variable, mean)

times <- unique(dummy.scores$ghost_time)
times2 <- seq(min(times),max(times))

# Absolute strength
plot(times, dummy.scores$score, type="o",
	main="Independent agents - strength",
	lty=1,
	col="red",
	pch=18,
	xlab="time [ms]",
	ylab="average score"
)
lines(times, sdummy.scores$score, type="o",
	lty=1, col="green", pch=19)
lines(times2, plain_strength(times2), lty=2)


# Strength speedup
plot(times, plain_strength_inv(dummy.scores$score)/times,
	main="Independent agents - strength speedup",
	ylim=c(0,4),
	type="o", lty=1, col="red", pch=18, xlab="time [ms]", ylab="strength speedup")
lines(times, plain_strength_inv(sdummy.scores$score)/times,
	type="o", lty=1, col="green", pch=19)
abline(h=1,lty=2)







#legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

if (export) dev.off()


