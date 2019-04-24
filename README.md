# Experiments on Summarization-Driven Book Generation
This repository contains the main pipeline and components of "Beta Writer", the algorithmic author of [the first machine-generated research book published by Springer Nature](https://link.springer.com/book/10.1007/978-3-030-16800-1).

---

## Quickstart
The script <b>pipeline.sh</b> contains all modules for end-to-end book generation.

## Components and Scripts

## License


## Additional Information

Note that "Beta Writer" has originally been tailored to consume and process Springer custom-specific document type formats ([A++](http://devel.springer.de/A++/V2.4/DTD/)) and does not (yet) support generic PDF.

We currently provide only the scripts for the major text processing tasks including:

* Preprocessing (entity masking)
* Document clustering (chapter generation)
* Syntactic restructuring (paraphrasing)
* Synonym generation

Moreover, we do not host the third-party software employed in the full pipeline, such as parsers, taggers, summarizers, because "Beta Writer" does not depend on any specific library. Appropriate pointers in the respective places of the pipeline indicate where external tools can be integrated. For this purpose, you should define your own wrappers.

