setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

i <- 0
n <- 2
legend = c()
colors <- rainbow(n)
linetype <- c(1:n)
plotchar <- seq(18,18+n,1)
wanted_cols = c("score", "ghost_time", "ghost_sims_per_sec", "ghost_avg_decision_sims")
export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/plain-mcts-strength.eps", width=6, height=4.5)
}

plain_p.data <- read.delim("results/20130530-1000-plain-mcts-pesimistic.txt", row.names=NULL)
plain_p.data2 <- plain_p.data[c(wanted_cols)]
plain_p.data_melted <- melt(plain_p.data2, id=c("ghost_time"))
plain_p.scores <- cast(plain_p.data_melted, ghost_time~variable, mean)


i <- i+1
legend = c(legend, "Plain MCTS (pesimistic)")
plot(plain_p.scores$ghost_time, plain_p.scores$score, type="o",
	main="Plain MCTS strength",
	xlab="time [ms]",
	ylab="average score",
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

lm <- lm( plain_p.scores$score ~ 1 + I(1/sqrt(plain_p.scores$ghost_time)))
c0 <- coef(lm )["(Intercept)"]
c1 <- coef(lm )["I(1/sqrt(plain_p.scores$ghost_time))"]

plain_strength <- function(t) { c0 + c1/sqrt(t) }

# 1/t^-0.5 regression
lines(plain_p.scores$ghost_time, plain_strength(plain_p.scores$ghost_time), lty=2)


legend("topright", lty = linetype, pch = plotchar, col = colors, legend = legend)

if (export) dev.off()

