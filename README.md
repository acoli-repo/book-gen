# Experiments on Summarization-Driven Book Generation
This repository contains the main components of the <i>prototype implementation of <b>Beta Writer</b></i>, the algorithmic author of [the first machine-generated research book published by Springer Nature](https://link.springer.com/book/10.1007/978-3-030-16800-1).

---
## Installation

### Required dependencies:
```
brew install python3
pip3 install numpy
pip3 install sklearn
pip3 install scipy
pip3 install matplotlib
pip3 install pandas
pip3 install gensim
```

### Download theanets from [here](https://github.com/lmjohns3/theanets)
Alternatively: 
```
wget https://github.com/lmjohns3/theanets/archive/master.zip
python3 setup.py install
```

### Install [Mate tools](https://code.google.com/archive/p/mate-tools/)
and place libraries and models into the /mate directory.


### Download [StanfordCoreNLP](https://stanfordnlp.github.io/CoreNLP/)
and place .jars in <code>dist/lib</code>

### Install [citeproc-java](https://michel-kraemer.github.io/citeproc-java/)
and place .jars in <code>dist/lib</code> 


## Quickstart
The script <code>pipeline.sh</code> contains all modules for end-to-end book generation.
```
sh pipeline.sh CORPUS_DIR gen/
```
where <code>CORPUS_DIR</code> = path to your A++ files and 

<code>gen/</code> = directory containing all generated files

Inspect generated <code>book.html</code> in <code>gen/</code> folder.


## License
This project is open source software and released under the [MIT license](https://opensource.org/licenses/MIT).


## Additional Information

Note that "Beta Writer" has originally been tailored to consume and process Springer custom-specific document type formats ([A++](http://devel.springer.de/A++/V2.4/DTD/)) and does not (yet) support generic PDF.

We currently provide the scripts for the major text processing tasks including:

* Preprocessing (entity masking)
* Document clustering (chapter generation) and visualization
* Syntactic restructuring (paraphrasing)
* Synonym generation


For more implementational details, please refer to our [system pipeline description in Section 2.3.](https://link.springer.com/content/pdf/bfm%3A978-3-030-16800-1%2F1.pdf).
