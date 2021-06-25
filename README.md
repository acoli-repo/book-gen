# Experiments on Summarization-Driven Book Generation
This repository contains the main components of the <i>prototype implementation of <b>Beta Writer</b></i>, the algorithmic author of [the first machine-generated research book published by Springer Nature (April 2019)](https://link.springer.com/book/10.1007/978-3-030-16800-1), developed by [Niko Schenk](https://www.english-linguistics.de/nschenk/), [Samuel Rönnqvist](https://github.com/sronnqvist) and other members of the Applied Computational Linguistics Lab under the direction of [Christian Chiarcos](https://github.com/chiarcos). To the best of our knowledge, the (full version of) system constitutes the state of industry on machine-generated scientific books as of today (June 2021).

Note that the open source release primarily builds on relatively shallow (but fast) techniques that produce a text based on direct extraction and aggregation over a larger collection of input texts as described in the preface of [first Beta Writer book](https://link.springer.com/book/10.1007/978-3-030-16800-1), but several of the techniques used for the purpose are no longer to be considered state of the art in scientific terms.

Subsequently, this workflow has thus been replaced with a knowledge-based approach that facilitates easier integration of content from different source texts or data bases. The revised workflow extends our earlier work on [semantic parsing (2014-2016)](https://github.com/acoli-repo/abi) and our [Flexible Integrated Transformation and Annotation Engineering (FINTAN) platform (2019-2022)](https://github.com/Pret-a-LLOD/Fintan) and uses an RDF adaptation of [Abstract Meaning Representations](https://amr.isi.edu/) for internal knowledge representation. With FINTAN not formally released yet, the revised book generation system is currently internally available only. If you would like to learn more, please contact [the ACoLi Lab](http://acoli.informatik.uni-frankfurt.de/) or [Christian Chiarcos](https://github.com/chiarcos).

---
## Installation

### Required dependencies:
```
brew install python3
pip3 install numpy
pip3 install sklearn
pip3 install scipy
pip3 install matplotlib
pip3 install gensim
```

### Install [Mate tools](https://code.google.com/archive/p/mate-tools/)
and place libraries and models into the <code>/mate</code> directory.
See [mate/README.txt](https://github.com/acoli-repo/book-gen/blob/master/mate/README.txt) for more details.


### Download [StanfordCoreNLP](https://stanfordnlp.github.io/CoreNLP/) and [citeproc-java](https://michel-kraemer.github.io/citeproc-java/)

Ideally open <code>beta_writer</code> as Netbeans project, link downloaded .jar files to project, and build <code>beta_writer.jar</code>.
The executable .jar should appear in <code>beta_writer/dist/</code>.

## Quickstart
The script <code>pipeline.sh</code> contains all modules for end-to-end book generation. 

Please point PYTHON to your local python installation (change line 32 in <code>pipeline.sh</code>)
```
sh pipeline.sh CORPUS_DIR gen/
```
where <code>CORPUS_DIR</code> = path to your A++ files and 

<code>gen/</code> = directory containing all generated files

Inspect generated <code>book.html</code> in <code>gen/</code> folder.

## Description

Note that <i><b>Beta Writer</b></i> has originally been tailored to consume and process Springer custom-specific document type formats ([A++](http://devel.springer.de/A++/V2.4/DTD/)) and does not (yet) support generic PDF.

We currently provide the scripts for the major text processing tasks including:

* Preprocessing (e.g., entity masking of chemical compounds with <code>mask_entities.py</code>)
* Book structure generation (<code>mkstructure_html.py</code>) and visualization (<code>plot.py</code>)
* Syntactic restructuring/paraphrasing (<code>restructuring.py</code>)
* Synonym generation (<code>synonyms.py</code>)

The current release makes use of [textrank](https://github.com/summanlp/textrank) for extractive summarization.


For more implementational details, please refer to our [system pipeline description in Section 2.3.](https://link.springer.com/content/pdf/bfm%3A978-3-030-16800-1%2F1.pdf).


## License
This project is open source software and released under the [MIT license](https://opensource.org/licenses/MIT).


