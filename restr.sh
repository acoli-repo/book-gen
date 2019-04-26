#!/bin/bash

# 1. RESTRUCTURE
echo "Restructuring sentences..."
# find all parsed sentences.
for f in $(find $1 -name '*.parsed.conll'); 
do 
  DIR=$(dirname "${f}")
  echo 'restructuring: '$f
  # take action (restructure) this file. 
  python3 restructure.py $f
done
