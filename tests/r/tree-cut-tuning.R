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
wanted_cols = c("score", "ghost_time", "ghost_sims_per_sec", "ghost_avg_decision_sims", "cuts_per_tick", "sims_per_sec_total")

data <- read.delim("results/20130612-1723-tree-cut-exchange-tuning.txt", row.names=NULL)
data2 <- data[c(wanted_cols)]
data_melted <- melt(data2, id=c("ghost_time", "cuts_per_tick"))
scores <- cast(data_melted, cuts_per_tick~variable, mean)

if (export) {
	setEPS()
	postscript(file="../text/img/tree-cut-tuning.eps", width=6, height=5)
}

plot(log(scores$cuts_per_tick), scores$score, type="o", axes=FALSE,
	main="Tree-cut exchange (communication redundancy tuning) ",
	xlab="Cuts per tick",
	ylab="Average score",
	ylim=c(1100,1800),
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])

axis(1, at=log(scores$cuts_per_tick), labels=scores$cuts_per_tick) 
axis(2, at=seq(1000,1800,by=100), labels=seq(1000,1800,by=100))
box()

if (export) dev.off()