setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

wanted_cols <- c("score", "ghost_time")
export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/simulation-passing-unreliable.eps", width=6, height=9)
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

# Distributed algorithm data
#distr.data <- read.delim("results/20130612-2358-simulation-passing.txt", row.names=NULL)
distr.data <- read.delim("results/20130624-1711-simulation-passing-unreliable.txt", row.names=NULL)
distr.data2 <- distr.data[c(wanted_cols,"reliability")]
distr.data_melted <- melt(distr.data2, id=c("ghost_time","reliability"))
distr.scores <- cast(distr.data_melted, ghost_time+reliability~variable, mean)
distr.scores[distr.scores$reliability=="NaN",]$reliability <- 1.0

distr.scores$speedup <- plain_strength_inv(distr.scores$score)/distr.scores$ghost_time

times <- unique(distr.scores$ghost_time)
times2 <- seq(min(times),max(times))

# Absolute strength
plot(distr.scores$reliability, distr.scores$score, type="o",
	main="Simulation results passing agents - strength",
	lty=1,
	col="red",
	pch=18,
	xlab="reliability",
	ylab="average score"
)
lines(times2, plain_strength(times2), lty=2)


# Strength speedup
plot(distr.scores$reliability, plain_strength_inv(distr.scores$score)/times,
	main="Simulation results passing agents - strength speedup",
	type="o", lty=1, col="red", pch=18, xlab="reliability", 
	ylab="strength speedup",
	ylim=c(0,2.5))
abline(h=1,lty=2)







#legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

if (export) dev.off()


