setwd("d:/diplomka/git/tests")
library(reshape)
library(gplots)
source(file="r/utils.R")

export=TRUE

if (export) {
	setEPS()
	postscript(file="../text/img/parallel-mcts.eps", width=6, height=4)
}

data <- read.delim("r/parallel-mcts-matrix.txt", row.names=NULL)

barplot(t(as.matrix(data))[2:6,], beside=TRUE, col=rainbow(5),
	main="Parallel MCTS - strength speedup",
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
	"Tree parallelization (virtual loss)"), bty="n", fill=rainbow(5)
)
box()

if (export) dev.off()


