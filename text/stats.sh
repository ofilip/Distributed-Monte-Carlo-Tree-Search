#!/bin/bash

chars=`pdftotext thesis.pdf -enc UTF-8 - | wc -c`
pages=`perl -e "print sprintf(\"%.3f\", $chars / 1800.0)"`
echo NS: $pages, znaku: $chars
