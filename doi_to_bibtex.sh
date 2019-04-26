#!/bin/bash

url="http://dx.doi.org/"$1
curl -LH 'Accept: application/x-bibtex' $url
