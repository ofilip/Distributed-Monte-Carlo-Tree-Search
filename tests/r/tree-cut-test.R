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
	"ru_prob", "transmitted_per_second_successfully", "transmitted_per_second_total")

data <- read.delim("results/20130618-1715-tree-cut-2-test.txt", row.names=NULL)
data2 <- data[c(wanted_cols)]
data_melted <- melt(data2, id=c("ghost_time", "ru_prob"))
scores <- cast(data_melted, ru_prob~variable, mean)

if (export) {
	setEPS()
	#postscript(file="../text/img/tree-cut-tuning.eps", width=6, height=5)
}

plot(scores$ghost_time, scores$score, type="o", axes=TRUE,
	main="Tree-cut exchange",
	xlab="Cuts per tick",
	ylab="Average score",
	ylim=c(1100,1800),
	lty = linetype[i],
	col = colors[i],
	pch = plotchar[i])

#axis(1, at=log(scores$cuts_per_tick), labels=scores$cuts_per_tick) 
#axis(2, at=seq(1000,1800,by=100), labels=seq(1000,1800,by=100))
#box()

if (export) dev.off()