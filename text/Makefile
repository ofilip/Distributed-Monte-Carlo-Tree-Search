all: thesis.pdf

# Vyroba PDF primo z DVI by byla prijemnejsi, ale vetsina verzi dvipdfm nici obrazky
#thesis.pdf: thesis.dvi
#	dvipdfm -o $@ -p a4 -r 600 $<

thesis.pdf: thesis.ps
	ps2pdf $< $@

thesis.ps: thesis.dvi
	dvips -o $@ -D600 -t a4 $<

# LaTeX je potreba spustit dvakrat, aby spravne spocital odkazy
thesis.dvi: thesis.tex $(wildcard *.tex) img/*
	psnup.exe -l img/plain-mcts-strength.eps img/plain-mcts-strength-90.eps
	cslatex $<
	bibtex thesis
	cslatex $<
	cslatex $<

clean:
	rm -f *.{log,dvi,aux,toc,lof,out,bbl,tex~,blg,run.xml,bcf,loa,tdo} thesis-blx.bib thesis.ps thesis.pdf
