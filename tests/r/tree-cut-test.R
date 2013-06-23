setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")
export=FALSE


i <- 1
n <- 1
legend = c()
colors <- rainbow(n)
linetype <- c(1:n)
plotchar <- seq(18,18+n,1)
wanted_cols = c("score", "ghost_time", "ghost_sims_per_sec", "ghost_avg_decision_sims", "sims_per_sec_total", 
	"transmitted_per_second_successfully", "transmitted_per_second_total","channel_speed")

# reference plain MCTS data
plain_p.data <- read.delim("results/20130530-1000-plain-mcts-pesimistic.txt", row.names=NULL)
plain_p.data2 <- plain_p.data[c("score","ghost_time")]
plain_p.data_melted <- melt(plain_p.data2, id=c("ghost_time"))
plain_p.scores <- cast(plain_p.data_melted, ghost_time~variable, mean)

lm <- lm( plain_p.scores$score ~ 1 + I(1/sqrt(plain_p.scores$ghost_time)))
c0 <- coef(lm )["(Intercept)"]
c1 <- coef(lm )["I(1/sqrt(plain_p.scores$ghost_time))"]

plain_strength <- function(t) { c0 + c1/sqrt(t) }
plain_strength_inv <- function(s) { (c1 / (s-c0))^2 }

# Test data
data <- read.delim("results/20130621-0526-tree-cut-2-test.txt", row.names=NULL)
data2 <- data[c(wanted_cols)]
data_melted <- melt(data2, id=c("ghost_time"))
scores <- cast(data_melted, ghost_time~variable, mean)
times <- unique(scores$ghost_time)
times2 <- seq(min(times),max(times))


if (export) {
	setEPS()
	#postscript(file="../text/img/tree-cut-tuning.eps", width=6, height=5)
}

plot(scores$ghost_time, scores$score, type="o", axes=TRUE,
	main="Tree-cut exchange",
	xlab="Cuts per tick",
	ylab="Average score",
	ylim=c(800,1400),
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])
lines(times2, plain_strength(times2), lty=2)

#axis(1, at=log(scores$cuts_per_tick), labels=scores$cuts_per_tick) 
#axis(2, at=seq(1000,1800,by=100), labels=seq(1000,1800,by=100))
#box()

if (export) dev.off()