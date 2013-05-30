setwd("d:/diplomka/git/Distributed-Monte-Carlo-Tree-Search/tests")
library(reshape)
library(gplots)
source(file="r/utils.r")

data <- read.delim("results/20130414-0456-example-ghosts.txt")
data <- data[c("ghost_class","score")]
melted_data <- melt(data, id=c("ghost_class"))

scores <- cast(melted_data, ghost_class~variable, mean)


barplot2(scores$score,beside=TRUE, 
	border="black",
	main=c("Comparison of Example Ghosts and MCTS Controller"),
	xlab="Ghost controller name",
	ylab="Average score",
	font.lab=2,
	plot.grid=TRUE
)
