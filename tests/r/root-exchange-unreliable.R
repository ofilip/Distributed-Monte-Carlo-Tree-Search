setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols <- c("score", "ghost_time", "ru_prob")
export=FALSE

if (export) {
	setEPS()
	postscript(file="../text/img/root-exchange-channel-speed.eps", width=6, height=4.5)
}

# reference plain MCTS data
plain_p.data <- read.delim("results/20130530-1000-plain-mcts-pesimistic.txt", row.names=NULL)
plain_p.data2 <- plain_p.data[c(c("score", "ghost_time"))]
plain_p.data_melted <- melt(plain_p.data2, id=c("ghost_time"))
plain_p.scores <- cast(plain_p.data_melted, ghost_time~variable, mean)

lm <- lm( plain_p.scores$score ~ 1 + I(1/sqrt(plain_p.scores$ghost_time)))

c0 <- coef(lm)["(Intercept)"]
c1 <- coef(lm)["I(1/sqrt(plain_p.scores$ghost_time))"]

plain_strength <- function(t) { c0 + c1/sqrt(t) }
plain_strength_inv <- function(s) { (c1 / (s-c0))^2 }

# Distributed algorithm data
distr.data <- read.delim("results/20130616-0323-root-exchange-unreliable.txt", row.names=NULL)
distr.data2 <- distr.data[c(wanted_cols)]
distr.data_melted <- melt(distr.data2, id=c("ghost_time","ru_prob"))
distr.scores <- cast(distr.data_melted, ghost_time+ru_prob~variable, mean)
scores20 <- distr.scores[distr.scores$ghost_time==20,]
scores40 <- distr.scores[distr.scores$ghost_time==20,]

# Absolute strength
##plot(times, distr.scores$score, type="o",
#	main="Root exchanging agents - strength",
#	lty=1,
#	col="red",
#	pch=18,
#	xlab="time [ms]",
#	ylab="average score"
#)
#lines(times2, plain_strength(times2), lty=2)


# Strength speedup
plot(scores20$ru_prob, plain_strength_inv(scores20$score)/scores20$ghost_time,
	main="Root exchanging agents - strength speedup",
	type="o", lty=1, col="red", pch=18, xlab="time [ms]", 
	ylab="strength speedup")
abline(h=1,lty=2)







#legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

if (export) dev.off()


