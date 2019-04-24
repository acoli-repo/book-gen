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
* Document clustering (chapter generation) and visualization
* Syntactic restructuring (paraphrasing)
* Synonym generation


Moreover, we do not host the third-party software employed in the full pipeline, such as [parsers](https://stanfordnlp.github.io/CoreNLP/), [taggers](https://stanfordnlp.github.io/CoreNLP/), [summarizers](https://github.com/summanlp/textrank), because "Beta Writer" work genericly and does not depend on any specific NLP library. Appropriate pointers in the respective places of the pipeline indicate where a variety of different external tools can be integrated. (For this purpose, you should define your own wrappers.) For implementational details and the actual components used, please refer to our [system pipeline description in Section 2.3.](https://link.springer.com/content/pdf/bfm%3A978-3-030-16800-1%2F1.pdf).
