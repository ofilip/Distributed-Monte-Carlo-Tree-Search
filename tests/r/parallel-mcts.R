setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/parallel-mcts.eps", width=6, height=3.5)
}

data <- read.delim("r/parallel-mcts-matrix.txt", row.names=NULL)
colors <- c("red", "blue", "green", "yellow", "brown")
barplot(t(as.matrix(data))[2:6,], beside=TRUE, col=colors,
	ylab="strength speedup",
	ylim=c(0,15.5),
	xlab="thread count",
	axes=FALSE,
	names.arg=c("2","4","16")
	)
axis(2, at=seq(2,16,by=2))
legend("topleft", c(
	"Leaf parallelization",
	"Root parallelization",
	"Tree parallelization (global mutex)",
	"Tree parallelization (local mutex)",
	"Tree parallelization (virtual loss)"), bty="n", fill=colors
)
box()

if (export) dev.off()


